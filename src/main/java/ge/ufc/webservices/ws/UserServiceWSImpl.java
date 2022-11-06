package ge.ufc.webservices.ws;

import ge.ufc.webservices.dao.DatabaseManager;
import ge.ufc.webservices.exceptions.*;
import ge.ufc.webservices.exceptions.InternalError;
import ge.ufc.webservices.model.User;
import ge.ufc.webservices.repository.UserRepository;
import ge.ufc.webservices.repository.UserRepositoryImpl;
import ge.ufc.webservices.util.Utilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import java.sql.Connection;
import java.sql.SQLException;

@WebService
public class UserServiceWSImpl implements UserServiceWS {
    private Connection connection;
    private static final Logger lgg = LogManager.getLogger();
    @Resource
    WebServiceContext wsContext;

    @Override
    public User getUser(int user_id) throws UserNotFound, DatabaseException, AgentAccessDenied, AgentAuthFailed, InternalError {
        try {
            connection = DatabaseManager.getDatabaseConnection();
            UserRepository userRepository = new UserRepositoryImpl(connection);

            lgg.trace("Received user_id :" +user_id);
            int agent_id = Utilities.checkAgentID(wsContext.getMessageContext());

            if (!Utilities.checkAgentInfo(agent_id, wsContext)) {
                throw new DatabaseException("Exception");
            }

            User user1 = userRepository.check(user_id);

            lgg.trace("Response: "+user1);
            return user1;

        } finally {
            DatabaseManager.close(connection);
        }
    }

    @Override
    public int pay(String tr_id, int user_id, double amount) throws DuplicateFault, AmountNotPositive, UserNotFound, DatabaseException, AgentAccessDenied, AgentAuthFailed, TransactionNotFound, InternalError {
        try {
            connection = DatabaseManager.getDatabaseConnection();
            UserRepository user = new UserRepositoryImpl(connection);
            lgg.trace("Received arguments: agent_transaction_id {} , user_id {} , amount {} "+tr_id , user_id , amount);
            int agent_id = Utilities.checkAgentID(wsContext.getMessageContext());

            if (!Utilities.checkAgentInfo(agent_id, wsContext)) {
                throw new DatabaseException("Exception");
            }
            int sys_id = user.pay(agent_id, tr_id, user_id, amount);
            lgg.trace("Response : "+ sys_id);
            return sys_id;

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
            lgg.trace("Received transaction id : {}"+transaction_id);

            int agent_id = Utilities.checkAgentID(wsContext.getMessageContext());

            if (!Utilities.checkAgentInfo(agent_id, wsContext)) {
                throw new DatabaseException("Exception");
            }
            int status = user.status(transaction_id);
            lgg.trace("System transaction id : "+ status);
            return status;

        } finally {
            DatabaseManager.close(connection);
        }
    }

}
