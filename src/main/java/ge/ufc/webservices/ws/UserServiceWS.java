package ge.ufc.webservices.ws;

import ge.ufc.webservices.exceptions.*;
import ge.ufc.webservices.exceptions.InternalError;
import ge.ufc.webservices.model.User;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.sql.SQLException;

@WebService
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT,use=SOAPBinding.Use.LITERAL,
parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
@SuppressWarnings("unused")
public interface UserServiceWS {

    @WebMethod(operationName = "getUser")
    @WebResult(name = "getUserResult")
    User getUser (@WebParam(name = "id") int user_id) throws UserNotFound, DatabaseException, AgentAccessDenied, SQLException, AgentAuthFailed;

    @WebMethod(operationName = "pay")
    @WebResult(name = "getPayResult")
    int pay(
            @WebParam(name = "transaction_id") String tr_id,
            @WebParam(name = "user_id")int user_id,
            @WebParam(name = "amount") double amount)
            throws DuplicateFault, AmountNotPositive, UserNotFound, DatabaseException, AgentAccessDenied, SQLException, AgentAuthFailed;

    @WebMethod(operationName = "status")
    @WebResult(name = "getStatus")
    int status(@WebParam(name = "transaction_id") String  transaction_id) throws AgentAccessDenied, SQLException, TransactionNotFound, AgentAuthFailed, InternalError, DatabaseException;
}
