package ge.ufc.webservices.util;

import ge.ufc.webservices.dao.DatabaseManager;
import ge.ufc.webservices.exceptions.AgentAccessDenied;
import ge.ufc.webservices.exceptions.AgentAuthFailed;
import ge.ufc.webservices.exceptions.DatabaseException;
import ge.ufc.webservices.repository.SQLQuery;

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

    public static int checkAgentID(MessageContext msg) {
        HttpServletRequest req = (HttpServletRequest) msg.get(MessageContext.SERVLET_REQUEST);
        return Integer.parseInt(req.getHeader("agent_id"));
    }

    public static boolean checkAgentPassword(MessageContext msg, String rightPassword) throws AgentAuthFailed {
        HttpServletRequest req = (HttpServletRequest) msg.get(MessageContext.SERVLET_REQUEST);
        Base64.Decoder decoder = Base64.getDecoder();
        String dStr = new String(decoder.decode(req.getHeader("password")));

        if (!dStr.equals(rightPassword)) {
            throw new AgentAuthFailed();
        }
        return true;

    }

    public static boolean checkAgentInfo(Connection connection, int agent_id, WebServiceContext wsContext) throws AgentAccessDenied, AgentAuthFailed, DatabaseException {
        HttpServletRequest req = (HttpServletRequest) wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
        try (PreparedStatement ps = connection.prepareStatement(SQLQuery.CheckPasswd.query)) {

            connection = DatabaseManager.getDatabaseConnection();
            ps.setInt(1, agent_id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new AgentAuthFailed();
                } else {
                    //check agent password
                    if (Utilities.checkAgentPassword(wsContext.getMessageContext(), rs.getString("password"))) {
                        try (PreparedStatement ps2 = connection.prepareStatement(SQLQuery.CheckIP.query)) {
                            ps2.setInt(1, agent_id);
                            try (ResultSet rs2 = ps2.executeQuery()) {
                                if (rs2.next()) {
                                    //check agent IP
                                    if (!Objects.equals(rs2.getString("allowed_ip"), req.getRemoteAddr()))
                                        throw new AgentAccessDenied();

                                }

                            }
                        }

                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Exception occurred");
        }
        return true;

    }
}
