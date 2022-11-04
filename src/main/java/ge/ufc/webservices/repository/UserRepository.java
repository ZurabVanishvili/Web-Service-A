package ge.ufc.webservices.repository;

import ge.ufc.webservices.exceptions.*;
import ge.ufc.webservices.exceptions.InternalError;
import ge.ufc.webservices.model.User;

import java.sql.SQLException;

public interface UserRepository {

    User check(int user_id) throws UserNotFound, DatabaseException, InternalError;

    int pay(int agent_id, String transaction_id, int user_id, double amount) throws DuplicateFault, UserNotFound, AmountNotPositive, DatabaseException, SQLException, TransactionNotFound, InternalError;

    int status(String transaction_id) throws TransactionNotFound, InternalError;

}
