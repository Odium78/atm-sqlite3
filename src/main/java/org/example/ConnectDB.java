package org.example;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    private Connection connection;

    public boolean addClient(String username, int pin, double money) {
        String sql = "INSERT INTO clients(username, pin, money) VALUES(?, ?, ?)"; // add username pin money to table with values
        try (var pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, String.valueOf(pin));
            pstmt.setDouble(3, money); // should be zero
            pstmt.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("Error inserting client: " + e.getMessage());
            return false;
        }
    }

    public void transfer(String sender, String receiver, double money) {
        if (money <= 0) { System.out.println("Amount must be greater than 0."); }

        Double fromBalance = getBal(sender);
        Double toBalance = getBal(receiver);

        // error checks first
        if (fromBalance == null) { System.out.println("== Error! Sender account not found =="); }
        if (toBalance == null) { System.out.println("== Error! Receiver account not found =="); }
        if (fromBalance < money) { System.out.println("== Error! Insufficient funds =="); }

        String withdrawSql = "UPDATE clients SET money = money - ? WHERE username = ?";
        String depositSql = "UPDATE clients SET money = money + ? WHERE username = ?";

        try {
            connection.setAutoCommit(false); // dont update table content yet incase inconsistent
            try (var withdrawStmt = connection.prepareStatement(withdrawSql);
                 var depositStmt = connection.prepareStatement(depositSql)) {

                withdrawStmt.setDouble(1, money);
                withdrawStmt.setString(2, sender);
                withdrawStmt.executeUpdate();

                depositStmt.setDouble(1, money);
                depositStmt.setString(2, receiver);
                depositStmt.executeUpdate();

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                System.out.println("Error during transfer" + e.getMessage()); //safety
            } finally {
                connection.setAutoCommit(true); // restore default
            }
        } catch (SQLException e) {
            System.out.println("Transaction error: " + e.getMessage());
        }
    }

    public void deposit(String username, double amount) {
        String sql = "UPDATE clients SET money = money + ? WHERE username = ?";
        try (var pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
            } else {
            }
        } catch (Exception e) {
            System.out.println("Error during deposit: " + e.getMessage());
        }
    }

    public void withdraw(String username, double amount) {
        if (amount <= 0 && amount % 100 == 0) { System.out.println("Amount must be divisible by 100"); }
        Double balance = getBal(username);

        if (balance == null) { System.out.println("Client not found."); }
        if (balance < amount) { System.out.println("Insufficient funds. Current balance: " + balance); }
        String sql = "UPDATE clients SET money = money - ? WHERE username = ?";

        try (var pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected < 0) { System.out.println("No client found with username: " + username); }
        } catch (SQLException e) {
            System.out.println("Error with withdrawal: " + e.getMessage());
        }
    }

    public Double getBal(String username) {
        String sql = "SELECT money FROM clients WHERE username = ?";
        try (var pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            var rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("money");
            } else {
                return null;
            }
        } catch (Exception e) {
            System.out.println("Error retrieving balance: " + e.getMessage());
            return null;
        }
    }

    public boolean findClient(String username) {
        String sql = "SELECT username FROM clients WHERE username = ?";
        try (var pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            var rs = pstmt.executeQuery();
            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error looking up client: " + e.getMessage());
            return false;
        }
    }

    public boolean authClient(String username, String pin) {
        String sql = "SELECT * FROM clients WHERE username = ? AND pin = ?";
        try (var pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, pin);
            var rs = pstmt.executeQuery();
            if (rs.next()) {
                return true;
            } else {;
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error during authentication: " + e.getMessage());
            return false;
        }
    }

    public ConnectDB() {
        //System.out.println("Initializing Database..");
        String url = "jdbc:sqlite:clients.db";
        try {
            connection = DriverManager.getConnection(url);
            //System.out.println("Connection Successful");
        } catch (Exception e) {
            System.out.println("Error Database Connection: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null) {
                //System.out.println("Connection Closed");
                connection.close();
            }
        } catch (Exception e) {
            //e.printStackTrace(); lazy
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }



    // FOR DEBUGGING PURPOSES, DO NOT USE ON PRODUCT
    public void clrclnt() {
        String sql = "DELETE FROM clients";
        try (var stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("All clients removed.");
        } catch (SQLException e) {
            System.out.println("Error deleting clients: " + e.getMessage());
        }
    }

    public void mktbl() {
        String sql = "CREATE TABLE IF NOT EXISTS clients (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL UNIQUE," +
                "pin TEXT NOT NULL CHECK(length(pin) = 4 AND pin GLOB '[0-9]*')," +
                "money REAL DEFAULT 0" +
                ");";
        try (var stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table 'clients' created or already exists.");
        } catch (Exception e) {
            System.out.println("Error creating table: " + e.getMessage());
        }
    }
}