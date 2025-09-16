//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

// Compiled via JDK 21 Temurin LTS

package org.example;
import java.sql.Connection;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        //init required data
        boolean isActive = true;
        ConnectDB connectDB = new ConnectDB();
        Connection connection = connectDB.getConnection();
        Scanner scanner = new Scanner(System.in);
        int prmpt; // prompt for welcome page

        while (isActive) { //welcome/login page
            System.out.println();
            System.out.println("Welcome to ReliableBankingSystem!");
            System.out.println("[1] Login");
            System.out.println("[2] Register");
            System.out.println("[3] Exit");
            System.out.print("Select an option: ");
            prmpt = scanner.nextInt();

            switch(prmpt) {
                case 1:
                    System.out.println();
                    System.out.println("ReliableBankingSystem | Login");
                    System.out.print("Input Account Name: ");
                    String accountName = scanner.next();
                    if (connectDB.findClient(accountName)) {
                        boolean loggedIn = false;
                        loggedIn = isAllowed(connectDB, scanner, accountName, loggedIn);
                        if (loggedIn) {
                            // Main ATM Program
                            while (loggedIn) {
                                System.out.println();
                                System.out.println("Welcome To ReliableBankingSystem | Hi, " + accountName + "!");
                                System.out.println("[1] Withdrawal");
                                System.out.println("[2] Deposit");
                                System.out.println("[3] Balance");
                                System.out.println("[4] Transfer");
                                System.out.println("[5] Exit");
                                System.out.print("Select an option: ");
                                int logprmpt = scanner.nextInt(); // prompt number for withdraw deposit balance transfer exit
                                char trnprmpt; // prompt for yes or no [y][n]

                                switch (logprmpt) {
                                    case 1:
                                        System.out.println();
                                        System.out.println("Withdrawal (must be divisible by 100)");
                                        System.out.print("Enter Amount: ");
                                        double amount = scanner.nextDouble();

                                        if (amount <= 0 || amount % 100 != 0) {
                                            System.out.println("Amount must be greater than 0 and divisible by 100!");
                                            break;
                                        }

                                        boolean allowed = false;
                                        allowed = isAllowed(connectDB, scanner, accountName, allowed);
                                        if (allowed) {
                                            connectDB.withdraw(accountName, amount);
                                            System.out.println("You have successfully withdrawn amount of: " + amount);
                                            System.out.println("Your new balance: " + connectDB.getBal(accountName));
                                            System.out.print("Do you want another transaction? [Y][N]: ");
                                            trnprmpt = scanner.next().charAt(0);
                                            loggedIn = trnprmpt == 'y' || trnprmpt == 'Y';
                                        }else {
                                            System.out.println("Too many failed attempts. Withdrawal Denied.");
                                        }
                                        break;
                                    case 2:
                                        System.out.println();
                                        System.out.println("Deposit");
                                        System.out.print("Enter Amount: ");
                                        amount = scanner.nextDouble();
                                        allowed = false;
                                        allowed = isAllowed(connectDB, scanner, accountName, allowed);

                                        if (allowed) {
                                            connectDB.deposit(accountName, amount);
                                            System.out.println("You have successfully deposited amount of: " + amount);
                                            System.out.println("Your new balance: " + connectDB.getBal(accountName));
                                            System.out.print("Do you want another transaction? [Y][N]: ");
                                            trnprmpt = scanner.next().charAt(0);
                                            loggedIn = trnprmpt == 'y' || trnprmpt == 'Y';
                                        }else {
                                            System.out.println("Too many failed attempts. Deposit Denied.");
                                        }
                                        break;
                                    case 3:
                                        System.out.println();
                                        System.out.println("Your Current Balance Is: " + connectDB.getBal(accountName));
                                        System.out.print("Do you want another transaction? [Y][N]: ");
                                        trnprmpt = scanner.next().charAt(0);
                                        loggedIn = trnprmpt == 'y' || trnprmpt == 'Y';
                                        break;
                                    case 4:
                                        System.out.println();
                                        System.out.print("Input Receiver Name: ");
                                        String receiverName = scanner.next();
                                        System.out.print("Input Amount: ");
                                        amount = scanner.nextInt();
                                        allowed = false;
                                        allowed = isAllowed(connectDB, scanner, accountName, allowed);
                                        if (allowed) {
                                            connectDB.transfer(accountName, receiverName, amount);
                                            System.out.println("You Have Successfully transferred amount of: " +
                                                    amount +
                                                    " Onto " +
                                                    receiverName + "'s Wallet.");
                                            System.out.println("Your new balance: " + connectDB.getBal(accountName));
                                            System.out.print("Do you want another transaction? [Y][N]: ");
                                            trnprmpt = scanner.next().charAt(0);
                                            loggedIn = trnprmpt == 'y' || trnprmpt == 'Y';
                                        }else {
                                            System.out.println("Too many failed attempts. Transfer Denied.");
                                        }
                                        break;
                                    case 5:
                                        loggedIn = false;
                                        break;
                                }
                            }
                        } else {
                            System.out.println("Too many failed attempts. Access denied.");
                        }
                    } else {
                        System.out.println("== No Account Found! ==");
                    }
                    break;
                case 2:
                    System.out.println();
                    System.out.println("ReliableBankingSystem | Register");
                    System.out.print("Desired username: ");
                    String username = scanner.next();
                    System.out.print("Desired pin: ");
                    int pin = scanner.nextInt();
                    while (!(pin >= 1000 && pin <= 9999))
                    {
                        System.out.println("== Invalid PIN. Must be exactly 4 digits. ==");
                        System.out.print("Desired pin: ");
                        pin = scanner.nextInt();
                    }
                    if (connectDB.addClient(username, pin, 0.0)) {
                        System.out.println("Register Complete! Thank you for Joining!");
                    } else {
                        System.out.println("Failed to register :(");
                    }
                    break;
                case 3:
                    System.out.println();
                    System.out.println("Thank you for using ReliableBankingSystem!");
                    isActive = false;
                    break;
                default:
                    System.out.println("== Invalid input! ==");
                    break;
            }
        }

        scanner.close();
        connectDB.closeConnection();
    }

    
    // since the pin is repeated make it a function for cleaner code
    private static boolean isAllowed(ConnectDB connectDB, Scanner scanner, String accountName, boolean allowed) {
        for (int i = 1; i <= 3; i++) {
            System.out.print("Input Pin: ");
            int accPin = scanner.nextInt();
            if (!connectDB.authClient(accountName, String.valueOf(accPin))) {
                System.out.println("== Wrong pin, try again! ==");
            } else {
                allowed = true;
                break;
            }
        }
        return allowed;
    }
}