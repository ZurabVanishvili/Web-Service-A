package ge.ufc.webservices.repository;

public enum SQLQuery {

    InsertIntoTransactions(" insert into transactions(agent_id,agent_transaction_id,user_id,amount,transaction_date)values(?,?,?,?,?)"),
    SelectUserById("select * from users where id = ?"),
    SelectBalanceFromUserByID(" select balance from users where id = ?"),
    UpdateUserBalance("update users set balance = ? where id=?"),
    SelectFromUsers("select * from users where id = ?"),
    ReturnSysTransID("select user_id,amount from transactions where system_transaction_id = ?"),
    ReturnIDForStatus("select system_transaction_id from transactions where agent_transaction_id = ?"),
    CheckPasswd("select password from agents where id = ?"),
    CheckIP("select allowed_ip from agent_access inner join agents on agent_access.agent_id = agents.id where agents.id = ?");


    public final String query;

    SQLQuery(String s) {
        this.query = s;
    }
}
