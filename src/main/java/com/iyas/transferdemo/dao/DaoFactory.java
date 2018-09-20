package com.iyas.transferdemo.dao;

import com.iyas.transferdemo.common.Utils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.h2.tools.RunScript;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DaoFactory {
    private static final String h2_driver = Utils.getStringProperty("h2_driver");
    private static final String h2_connection_url = Utils.getStringProperty("h2_connection_url");
    private static final String h2_user = Utils.getStringProperty("h2_user");
    private static final String h2_password = Utils.getStringProperty("h2_password");
    private static Logger log = Logger.getLogger(DaoFactory.class);

    private final AccountDao accountDAO = new AccountDao();

    public DaoFactory() {
        DbUtils.loadDriver(h2_driver);
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(h2_connection_url, h2_user, h2_password);

    }

    public AccountDao getAccountDAO() {
        return accountDAO;
    }


    public void populateTestData() {
        Connection conn = null;
        try {
            conn = DaoFactory.getConnection();
            RunScript.execute(conn, new FileReader("src/test/resources/demo.sql"));
        } catch (SQLException e) {
            log.error("populateTestData: Error populating test data: ", e);
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            log.error("populateTestData: Error finding test data file ", e);
            throw new RuntimeException(e);
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

}