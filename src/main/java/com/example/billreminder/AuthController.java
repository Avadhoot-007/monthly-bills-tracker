package com.example.billreminder;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

// This class handles register, login, logout, and "who am I"
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    // This tool scrambles passwords and checks them later
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // BCrypt only reads the first 72 bytes of a password
    private boolean tooLongForBcrypt(String password) {
        return password.getBytes(StandardCharsets.UTF_8).length > 72;
    }

    // POST /api/auth/register
    // Returns "OK" or an error message
    @PostMapping("/register")
    public String register(@RequestBody AuthRequest request) {
        String username = "";
        if (request.getUsername() != null) {
            username = request.getUsername().trim();
        }
        String password = "";
        if (request.getPassword() != null) {
            password = request.getPassword();
        }

        // Check the rules
        if (username.length() < 3 || username.length() > 50) {
            return "Username must be 3 to 50 characters";
        }
        if (password.length() < 6 || password.length() > 50) {
            return "Password must be 6 to 50 characters";
        }
        if (tooLongForBcrypt(password)) {
            return "Password is too long. Use fewer special characters";
        }
        if (userRepository.findByUsername(username).isPresent()) {
            return "Username already taken";
        }

        // Save the new user with a scrambled password
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(encoder.encode(password));
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            // Two people took the same name at the same moment
            return "Username already taken";
        }
        return "OK";
    }

    // POST /api/auth/login
    // Returns "OK" or an error message
    @PostMapping("/login")
    public String login(@RequestBody AuthRequest request, HttpServletRequest httpRequest) {
        String username = "";
        if (request.getUsername() != null) {
            username = request.getUsername().trim();
        }
        String password = "";
        if (request.getPassword() != null) {
            password = request.getPassword();
        }
        if (tooLongForBcrypt(password)) {
            return "Wrong username or password";
        }

        // Look for the user
        Optional<User> result = userRepository.findByUsername(username);
        if (!result.isPresent()) {
            return "Wrong username or password";
        }
        User user = result.get();

        // Compare the typed password with the scrambled one
        if (!encoder.matches(password, user.getPasswordHash())) {
            return "Wrong username or password";
        }

        // Fresh session id after login. Blocks session fixation
        HttpSession session = httpRequest.getSession();
        httpRequest.changeSessionId();

        // Remember this user in the server session
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());
        return "OK";
    }

    // POST /api/auth/logout
    // getSession(false): do not create a session just to delete it
    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // forget the user
        }
        return "OK";
    }

    // GET /api/auth/me
    // Returns the logged-in username, or empty text if nobody is logged in
    // getSession(false): anonymous visitors must not create sessions
    @GetMapping("/me")
    public String whoAmI(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "";
        }
        Object name = session.getAttribute("username");
        if (name == null) {
            return "";
        }
        return name.toString();
    }
}
