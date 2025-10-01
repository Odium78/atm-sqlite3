package org.example;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Database {
    ConnectDB connectDB;
    Connection connection;

    public Database() {
        this.connectDB = new ConnectDB("jdbc:sqlite:clients.db");
        this.connection = connectDB.getConnection();
    }

    public boolean addClient(String username, int pin, double money) {
        String sql = "INSERT INTO clients(username, pin, money) VALUES(?, ?, ?)"; // add username pin money to table with values
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, String.valueOf(pin));
            pstmt.setDouble(3, money); // should be zero
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getErrorCode() == 19) { System.out.println("Username \"" + username + "\" already exists!"); }
            else { System.out.println("Error inserting client: " + e.getMessage()); }
            return false;
        }
    }

    public boolean deleteClient(String username) {
        String sql = "DELETE FROM clients WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) { // is username exist?
                System.out.println("Client '" + username + "' deleted successfully.");
                return true;
            } else {
                System.out.println("No client found with username: " + username);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error deleting client: " + e.getMessage());
            return false;
        }
    }

    public boolean findClient(String username) {
        String sql = "SELECT username FROM clients WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
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
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
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

    // money handling
    public boolean transfer(String fromUser, String toUser, double amount) {
        String withdrawSql = "UPDATE clients SET money = money - ? WHERE username = ? AND money >= ?";
        String depositSql = "UPDATE clients SET money = money + ? WHERE username = ?";

        try {
            connection.setAutoCommit(false); // start transaction

            // Withdraw from sender
            try (PreparedStatement withdrawStmt = connection.prepareStatement(withdrawSql)) {
                withdrawStmt.setDouble(1, amount);
                withdrawStmt.setString(2, fromUser);
                withdrawStmt.setDouble(3, amount);
                int rowsWithdrawn = withdrawStmt.executeUpdate();
                if (rowsWithdrawn == 0) {
                    connection.rollback();
                    System.out.println("Insufficient Balance!");
                    return false; // not enough funds
                }
            }

            // Deposit to recipient
            try (PreparedStatement depositStmt = connection.prepareStatement(depositSql)) {
                depositStmt.setDouble(1, amount);
                depositStmt.setString(2, toUser);
                int rowsDeposited = depositStmt.executeUpdate();
                if (rowsDeposited == 0) {
                    connection.rollback();
                    System.out.println("Receiver with name \"" + toUser + "\" does not exist!");
                    return false; // recipient not found
                }
            }

            connection.commit();
            return true;

        } catch (Exception e) {
            System.out.println("Error during transfer: " + e.getMessage());
            try { connection.rollback(); } catch (Exception ignored) {}
            return false;
        } finally {
            try { connection.setAutoCommit(true); } catch (Exception ignored) {} // roll back set commits to true again
        }
    }

    public boolean deposit(String username, double amount) {
        String sql = "UPDATE clients SET money = money + ? WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // return true if deposit worked
        } catch (Exception e) {
            System.out.println("Error during deposit: " + e.getMessage());
            return false;
        }
    }

    public boolean withdraw(String username, double amount) {
        String sql = "UPDATE clients SET money = money - ? WHERE username = ? AND money >= ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setString(2, username);
            pstmt.setDouble(3, amount);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) { return true; }
            else {
                System.out.println("Insufficient Balance");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error during withdrawal: " + e.getMessage());
            return false;
        }
    }

    public Double getBal(String username) {
        String sql = "SELECT money FROM clients WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            var rs = pstmt.executeQuery();
            if (rs.next()) { return rs.getDouble("money"); } else { return null; }
        } catch (Exception e) {
            System.out.println("Error retrieving balance: " + e.getMessage());
            return null;
        }
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
}
