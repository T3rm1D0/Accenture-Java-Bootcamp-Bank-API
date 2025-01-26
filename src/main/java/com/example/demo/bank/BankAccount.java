package com.example.demo.bank;

class BankAccount {

    private double balance;

    public BankAccount() {
        this.balance = 0;
    }

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
        balance += amount;
    }

    public void withdraw(double amount) {
        if (amount <= 0 || amount > balance) {
            throw new IllegalArgumentException("Invalid withdrawal amount.");
        }
        balance -= amount;
    }

    public void transfer(BankAccount targetAccount, double amount) {
        if (amount <= 0 || amount > this.balance) {
            throw new IllegalArgumentException("Invalid transfer amount.");
        }
        this.withdraw(amount);
        targetAccount.deposit(amount);
    }

    public double getBalance() {
        return balance;
    }
}