package com.example.demo.bank;

import com.example.demo.bank.ExternalTransferRequest;
import com.example.demo.bank.BankAccount;
import com.example.demo.bank.User;
import com.example.demo.bank.BankAccountRepository;
import com.example.demo.bank.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
public class BankAppController {

    @Autowired
    private BankAccountRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthController authController;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/deposit")
    public String deposit(@RequestParam String username, @RequestParam String bankaccountAddress, @RequestParam double amount) {
        if (!authController.isUserLoggedIn(username)) {
            return "Unauthorized. Please log in.";
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return "User not found.";
        }

        User user = userOpt.get();
        BankAccount account = user.getBankAccount();

        if (account == null || !account.getBankaccountAddress().equals(bankaccountAddress)) {
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

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam String username, @RequestParam String bankaccountAddress, @RequestParam double amount) {
        if (!authController.isUserLoggedIn(username)) {
            return "Unauthorized. Please log in.";
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return "User not found.";
        }

        User user = userOpt.get();
        BankAccount account = user.getBankAccount();

        if (account == null || !account.getBankaccountAddress().equals(bankaccountAddress)) {
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

    @PostMapping("/transfer/internal")
    public String internalTransfer(
            @RequestParam String username,
            @RequestParam String targetBankAccountAddress,
            @RequestParam double amount) {

        if (!authController.isUserLoggedIn(username)) {
            return "Unauthorized. Please log in.";
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return "User not found.";
        }

        User user = userOpt.get();
        BankAccount sourceAccount = user.getBankAccount();

        if (sourceAccount == null) {
            return "You don't have a bank account.";
        }

        Optional<BankAccount> targetAccountOpt = repository.findByBankaccountAddress(targetBankAccountAddress);
        if (targetAccountOpt.isEmpty()) {
            return "Target account not found.";
        }

        BankAccount targetAccount = targetAccountOpt.get();

        try {
            sourceAccount.transfer(targetAccount, amount);
            repository.save(sourceAccount);
            repository.save(targetAccount);
            return "Transferred " + amount + " from your account to Account: " + targetBankAccountAddress;
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    @PostMapping("/transfer/external")
    public String externalTransfer(
            @RequestParam String username,
            @RequestParam String toAccountNumber,
            @RequestParam double amount) {

        if (!authController.isUserLoggedIn(username)) {
            return "Unauthorized. Please log in.";
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return "User not found.";
        }

        User user = userOpt.get();
        BankAccount sourceAccount = user.getBankAccount();

        if (sourceAccount == null) {
            return "You don't have a bank account.";
        }

        if (sourceAccount.getBalance() < amount) {
            return "Insufficient balance.";
        }

        ExternalTransferRequest transferRequest = new ExternalTransferRequest();
        transferRequest.setFromAccountNumber(user.getUniqueId());
        transferRequest.setToAccountNumber(toAccountNumber);
        transferRequest.setAmount(amount);

        String externalApiUrl = "https://springboot-render-2-c5m2.onrender.com/swagger-ui/index.html#/bank-app-controller/externalTransfer";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ExternalTransferRequest> request = new HttpEntity<>(transferRequest, headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(externalApiUrl, HttpMethod.POST, request, String.class);
        } catch (Exception e) {
            return "Transfer failed: " + e.getMessage();
        }

        if (response.getStatusCode().is2xxSuccessful()) {
            sourceAccount.withdraw(amount);
            repository.save(sourceAccount);
            return "Transfer successful: " + response.getBody();
        } else {
            return "Transfer failed: " + response.getBody();
        }
    }

    @PostMapping("/transfer/receive")
    public ResponseEntity<String> receiveExternalTransfer(@RequestBody ExternalTransferRequest transferRequest) {
        Optional<BankAccount> recipientAccountOpt = repository.findByBankaccountAddress(transferRequest.getToAccountNumber());
        if (recipientAccountOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Recipient account not found.");
        }

        BankAccount recipientAccount = recipientAccountOpt.get();

        try {
            recipientAccount.deposit(transferRequest.getAmount());
            repository.save(recipientAccount);
            return ResponseEntity.ok("Transfer received successfully. New balance: " + recipientAccount.getBalance());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid transfer amount.");
        }
    }
}