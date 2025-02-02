package com.example.demo.bank.services;

import com.example.demo.bank.entity.BankAccount;
import com.example.demo.bank.entity.User;
import com.example.demo.bank.repository.BankAccountRepository;
import com.example.demo.bank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.Optional;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String registerUser(String name, String surname, String username, String password, String email) {
        try {
            if (userRepository.findByUsername(username).isPresent()) {
                return "Username already exists.";
            }
            if (userRepository.findByEmail(email).isPresent()) {
                return "Email already exists.";
            }

            String uniqueId = generateUniqueId(name, surname);

            BankAccount bankAccount = new BankAccount();
            bankAccount. setBankaccountAddress(uniqueId);
            bankAccountRepository.save(bankAccount);

            User user = new User();
            user.setUniqueId(uniqueId);
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email);
            user.setBankAccount(bankAccount);

            userRepository.save(user);

            return "User registered successfully! Unique ID: " + uniqueId;
        } catch (DataIntegrityViolationException e) {
            return "Error: Username or email already exists.";
        } catch (Exception e) {
            return "Error: Unable to register user.";
        }
    }

    private String generateUniqueId(String name, String surname) {
        String namePart = name.substring(0, Math.min(name.length(), 3)).toUpperCase();
        String surnamePart = surname.substring(0, Math.min(surname.length(), 3)).toUpperCase();
        String randomPart = generateRandomString(12);
        return namePart + surnamePart + "_" + randomPart;
    }

    private String generateRandomString(int length) {
        String characters = "0123456789abcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    public String authenticateUser(String username, String password) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return "Login successful!";
        }
        return "Invalid username or password.";
    }

    public String deleteUser(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (user.getBankAccount() != null) {
                bankAccountRepository.delete(user.getBankAccount());
            }

            userRepository.delete(user);
            return "User and associated bank account deleted successfully.";
        }

        return "User not found.";
    }
}
