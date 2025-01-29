package com.example.demo.bank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

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

    @Autowired
    private RestTemplate restTemplate;

    // Deposit Money (Requires authentication)
    @PostMapping("/deposit")
    public String deposit(@RequestParam String username, @RequestParam Long id, @RequestParam double amount) {
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
    @PostMapping("/withdraw")
    public String withdraw(@RequestParam String username, @RequestParam Long id, @RequestParam double amount) {
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
    @GetMapping("/balance")
    public String getBalance(@RequestParam String username) {
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

    // Internal Transfer Money (Requires authentication & ownership check)
    @PostMapping("/transfer/internal")
    public String internalTransfer(
            @RequestParam String username,
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

    // External Transfer Money (Requires authentication)
    @PostMapping("/transfer/external")
    public String externalTransfer(
            @RequestParam String username,
            @RequestParam String toAccountNumber,
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

        // Check if the sender has enough balance
        if (sourceAccount.getBalance() < amount) {
            return "Insufficient balance.";
        }

        // Prepare the request to the recipient's API
        String recipientApiUrl = "https://springboot-render-2-c5m2.onrender.com/swagger-ui/index.html#/bank-app-application/externalTransfer"; // Replace with actual URL
        ExternalTransferRequest transferRequest = new ExternalTransferRequest();
        transferRequest.setFromAccountNumber(user.getUniqueId());
        transferRequest.setToAccountNumber(toAccountNumber);
        transferRequest.setAmount(amount);

        // Send the request to the recipient's API
        String response = restTemplate.postForObject(recipientApiUrl, transferRequest, String.class);

        // If the recipient's API confirms the transfer, deduct the amount from the sender's account
        if (response != null && response.contains("success")) {
            sourceAccount.withdraw(amount);
            repository.save(sourceAccount);
            return "Transfer successful: " + response;
        } else {
            return "Transfer failed: " + response;
        }
    }
}