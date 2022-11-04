package ge.ufc.webservices.ws;

import ge.ufc.webservices.dao.DatabaseManager;
import ge.ufc.webservices.exceptions.*;
import ge.ufc.webservices.exceptions.InternalError;
import ge.ufc.webservices.model.User;
import ge.ufc.webservices.repository.UserRepository;
import ge.ufc.webservices.repository.UserRepositoryImpl;
import ge.ufc.webservices.util.Utilities;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import java.sql.Connection;
import java.sql.SQLException;

@WebService
public class UserServiceWSImpl implements UserServiceWS {
    private Connection connection;
    @Resource
    WebServiceContext wsContext;

    @Override
    public User getUser(int user_id) throws UserNotFound, DatabaseException, AgentAccessDenied, AgentAuthFailed, InternalError {
        try {
            connection = DatabaseManager.getDatabaseConnection();
            UserRepository user = new UserRepositoryImpl(connection);

            int agent_id = Utilities.checkAgentID(wsContext.getMessageContext());

            if (!Utilities.checkAgentInfo(connection, agent_id, wsContext)) {
                throw new DatabaseException("Exception");
            }

            return user.check(user_id);

        } finally {
            DatabaseManager.close(connection);
        }
    }

    @Override
    public int pay(String tr_id, int user_id, double amount) throws DuplicateFault, AmountNotPositive, UserNotFound, DatabaseException, AgentAccessDenied, AgentAuthFailed, TransactionNotFound, InternalError {
        try {
            connection = DatabaseManager.getDatabaseConnection();
            UserRepository user = new UserRepositoryImpl(connection);

            int agent_id = Utilities.checkAgentID(wsContext.getMessageContext());

            if (!Utilities.checkAgentInfo(connection, agent_id, wsContext)) {
                throw new DatabaseException("Exception");
            }

            return user.pay(agent_id, tr_id, user_id, amount);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            DatabaseManager.close(connection);
        }
        return 0;
    }

    @Override
    public int status(String transaction_id) throws AgentAccessDenied, TransactionNotFound, AgentAuthFailed, InternalError, DatabaseException {
        try {
            connection = DatabaseManager.getDatabaseConnection();
            UserRepository user = new UserRepositoryImpl(connection);

            int agent_id = Utilities.checkAgentID(wsContext.getMessageContext());

            if (!Utilities.checkAgentInfo(connection, agent_id, wsContext)) {
                throw new DatabaseException("Exception");
            }
            return user.status(transaction_id);

        } finally {
            DatabaseManager.close(connection);
        }
    }

}
