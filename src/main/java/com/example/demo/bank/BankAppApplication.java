package com.example.demo.bank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;


import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
public class BankAppApplication {

    @Autowired
    private BankAccountRepository repository;

    // Create Account
    @PostMapping("/create")
    public String createAccount() {
        BankAccount newAccount = new BankAccount();
        repository.save(newAccount);
        return "Account created with ID: " + newAccount.getId();
    }

    // Deposit Money
    @PostMapping("/{id}/deposit")
    public String deposit(@PathVariable Long id, @RequestParam double amount) {
        Optional<BankAccount> optionalAccount = repository.findById(id);
        if (optionalAccount.isPresent()) {
            BankAccount account = optionalAccount.get();
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
        Optional<BankAccount> optionalAccount = repository.findById(id);
        if (optionalAccount.isPresent()) {
            BankAccount account = optionalAccount.get();
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
        Optional<BankAccount> optionalAccount = repository.findById(id);
        if (optionalAccount.isPresent()) {
            BankAccount account = optionalAccount.get();
            return "Balance of Account " + id + ": " + account.getBalance();
        } else {
            return "Invalid account ID.";
        }
    }

    // Transfer Money
    @PostMapping("/{id}/transfer")
    public String transfer(@PathVariable Long id, @RequestParam Long targetId, @RequestParam double amount) {
        Optional<BankAccount> sourceAccountOpt = repository.findById(id);
        Optional<BankAccount> targetAccountOpt = repository.findById(targetId);

        if (sourceAccountOpt.isPresent() && targetAccountOpt.isPresent()) {
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
            return "Invalid source or target account ID.";
        }
    }
}
