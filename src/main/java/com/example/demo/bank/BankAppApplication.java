package com.example.demo.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class BankAppApplication {

    private final List<BankAccount> accounts = new ArrayList<>();

    public static void main(String[] args) {
        SpringApplication.run(BankAppApplication.class, args);
    }

    public BankAppApplication() {
        loadAccountsFromFile();
    }

    // Display Options
    @GetMapping
    public String getOptions() {
        return "Welcome to the Bank App! Here are the options you can use:    " +
                "1. /api/accounts/create - Create a new account   " +
                "2. /api/accounts/{id}/deposit?amount=100 - Deposit money into an account    " +
                "3. /api/accounts/{id}/withdraw?amount=50 - Withdraw money from an account   " +
                "4. /api/accounts/{id}/balance - Check account balance  " +
                "5. /api/accounts/{id}/transfer?targetId=2&amount=20 - Transfer money between accounts   ";
    }

    // Create Account
    @PostMapping("/create")
    public String createAccount() {
        accounts.add(new BankAccount());
        saveAccountsToFile();
        return "Account created. Total accounts: " + accounts.size();
    }

    // Deposit Money
    @PostMapping("/{id}/deposit")
    public String deposit(@PathVariable int id, @RequestParam double amount) {
        if (id > 0 && id <= accounts.size()) {
            BankAccount account = accounts.get(id - 1);
            account.deposit(amount);
            saveAccountsToFile();
            return "Deposited " + amount + " to Account " + id;
        } else {
            return "Invalid account ID.";
        }
    }

    // Withdraw Money
    @PostMapping("/{id}/withdraw")
    public String withdraw(@PathVariable int id, @RequestParam double amount) {
        if (id > 0 && id <= accounts.size()) {
            BankAccount account = accounts.get(id - 1);
            try {
                account.withdraw(amount);
                saveAccountsToFile();
                return "Withdrew " + amount + " from Account " + id;
            } catch (IllegalArgumentException e) {
                return e.getMessage();
            }
        } else {
            return "Invalid account ID.";
        }
    }

    // Get Balance
    @GetMapping("/{id}/balance")
    public String getBalance(@PathVariable int id) {
        if (id > 0 && id <= accounts.size()) {
            BankAccount account = accounts.get(id - 1);
            return "Balance of Account " + id + ": " + account.getBalance();
        } else {
            return "Invalid account ID.";
        }
    }

    // Transfer Money
    @PostMapping("/{id}/transfer")
    public String transfer(@PathVariable int id, @RequestParam int targetId, @RequestParam double amount) {
        if (id > 0 && id <= accounts.size() && targetId > 0 && targetId <= accounts.size() && id != targetId) {
            BankAccount sourceAccount = accounts.get(id - 1);
            BankAccount targetAccount = accounts.get(targetId - 1);
            try {
                sourceAccount.transfer(targetAccount, amount);
                saveAccountsToFile();
                return "Transferred " + amount + " from Account " + id + " to Account " + targetId;
            } catch (IllegalArgumentException e) {
                return e.getMessage();
            }
        } else {
            return "Invalid account IDs.";
        }
    }

    private void loadAccountsFromFile() {
        File file = new File("accounts.txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    double balance = Double.parseDouble(line.trim());
                    accounts.add(new BankAccount(balance));
                }
            } catch (IOException | NumberFormatException e) {
                System.out.println("Error loading accounts: " + e.getMessage());
            }
        }
    }

    private void saveAccountsToFile() {
        try (PrintWriter writer = new PrintWriter("accounts.txt")) {
            for (BankAccount account : accounts) {
                writer.println(account.getBalance());
            }
        } catch (IOException e) {
            System.out.println("Error saving accounts: " + e.getMessage());
        }
    }
}