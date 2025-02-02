package com.example.demo.bank.repository;

import com.example.demo.bank.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByBankaccountAddress(String bankaccountAddress);
}