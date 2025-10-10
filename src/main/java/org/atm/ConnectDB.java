package org.atm;
import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectDB {
    private Connection connection;
    private String url;

    public ConnectDB(String url) {
        //System.out.println("Initializing Database..");
        this.url = url;
        try {
            connection = DriverManager.getConnection(url);
            //System.out.println("Connection Successful");
        } catch (Exception e) {
            System.out.println("Error Database Connection: " + e.getMessage());
        }
    }

    public Connection getConnection() { return connection; }
}