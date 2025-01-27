package com.example.demo.bank;

import jakarta.persistence.*;

@Entity
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double balance;

    public BankAccount() {}

    public BankAccount(double initialBalance) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative.");
        }
        this.balance = initialBalance;
    }

    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero.");
        }
        this.balance += amount;
    }

    public void withdraw(double amount) {
        if (amount <= 0 || amount > this.balance) {
            throw new IllegalArgumentException("Invalid withdrawal amount.");
        }
        this.balance -= amount;
    }

    public Long getId() {
        return id;
    }

    public double getBalance() {
        return balance;
    }
    public void transfer(BankAccount targetAccount, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero.");
        }
        if (this.balance < amount) {
            throw new IllegalArgumentException("Insufficient balance for transfer.");
        }
        this.withdraw(amount);  // Deduct from source account
        targetAccount.deposit(amount);  // Add to target account
    }

}
