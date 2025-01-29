package com.example.demo.bank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    private Map<String, String> sessions = new HashMap<>(); // Store logged-in users' sessions

    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email) {

        if (username == null || username.isEmpty()) {
            return "Username is required.";
        }
        if (password == null || password.isEmpty()) {
            return "Password is required.";
        }
        if (email == null || email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return "Invalid email format.";
        }

        return userService.registerUser(username, password, email);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestParam String username, @RequestParam String password) {
        String result = userService.authenticateUser(username, password);

        Map<String, String> response = new HashMap<>();
        if (result.equals("Login successful!")) {
            String token = username + "_token";  // Generate a simple token
            sessions.put(username, token);
            response.put("message", "Login successful!");
            response.put("token", token);
        } else {
            response.put("message", "Invalid username or password.");
        }

        return response;
    }

    @GetMapping("/logout")
    public String logout(@RequestParam String username) {
        if (sessions.containsKey(username)) {
            sessions.remove(username);
            return "User logged out successfully.";
        }
        return "User not logged in.";
    }

    public boolean isUserLoggedIn(String username) {
        return sessions.containsKey(username);
    }
}
