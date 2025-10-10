//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

// Compiled via JDK 21 Temurin LTS
package org.atm;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // database init
        Database db = new Database();

        // scanner init
        Scanner scanner = new Scanner(System.in);

        //init required data
        boolean isActive = true;
        boolean allowed = false;
        int prmpt; // prompt for welcome page
        int logprmpt = 0; // prompt number for withdraw deposit balance transfer exit
        char trnprmpt = 'n'; // prompt for yes or no [y][n]
        double amount = 0; // flexible transaction amount

        while (isActive) { // login page
            System.out.println();
            System.out.println("Welcome to ReliableBankingSystem!");
            System.out.println("[1] Login");
            System.out.println("[2] Register");
            System.out.println("[3] Exit");
            System.out.print("Select an option (1-3): ");

            try { // check for invalid input
                prmpt = scanner.nextInt();
            } catch (InputMismatchException e) {
                prmpt = 0;
            }

            switch(prmpt) {
                case 1: // login
                    scanner.nextLine();
                    System.out.println();
                    System.out.println("ReliableBankingSystem | Login");
                    System.out.print("Input Account Name: ");
                    String accountNameDef = scanner.nextLine();
                    String accountName = accountNameDef.toLowerCase();

                    if (db.findClient(accountName)) {
                        boolean loggedIn = false;
                        loggedIn = isAllowed(db, scanner, accountName, loggedIn);
                        if (loggedIn) {
                            // Main ATM Program
                            while (loggedIn) {
                                scanner.nextLine();
                                switch (loginUser(scanner, logprmpt, accountName)) {
                                    case 1: //withdrawal
                                        withdrawal(scanner, allowed, loggedIn, accountName, amount, trnprmpt, db);
                                        loggedIn = prompt(scanner, trnprmpt);
                                        break;
                                    case 2: //deposit
                                        deposit(scanner, allowed, loggedIn, accountName, amount, trnprmpt, db);
                                        loggedIn = prompt(scanner, trnprmpt);
                                        break;
                                    case 3: // check bal
                                        checkBalance(scanner, loggedIn, accountName, trnprmpt, db);
                                        loggedIn = prompt(scanner, trnprmpt);
                                        break;
                                    case 4: //transfer
                                        transferMoney(scanner, allowed, loggedIn, accountName, amount, trnprmpt, db);
                                        loggedIn = prompt(scanner, trnprmpt);
                                        break;
                                    case 5: // delete account
                                        deleteAccount(scanner, db);
                                        break;
                                    case 6: // exit
                                        loggedIn = false;
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
                case 3: // exit
                    scanner.nextLine();
                    System.out.println();
                    System.out.println("Thank you for using ReliableBankingSystem!");
                    isActive = false;
                    break;
                default:
                    scanner.nextLine();
                    System.out.println("Invalid input!");
                    break;
            }
        }

        scanner.close();
        db.closeConnection();
    }

    // yes or no prompt
    private static boolean prompt(Scanner scanner, char trnprmpt) {
        while (true){
            System.out.print("\nDo you want another transaction? [Y][N]: ");
            trnprmpt = scanner.next().charAt(0);
            if  (Character.toLowerCase(trnprmpt) == 'y') { return true; }
            else if (Character.toLowerCase(trnprmpt) == 'n') { return false; }
            else { System.out.println("Invalid Option!"); }
        }
    }

    // since the pin is repeated make it a function for cleaner code
    private static boolean isAllowed(Database db, Scanner scanner, String accountName, boolean allowed) {
        int accPin;

        for (int i = 1; i <= 3; i++) {
            System.out.print("Input Pin (Max 3 Tries): ");

            try { // check for invalid input
                accPin = scanner.nextInt();
            } catch (InputMismatchException e) {
                scanner.nextLine();
                accPin = 0;
            }

            if (!db.authClient(accountName, String.valueOf(accPin))) {
                System.out.println("\nWrong pin, try again! " + "(" + i + "x" + ")");
            } else {
                allowed = true;
                break;
            }
        }
        return allowed;
    }

    // atm functions require every "init required data" variables in order to work
    private static void withdrawal(Scanner scanner, boolean allowed, boolean loggedIn, String accountName, double amount, char trnprmpt, Database db) {
        System.out.println();
        System.out.println("Withdrawal (must be divisible by 100)");
        System.out.print("Enter Amount: ");

        try { // check for invalid input
            amount = scanner.nextDouble();
        } catch (InputMismatchException e) {
            System.out.println("Invalid Amount!");
            scanner.nextLine();
            return;
        }

        if (amount <= 0 || amount % 100 != 0) {
            System.out.println("Amount must be greater than 0 and divisible by 100!");
            return;
        }

        allowed = false;
        allowed = isAllowed(db, scanner, accountName, allowed);
        if (allowed) {
            if (db.withdraw(accountName, amount)) {
                System.out.println("You have successfully withdrawn amount of: " + amount);
                System.out.println("Your new balance: P" + db.getBal(accountName));
            } else { System.out.println("Balance Error! Withdraw Failed, no changes made"); }
        }else { System.out.println("Too many failed attempts. Withdrawal Denied."); }
    }


    private static void deposit(Scanner scanner, boolean allowed, boolean loggedIn, String accountName, double amount, char trnprmpt, Database db) {
        System.out.println();
        System.out.println("Deposit");
        System.out.print("Enter Amount: ");

        try { // check for invalid input
            amount = scanner.nextDouble();
        } catch (InputMismatchException e) {
            System.out.println("Invalid Amount!");
            return;
        }

        allowed = false;
        allowed = isAllowed(db, scanner, accountName, allowed);

        if (allowed) {
            if (db.deposit(accountName, amount)) {
                System.out.println("You have successfully deposited amount of: " + amount);
                System.out.println("Your new balance: " + db.getBal(accountName));
            } else { System.out.println("Deposit Denied, no Changes Made."); }
        } else { System.out.println("Too many failed attempts. Deposit Denied."); }
    }

    private static void checkBalance(Scanner scanner, boolean loggedIn, String accountName, char trnprmpt, Database db) {
        System.out.println();
        System.out.println("Your Current Balance Is: P" + db.getBal(accountName));
    }

    private static void transferMoney(Scanner scanner, boolean allowed, boolean loggedIn, String accountName, double amount, char trnprmpt, Database db) {
        scanner.nextLine();
        System.out.println();
        System.out.print("Input Receiver Name: ");
        String receiverName = scanner.nextLine();
        System.out.print("Input Amount: ");

        try { // check for invalid input
            amount = scanner.nextDouble();
        } catch (InputMismatchException e) {
            System.out.println("Invalid Amount!");
            scanner.nextLine();
            return;
        }

        allowed = false;
        allowed = isAllowed(db, scanner, accountName, allowed);
        if (allowed) {
            if (db.transfer(accountName, receiverName, amount)) {
                System.out.println("You Have Successfully transferred amount of: " +
                        amount +
                        " Onto " +
                        receiverName + "'s Wallet.");
                System.out.println("Your new balance: P" + db.getBal(accountName));
            } else { System.out.println("Transfer denied, no changes made."); }
        }else { System.out.println("Too many failed attempts. Transfer Denied."); }
    }

    private static void deleteAccount(Scanner scanner, Database db) {
        System.out.println();
        System.out.println("Are you sure you want to delete your RBS Account?");
        System.out.print("Input account name to continue: ");
        String usr =  scanner.nextLine();
        if (db.deleteClient(usr)){
            System.out.println("Thanks for using RBS! Happy to Serve you!");
        } else { System.out.println("Delete Failed! No Changes Made."); }
    }

    // for regsitering users
    private static void registerUser(Scanner scanner, Database db) {
        scanner.nextLine();
        System.out.println();
        System.out.println("ReliableBankingSystem | Register");
        System.out.print("Desired username: ");
        String username = scanner.next();
        System.out.print("Desired pin: ");
        int pin = scanner.nextInt();
        while (!(pin >= 1000 && pin <= 9999)) // check if 4 digits
        {
            System.out.println("Invalid PIN. Must be exactly 4 digits.");
            System.out.print("Desired pin: ");
            pin = scanner.nextInt();
        }
        if (db.addClient(username.toLowerCase(), pin, 0.0)) {
            System.out.println("Register Complete! Thank you for Joining!");
        } else {
            System.out.println("Failed to register. No user created.");
        }
    }

    // login page
    private static int loginUser(Scanner scanner, int logprmpt, String accountName) {
        System.out.println();
        System.out.println("Welcome To ReliableBankingSystem | Hi, " + accountName + "!");
        System.out.println("[1] Withdrawal");
        System.out.println("[2] Deposit");
        System.out.println("[3] Balance");
        System.out.println("[4] Transfer");
        System.out.println("[5] Delete Account");
        System.out.println("[6] Exit");
        System.out.print("Select an option (1-6): ");

        try { // check for invalid input
            return logprmpt = scanner.nextInt();
        } catch (InputMismatchException e) {
            return logprmpt = 0;
        }
    }
}