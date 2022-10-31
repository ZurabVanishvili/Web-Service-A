package ge.ufc.webservices.repository;

import ge.ufc.webservices.exceptions.*;
import ge.ufc.webservices.exceptions.InternalError;
import ge.ufc.webservices.model.User;
import ge.ufc.webservices.ws.UserServiceWSImpl;

import java.sql.*;
import java.util.Locale;
import java.util.Objects;


@SuppressWarnings("all")
public class UserRepositoryImpl implements UserRepository {
    private static final String DUPLICATE_KEY_ERROR = "23505";
    private Connection connection;

    public UserRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public User check(int user_id) throws UserNotFound, DatabaseException {
        User user = new User();
        try (PreparedStatement ps = connection.prepareStatement(String.valueOf(SQLQuery.SelectUserById.query))) {
            ps.setInt(1, user_id);
            try (ResultSet rs = ps.executeQuery()) {
                //find user
                if (rs.next()) {
                    //return Fullname and Balance
                    String fullName = rs.getString("firstname").substring(0, 1).toUpperCase(Locale.ROOT) +
                            "." +" "+ rs.getString("lastname").substring(0, 1).toUpperCase(Locale.ROOT) + ".";
                    user.setFullName(fullName);
                    user.setBalance(rs.getDouble("balance"));
                    return user;
                } else {
                    throw new UserNotFound("User not found");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("User not found");
        }
    }

    @Override
    public int pay(int agent_id, String transaction_id, int user_id, double amount) throws DuplicateFault, UserNotFound, AmountNotPositive, DatabaseException {
        double balance = 0;
        int system_transaction_id = 0;
        ResultSet resultSet;
        ResultSet resultSet2;

        try (PreparedStatement ps = connection.prepareStatement(SQLQuery.InsertIntoTransactions.query, new String[]{
                "agent_transaction_id"
        });
             PreparedStatement ps2 = connection.prepareStatement(SQLQuery.SelectBalanceFromUserByID.query);
             PreparedStatement ps3 = connection.prepareStatement(SQLQuery.UpdateUserBalance.query);
             PreparedStatement ps4 = connection.prepareStatement(SQLQuery.SelectFromUsers.query);
             PreparedStatement ps5 = connection.prepareStatement(SQLQuery.ReturnSysTransID.query)) {

            UserServiceWSImpl a = new UserServiceWSImpl();
            //get user balance
            ps2.setInt(1, user_id);
            try (ResultSet rs = ps2.executeQuery()) {
                if (rs.next()) {
                    balance += rs.getDouble("balance");
                }
            }
            //get requested user to fill balance

            ps4.setInt(1, user_id);
            try (ResultSet rs5 = ps4.executeQuery()) {
                if (!rs5.next()) {
                    throw new UserNotFound();
                }
            }
            try {
                //check amount
                if (amount > 0) {
                    ps.setInt(1, agent_id);
                    ps.setString(2, transaction_id);
                    ps.setInt(3, user_id);
                    ps.setDouble(4, amount);
                    ps.setTimestamp(5, new java.sql.Timestamp(new java.util.Date().getTime()));

                    ps.execute();

                } else {
                    throw new AmountNotPositive();
                }
            } catch (SQLException e) {
                //check if duplicate request
                if (Objects.equals(e.getSQLState(), DUPLICATE_KEY_ERROR)) {
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        system_transaction_id = rs.getInt(1);
                        ps5.setInt(1, system_transaction_id);
                        resultSet2 = ps5.executeQuery();
                        if (resultSet2.next()) {
                            int user_id1 = resultSet2.getInt("user_id");
                            double amount1 = resultSet2.getDouble("amount");

                            if (!(user_id1 == user_id && amount1 == amount)) {
                                //agent changed user_id or amount --> throw DuplicateFault
                                throw new DuplicateFault();
                            }
                        }
                        return system_transaction_id;

                    }
                }
            }
            //update user's balance

            ps3.setDouble(1, balance + amount);
            ps3.setInt(2, user_id);
            ps3.executeUpdate();


            resultSet = ps.getGeneratedKeys();
            if (resultSet.next()) {
                system_transaction_id = resultSet.getInt(1);

            }

        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
        return system_transaction_id;
    }

    @Override
    public int status(String transaction_id) throws TransactionNotFound, InternalError {

        try (PreparedStatement ps = connection.prepareStatement(SQLQuery.ReturnIDForStatus.query)) {
            ps.setString(1, transaction_id);
            try (ResultSet rs = ps.executeQuery()) {
                //find last system_transaction_id
                if (rs.next()) {
                    return rs.getInt("system_transaction_id");
                } else {
                    throw new TransactionNotFound();
                }
            }
        } catch (SQLException e) {
            throw new InternalError(e.getMessage());
        }
    }


}
