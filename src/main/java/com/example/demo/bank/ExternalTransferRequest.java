package com.example.demo.bank;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class ExternalTransferRequest {

    @NotBlank(message = "Sender account number cannot be empty")
    @Pattern(regexp = "[A-Z]{6}_[a-z0-9]{12}", message = "Invalid sender account number format")
    private String fromAccountNumber;

    @NotBlank(message = "Recipient account number cannot be empty")
    @Pattern(regexp = "[A-Z]{6}_[a-z0-9]{12}", message = "Invalid recipient account number format")
    private String toAccountNumber;

    @NotNull(message = "Amount cannot be null")
    @Min(value = 1, message = "Amount must be greater than zero")
    private Double amount;

    // Default constructor
    public ExternalTransferRequest() {}

    // Parameterized constructor
    public ExternalTransferRequest(String fromAccountNumber, String toAccountNumber, Double amount) {
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.amount = amount;
    }

    // Getters and Setters
    public String getFromAccountNumber() { return fromAccountNumber; }
    public void setFromAccountNumber(String fromAccountNumber) { this.fromAccountNumber = fromAccountNumber; }

    public String getToAccountNumber() { return toAccountNumber; }
    public void setToAccountNumber(String toAccountNumber) { this.toAccountNumber = toAccountNumber; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}