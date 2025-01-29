package com.example.demo.bank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String registerUser(String username, String password, String email) {
        try {
            if (userRepository.findByUsername(username).isPresent()) {
                return "Username already exists.";
            }
            if (userRepository.findByEmail(email).isPresent()) {
                return "Email already exists.";
            }

            // Create a new bank account for the user
            BankAccount bankAccount = new BankAccount();
            bankAccountRepository.save(bankAccount);

            // Create a new user
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email);
            user.setBankAccount(bankAccount);

            userRepository.save(user);

            return "User registered successfully! Bank Account ID: " + bankAccount.getId();
        } catch (DataIntegrityViolationException e) {
            return "Error: Username or email already exists.";
        } catch (Exception e) {
            return "Error: Unable to register user.";
        }
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

            // Delete associated bank account
            if (user.getBankAccount() != null) {
                bankAccountRepository.delete(user.getBankAccount());
            }

            // Delete user account
            userRepository.delete(user);
            return "User and associated bank account deleted successfully.";
        }

        return "User not found.";
    }
}
