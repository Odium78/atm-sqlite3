package org.atm;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Database db = new Database();
        Scanner scanner = new Scanner(System.in);

        boolean isActive = true;

        while (isActive) { // main welcome / login page
            System.out.println();
            System.out.println("Welcome to ReliableBankingSystem!");
            System.out.println("[1] Login");
            System.out.println("[2] Register");
            System.out.println("[3] Exit");
            System.out.print("Select an option (1-3): ");

            int prmpt = readInt(scanner);

            switch (prmpt) {
                case 1: // login
                    System.out.println();
                    System.out.println("ReliableBankingSystem | Login");
                    System.out.print("Input Account Name: ");
                    String accountName = readNonEmptyLine(scanner).toLowerCase();

                    if (db.findClient(accountName)) {
                        boolean loggedIn = isAllowed(db, scanner, accountName);
                        if (loggedIn) {
                            // Main ATM Program (user menu)
                            while (loggedIn) {
                                int choice = loginUser(scanner, accountName);
                                switch (choice) {
                                    case 1: // withdrawal
                                        withdrawal(scanner, accountName, db);
                                        loggedIn = readYesNo(scanner);
                                        break;
                                    case 2: // deposit
                                        deposit(scanner, accountName, db);
                                        loggedIn = readYesNo(scanner);
                                        break;
                                    case 3: // check balance
                                        checkBalance(accountName, db);
                                        loggedIn = readYesNo(scanner);
                                        break;
                                    case 4: // transfer
                                        transferMoney(scanner, accountName, db);
                                        loggedIn = readYesNo(scanner);
                                        break;
                                    case 5: // delete account
                                        loggedIn = deleteAccount(scanner, db, accountName);
                                        break;
                                    case 6: // exit
                                        loggedIn = false; // exit the logged-in loop
                                        break;
                                    default:
                                        System.out.println("Invalid Choice!");
                                        break;
                                }
                            }
                        } else {
                            System.out.println("Too many failed attempts. Access denied.");
                        }
                    } else {
                        System.out.println("No Account Found with " + accountName + "!");
                    }
                    break;

                case 2: // register
                    registerUser(scanner, db);
                    break;

                case 3: // exit program
                    System.out.println();
                    System.out.println("Thank you for using ReliableBankingSystem!");
                    isActive = false;
                    break;

                default:
                    System.out.println("Invalid input!");
                    break;
            }
        }

        scanner.close();
        db.closeConnection();
    }

    // input parsers
    private static String readNonEmptyLine(Scanner scanner) {
        String line;
        do {
            line = scanner.nextLine();
        } while (line == null || line.trim().isEmpty());
        return line.trim();
    }

    private static int readInt(Scanner scanner) {
        String line = scanner.nextLine();
        try {
            return Integer.parseInt(line.trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private static double readDouble(Scanner scanner) {
        String line = scanner.nextLine();
        try {
            return Double.parseDouble(line.trim());
        } catch (Exception e) {
            // signal invalid with NaN
            return Double.NaN;
        }
    }

    private static boolean readYesNo(Scanner scanner) {
        while (true) {
            System.out.print("\nDo you want another transaction? [Y][N]: ");
            String line = scanner.nextLine();
            if (line == null || line.trim().isEmpty()) {
                System.out.println("Invalid Option!");
                continue;
            }
            char c = Character.toLowerCase(line.trim().charAt(0));
            if (c == 'y') return true;
            if (c == 'n') return false;
            System.out.println("Invalid Option!");
        }
    }

    // pin tries
    private static boolean isAllowed(Database db, Scanner scanner, String accountName) {
        for (int i = 1; i <= 3; i++) {
            System.out.print("Input Pin (Max 3 Tries): ");
            int accPin = readInt(scanner);
            if (accPin == -1) {
                System.out.println("\nWrong pin, try again! (" + i + "x)");
                continue;
            }
            if (!db.authClient(accountName, String.valueOf(accPin))) {
                System.out.println("\nWrong pin, try again! (" + i + "x)");
            } else {
                return true;
            }
        }
        return false;
    }

    // atm functions
    private static void withdrawal(Scanner scanner, String accountName, Database db) {
        System.out.println();
        System.out.println("Withdrawal (must be divisible by 100)");
        System.out.print("Enter Amount: ");
        double amount = readDouble(scanner);

        if (amount >= 50000) {
            System.out.println("Basic Accounts Cannot Deposit More Than 50,000!");
            return;
        }

        if (Double.isNaN(amount)) {
            System.out.println("Invalid Amount!");
            return;
        }
        if (amount <= 0 || amount % 100 != 0) {
            System.out.println("Amount must be greater than 0 and divisible by 100!");
            return;
        }
        // Confirm pin again (3 tries)
        if (isAllowed(db, scanner, accountName)) {
            if (db.withdraw(accountName, amount)) {
                System.out.printf("You have successfully withdrawn amount of: %.2f%n", amount);
                System.out.printf("Your new balance: P%.2f%n", db.getBal(accountName));
            } else {
                System.out.println("Balance Error! Withdraw Failed, no changes made");
            }
        } else {
            System.out.println("Too many failed attempts. Withdrawal Denied.");
        }
    }

    private static void deposit(Scanner scanner, String accountName, Database db) {
        System.out.println();
        System.out.println("Deposit");
        System.out.print("Enter Amount: ");
        double amount = readDouble(scanner);

        if (amount >= 50000) {
            System.out.println("Basic Accounts Cannot Deposit More Than 50,000!");
            return;
        }

        if (Double.isNaN(amount)) {
            System.out.println("Invalid Amount!");
            return;
        }
        if (amount <= 0) {
            System.out.println("Amount must be greater than 0!");
            return;
        }
        if (isAllowed(db, scanner, accountName)) {
            if (db.deposit(accountName, amount)) {
                System.out.printf("You have successfully deposited amount of: %.2f%n", amount);
                System.out.printf("Your new balance: P%.2f%n", db.getBal(accountName));
            } else {
                System.out.println("Deposit Denied, no Changes Made.");
            }
        } else {
            System.out.println("Too many failed attempts. Deposit Denied.");
        }
    }

    private static void checkBalance(String accountName, Database db) {
        System.out.println();
        System.out.printf("Your Current Balance Is: P%.2f%n", db.getBal(accountName));
    }

    private static void transferMoney(Scanner scanner, String accountName, Database db) {
        System.out.println();
        System.out.print("Input Receiver Name: ");
        String receiverName = readNonEmptyLine(scanner);
        System.out.print("Input Amount: ");

        double amount = readDouble(scanner);

        if (amount >= 50000) {
            System.out.println("Basic Accounts Cannot Deposit More Than 50,000!");
            return;
        }
        if (Double.isNaN(amount) || amount <= 0) {
            System.out.println("Invalid Amount!");
            return;
        }
        if (isAllowed(db, scanner, accountName)) {
            if (db.transfer(accountName, receiverName, amount)) {
                System.out.printf("You have successfully transferred amount of: %.2f onto %s's wallet.%n", amount, receiverName);
                System.out.printf("Your new balance: P%.2f%n", db.getBal(accountName));
            } else {
                System.out.println("Transfer denied, no changes made.");
            }
        } else {
            System.out.println("Too many failed attempts. Transfer Denied.");
        }
    }

    private static boolean deleteAccount(Scanner scanner, Database db, String currentAccountName) {
        System.out.println();
        System.out.println("Are you sure you want to delete your RBS Account?");
        System.out.print("Input account name to continue: ");
        String usr = readNonEmptyLine(scanner);
        if (!usr.equalsIgnoreCase(currentAccountName)) {
            System.out.println("Account name mismatch. Delete Cancelled.");
            return true; // stay logged in
        }
        if (db.deleteClient(usr)) {
            System.out.println("Thanks for using RBS! Happy to Serve you!");
            return false; // logged out
        } else {
            System.out.println("Delete Failed! No Changes Made.");
            return true; // stay logged in
        }
    }

    // login & register menu
    private static void registerUser(Scanner scanner, Database db) {
        System.out.println();
        System.out.println("ReliableBankingSystem | Register");
        System.out.print("Desired username: ");
        String username = readNonEmptyLine(scanner).toLowerCase();
        System.out.print("Desired pin (4 digits): ");
        int pin = readInt(scanner);
        while (!(pin >= 1000 && pin <= 9999)) {
            System.out.println("Invalid PIN. Must be exactly 4 digits.");
            System.out.print("Desired pin: ");
            pin = readInt(scanner);
        }
        if (db.addClient(username.toLowerCase(), pin, 0.0)) {
            System.out.println("Register Complete! Thank you for Joining!");
        } else {
            System.out.println("Failed to register. No user created.");
        }
    }

    private static int loginUser(Scanner scanner, String accountName) {
        System.out.println();
        System.out.println("Welcome To ReliableBankingSystem | Hi, " + accountName + "!");
        System.out.println("[1] Withdrawal");
        System.out.println("[2] Deposit");
        System.out.println("[3] Balance");
        System.out.println("[4] Transfer");
        System.out.println("[5] Delete Account");
        System.out.println("[6] Exit");
        System.out.print("Select an option (1-6): ");
        int choice = readInt(scanner);
        return choice;
    }
}