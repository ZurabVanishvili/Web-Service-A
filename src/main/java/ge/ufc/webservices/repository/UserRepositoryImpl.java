package ge.ufc.webservices.repository;

import ge.ufc.webservices.exceptions.*;
import ge.ufc.webservices.exceptions.InternalError;
import ge.ufc.webservices.model.User;
import ge.ufc.webservices.ws.UserServiceWSImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.Locale;
import java.util.Objects;


@SuppressWarnings("all")
public class UserRepositoryImpl implements UserRepository {
    private static final String DUPLICATE_KEY_ERROR = "23505";
    private Connection connection;
    private static final Logger lgg = LogManager.getLogger();

    public UserRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public User check(int user_id) throws UserNotFound, DatabaseException, InternalError {
        User user = new User();
        try (PreparedStatement ps = connection.prepareStatement(String.valueOf(SQLQuery.SelectUserById.query))) {
            ps.setInt(1, user_id);
            lgg.info("Searching for user with id : " + user_id);
            try (ResultSet rs = ps.executeQuery()) {
                //find user
                if (rs.next()) {
                    //return Fullname and Balance
                    String fullName = rs.getString("firstname").substring(0, 1).toUpperCase(Locale.ROOT) +
                            "." + " " + rs.getString("lastname").substring(0, 1).toUpperCase(Locale.ROOT) + ".";
                    user.setFullName(fullName);
                    user.setBalance(rs.getDouble("balance"));
                    lgg.info("Found");
                    lgg.trace(user);
                    return user;
                } else {
                    lgg.error("User not found");
                    throw new UserNotFound("User not found");
                }
            }
        } catch (SQLException e) {
            lgg.error(" Database Exception");
            throw new InternalError(" Database Exception");
        }
    }

    @Override
    public int pay(int agent_id, String transaction_id, int user_id, double amount) throws DuplicateFault, UserNotFound, AmountNotPositive, DatabaseException, SQLException, TransactionNotFound, InternalError {
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

            lgg.info("Inserting into transactions");
            UserServiceWSImpl a = new UserServiceWSImpl();

            //აქ ჯერ ვიღებ არსებულ ბალანსს
            ps2.setInt(1, user_id);
            try (ResultSet rs = ps2.executeQuery()) {
                if (rs.next()) {
                    balance += rs.getDouble("balance");
                }
            }
            //შემდეგ უკვე იუზერს ვამოწმებ არსებობს თუ არა

            ps4.setInt(1, user_id);
            try (ResultSet rs5 = ps4.executeQuery()) {
                if (!rs5.next()) {
                    throw new UserNotFound();
                }
            }
            try {
                //აქ ვამოწმებ შემოსატანი თანხის ვალიდურობას და შემდეგ ვა-insert-ებ უკვე ცხრილში
                if (amount > 0) {
                    ps.setInt(1, agent_id);
                    ps.setString(2, transaction_id);
                    ps.setInt(3, user_id);
                    ps.setDouble(4, amount);
                    ps.setTimestamp(5, new Timestamp(new java.util.Date().getTime()));

                    ps.execute();

                } else {
                    throw new AmountNotPositive();
                }
            } catch (SQLException e) {
                lgg.error(e);
                //ვამოწმებ არის თუ არა ტრანზაქცია დუპლიკატი

                if (Objects.equals(e.getSQLState(), DUPLICATE_KEY_ERROR)) {
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        system_transaction_id = status(transaction_id);
                        ps5.setInt(1, system_transaction_id);
                        resultSet2 = ps5.executeQuery();
                        if (resultSet2.next()) {
                            //აქ ცხრილიდან მომაქვს შესაბამისი id და amount
                            int user_id1 = resultSet2.getInt("user_id");
                            double amount1 = resultSet2.getDouble("amount");
                            //და აქ ვამოწმებ უკვე თუ რომელიმე შეცვლილია ვისვრი DuplicateFault-ს
                            if (!(user_id1 == user_id && amount1 == amount)) {
                                //agent changed user_id or amount --> throw DuplicateFault
                                lgg.error(new DuplicateFault());
                                throw new DuplicateFault();
                            }
                        }
                        return system_transaction_id;

                    }
                }
            }

            //თუკი ყველა შემოწმება გაიარა ტრანზაქციამ უკვე ვა-update-ბ users და transactions ცხრილებს და ვაბრუნებ
            // დაგენერირებულ system_transaction_id-ს.

            ps3.setDouble(1, balance + amount);
            ps3.setInt(2, user_id);
            ps3.executeUpdate();


            resultSet = ps.getGeneratedKeys();
            if (resultSet.next()) {
                system_transaction_id = resultSet.getInt(1);

            }

        } catch (SQLException | TransactionNotFound | InternalError e) {
            lgg.error(e);
            throw e;
        }
        lgg.trace(system_transaction_id);
        return system_transaction_id;
    }

    @Override
    public int status(String transaction_id) throws TransactionNotFound, InternalError {

        try (PreparedStatement ps = connection.prepareStatement(SQLQuery.ReturnIDForStatus.query)) {
            ps.setString(1, transaction_id);
            try (ResultSet rs = ps.executeQuery()) {

//                ვეძებ system_transaction_id-ს agent_transactio_id-თ და თუ არსებობს ვაბრუნებ,თუარადა ვისვრი შესაბამის exception-ს

                if (rs.next()) {
                    lgg.trace( rs.getInt("system_transaction_id"));
                    return rs.getInt("system_transaction_id");
                } else {
                    lgg.error(new TransactionNotFound());
                    throw new TransactionNotFound();
                }
            }
        } catch (SQLException e) {
            lgg.error(new InternalError(e.getMessage()));
            throw new InternalError(e.getMessage());
        }
    }


}
