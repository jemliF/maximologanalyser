package com.smartech.loganalyser.src.db;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import psdi.util.MXProperties;

public class MaximoParameteredConnection {

    static Connection connection = null;
    static String jdbcDriver = "";
    static String database = "";
    static String user = "";
    static String password = "";

    public MaximoParameteredConnection() {
        loadMaximoConnectionParameters();

        try {
            Class.forName(jdbcDriver).newInstance();
            connection = DriverManager.getConnection(database, user, password);
            if (connection != null) {
                System.out.println("connection succeeded");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadMaximoConnectionParameters() {
        java.util.Properties sourceProperties;
        try {
            sourceProperties = MXProperties.loadProperties(new FileInputStream(
                    new File("./maximo.properties")), false);
            jdbcDriver = sourceProperties.getProperty("mxe.db.driver");
            database = sourceProperties.getProperty("mxe.db.url");
            user = sourceProperties.getProperty("mxe.db.user");
            password = sourceProperties.getProperty("mxe.db.password");
            System.out.println("jdbcDriver: " + jdbcDriver);
            System.out.println("database: " + database);
            System.out.println("user: " + user);
            System.out.println("password: " + password);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Connection connecter() {
        try {
            if (connection == null) {
                new MaximoParameteredConnection();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    public ResultSet selectQuery(String command) {
        ResultSet resultSet = null;
        try {
            connection = connecter();
            resultSet = connection.createStatement().executeQuery(command);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultSet;
    }

    public boolean updateQuery(String command) {
        boolean result = false;
        try {
            connection = connecter();
            result = (connection.createStatement().executeUpdate(command) > 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (new File("./maximo.properties").exists()) {
            System.out.println("maximo.properties exists");
        }
        new MaximoParameteredConnection();
    }
}
