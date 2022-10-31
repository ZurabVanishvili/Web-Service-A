package ge.ufc.webservices.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ge.ufc.webservices.exceptions.DatabaseException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@SuppressWarnings("all")
public class DatabaseManager {
    private static final Logger logger = LogManager.getLogger();

    private static final String MOVIE_TOMCAT_DS = "java:comp/env/jdbc/personDs";

    public static Connection getDatabaseConnection() throws DatabaseException {
        return getConnection();
    }

    private static Connection getConnection() throws DatabaseException {
        try {
            DataSource ds = getDataSource(MOVIE_TOMCAT_DS);
            return ds.getConnection();
        } catch (NamingException e) {
            throw new DatabaseException("Unable to find datasource", e);
        } catch (SQLException e) {
            throw new DatabaseException("Unable to connect to the database", e);
        }
    }

    private static DataSource getDataSource(String jndiName) throws NamingException {
        Context initCtx = new InitialContext();
        return (DataSource) initCtx.lookup(jndiName);
    }

    public static void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.warn("Unable to close connection", e);
            }
        }
    }
}

