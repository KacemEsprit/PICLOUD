package tn.esprit.pidev.controller;

import tn.esprit.pidev.dto.AuthResponse;
import tn.esprit.pidev.dto.LoginRequest;
import tn.esprit.pidev.dto.RegisterRequest;
import tn.esprit.pidev.entity.User;
import tn.esprit.pidev.security.JwtUtil;
import tn.esprit.pidev.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        User user = authService.registerUser(request);
        return ResponseEntity.ok("User registered successfully with username: " + user.getUsername());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Authentication authentication = authService.authenticateUser(request);
        String token = jwtUtil.generateJwtToken(authentication);

        // Get the actual User entity from the database using email or username
        String identifier = (request.getEmail() != null && !request.getEmail().isEmpty()) 
            ? request.getEmail() 
            : request.getUsername();
        User user = authService.getUserByUsername(identifier);
        
        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getUsername(),
                user.getEmail(), user.getName(), user.getRole()));
    }
}