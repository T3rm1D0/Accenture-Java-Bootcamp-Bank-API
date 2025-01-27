package com.example.demo.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@SpringBootApplication
@RestController
@RequestMapping("/api/accounts")
public class BankAppApplication {

    private final BankAccountRepository repository;

    public BankAppApplication(BankAccountRepository repository) {
        this.repository = repository;
    }

    public static void main(String[] args) {
        SpringApplication.run(BankAppApplication.class, args);
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
    public String createAccount(@RequestParam(defaultValue = "0.0") double initialBalance) {
        BankAccount newAccount = new BankAccount(initialBalance);
        repository.save(newAccount);
        return "Account created with ID: " + newAccount.getId();
    }

    // Deposit Money
    @PostMapping("/{id}/deposit")
    public String deposit(@PathVariable Long id, @RequestParam double amount) {
        Optional<BankAccount> accountOpt = repository.findById(id);
        if (accountOpt.isPresent()) {
            BankAccount account = accountOpt.get();
            account.deposit(amount);
            repository.save(account);
            return "Deposited " + amount + " to Account " + id;
        } else {
            return "Invalid account ID.";
        }
    }

    // Withdraw Money
    @PostMapping("/{id}/withdraw")
    public String withdraw(@PathVariable Long id, @RequestParam double amount) {
        Optional<BankAccount> accountOpt = repository.findById(id);
        if (accountOpt.isPresent()) {
            BankAccount account = accountOpt.get();
            try {
                account.withdraw(amount);
                repository.save(account);
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
    public String getBalance(@PathVariable Long id) {
        return repository.findById(id)
                .map(account -> "Balance of Account " + id + ": " + account.getBalance())
                .orElse("Invalid account ID.");
    }

    // Transfer Money
    @PostMapping("/{id}/transfer")
    public String transfer(@PathVariable Long id, @RequestParam Long targetId, @RequestParam double amount) {
        Optional<BankAccount> sourceAccountOpt = repository.findById(id);
        Optional<BankAccount> targetAccountOpt = repository.findById(targetId);

        if (sourceAccountOpt.isPresent() && targetAccountOpt.isPresent() && !id.equals(targetId)) {
            BankAccount sourceAccount = sourceAccountOpt.get();
            BankAccount targetAccount = targetAccountOpt.get();
            try {
                sourceAccount.transfer(targetAccount, amount);
                repository.save(sourceAccount);
                repository.save(targetAccount);
                return "Transferred " + amount + " from Account " + id + " to Account " + targetId;
            } catch (IllegalArgumentException e) {
                return e.getMessage();
            }
        } else {
            return "Invalid account IDs.";
        }
    }
}
