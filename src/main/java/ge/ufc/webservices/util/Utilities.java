package ge.ufc.webservices.util;

import ge.ufc.webservices.dao.DatabaseManager;
import ge.ufc.webservices.exceptions.AgentAccessDenied;
import ge.ufc.webservices.exceptions.AgentAuthFailed;
import ge.ufc.webservices.exceptions.DatabaseException;
import ge.ufc.webservices.repository.SQLQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Objects;

public class Utilities {
    private static final Logger lgg = LogManager.getLogger();

    public static int checkAgentID(MessageContext msg) {
        HttpServletRequest req = (HttpServletRequest) msg.get(MessageContext.SERVLET_REQUEST);
        return Integer.parseInt(req.getHeader("agent_id"));
    }

    public static boolean checkAgentPassword(MessageContext msg, String rightPassword) throws AgentAuthFailed {
        // ვიღებ პაროლის ჰედერს base64-ში, ვუკეთებ დეკოდს და ამის შემდეგ უკვე ვადარებ ემთხვევა თუ არა შესაბამისი აგენტის პაროლს.
        HttpServletRequest req = (HttpServletRequest) msg.get(MessageContext.SERVLET_REQUEST);
        Base64.Decoder decoder = Base64.getDecoder();
        String decoded = new String(decoder.decode(req.getHeader("password")));
        lgg.trace("Request header password : " + decoded);
        if (!decoded.equals(rightPassword)) {
            lgg.error(new AgentAuthFailed());
            throw new AgentAuthFailed();
        }
        return true;

    }

    public static boolean checkAgentInfo(int agent_id, WebServiceContext wsContext) throws AgentAccessDenied, AgentAuthFailed, DatabaseException {
        HttpServletRequest req = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
        Connection connection = DatabaseManager.getDatabaseConnection();

        try (PreparedStatement ps = connection.prepareStatement(SQLQuery.CheckPasswd.query);
             PreparedStatement ps2 = connection.prepareStatement(SQLQuery.CheckIP.query)) {
            //agents ცხრილიდან მომაქვს პაროლი ჩაწოდებული agent_id-ს შესაბამისი , რომელსაც შემდეგ 56 ხაზზე ვამოწმებ.
            ps.setInt(1, agent_id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new AgentAuthFailed();
                } else {
                    //check agent password
                    if (Utilities.checkAgentPassword(wsContext.getMessageContext(), rs.getString("password"))) {
                        //თუ id და password ემთხვევა ერთმანეთს დაგვრჩება IP,რომელიც agent_access ცხრილიდან იმავე agent_id-თ მომაქვს და ვამოწმებ
                        // ემთხვევა თუ არა req.getRemoteAddr()-ს.
                        ps2.setInt(1, agent_id);
                        try (ResultSet rs2 = ps2.executeQuery()) {
                            if (rs2.next()) {
                                //check agent IP
                                if (!Objects.equals(rs2.getString("allowed_ip"), req.getRemoteAddr()))
                                    throw new AgentAccessDenied();

                            }
                        //თუ ყველა ეს შემოწმება გაიარა ვაბრუნებ true-ს.

                        }
                    }
                }
            }
        } catch (SQLException e) {
            lgg.fatal(e);
            throw new DatabaseException("Exception occurred");
        }
        return true;

    }
}
