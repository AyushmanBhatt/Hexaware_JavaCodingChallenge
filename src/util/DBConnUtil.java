/*
package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnUtil {
    public static Connection getDBConn(String connStr) throws SQLException {
        String[] parts = connStr.split(",");
        String url = parts[0];
        String username = parts[1];
        String password = parts[2];

        Connection conn = DriverManager.getConnection(url, username, password);
        return conn;
    }
}
*/
package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnUtil {

    public static Connection getDBConn(String connStr) throws SQLException {
        String[] parts = connStr.split(",");
        String url = parts[0];
        String username = parts[1];
        String password = parts[2];

        Connection conn = DriverManager.getConnection(url, username, password);
        return conn;
    }
}
