package com.example.demo.bank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
public class BankAppApplication {

    @Autowired
    private BankAccountRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthController authController;

    // Deposit Money (Requires authentication)
    @PostMapping("/{username}/{id}/deposit")
    public String deposit(@PathVariable String username, @PathVariable Long id, @RequestParam double amount) {
        if (!authController.isUserLoggedIn(username)) {
            return "Unauthorized. Please log in.";
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return "User not found.";
        }

        User user = userOpt.get();
        BankAccount account = user.getBankAccount();

        if (account == null || !account.getId().equals(id)) {
            return "You can only deposit to your own account.";
        }

        try {
            account.deposit(amount);
            repository.save(account);
            return "Deposited " + amount + " to your account.";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    // Withdraw Money (Requires authentication)
    @PostMapping("/{username}/{id}/withdraw")
    public String withdraw(@PathVariable String username, @PathVariable Long id, @RequestParam double amount) {
        if (!authController.isUserLoggedIn(username)) {
            return "Unauthorized. Please log in.";
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return "User not found.";
        }

        User user = userOpt.get();
        BankAccount account = user.getBankAccount();

        if (account == null || !account.getId().equals(id)) {
            return "You can only withdraw from your own account.";
        }

        try {
            account.withdraw(amount);
            repository.save(account);
            return "Withdrew " + amount + " from your account.";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    // Get Balance (Requires authentication)
    @GetMapping("/{username}/balance")
    public String getBalance(@PathVariable String username) {
        if (!authController.isUserLoggedIn(username)) {
            return "Unauthorized. Please log in.";
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return "User not found.";
        }

        BankAccount account = userOpt.get().getBankAccount();
        if (account == null) {
            return "You do not have a bank account.";
        }

        return "Balance of your account: " + account.getBalance();
    }

    // Transfer Money (Requires authentication & ownership check)
    @PostMapping("/{username}/transfer")
    public String transfer(
            @PathVariable String username,
            @RequestParam Long targetAccountId,
            @RequestParam double amount) {

        if (!authController.isUserLoggedIn(username)) {
            return "Unauthorized. Please log in.";
        }

        // Find the logged-in user's account
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return "User not found.";
        }

        User user = userOpt.get();
        BankAccount sourceAccount = user.getBankAccount();

        if (sourceAccount == null) {
            return "You don't have a bank account.";
        }

        // Check if the target account exists
        Optional<BankAccount> targetAccountOpt = repository.findById(targetAccountId);
        if (targetAccountOpt.isEmpty()) {
            return "Target account not found.";
        }

        BankAccount targetAccount = targetAccountOpt.get();

        // Perform the transfer
        try {
            sourceAccount.transfer(targetAccount, amount);
            repository.save(sourceAccount);
            repository.save(targetAccount);
            return "Transferred " + amount + " from your account to Account ID: " + targetAccountId;
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }
}
