package com.r16a.metis.identity.controllers;

import com.r16a.metis.identity.dto.AuthenticationRequest;
import com.r16a.metis.identity.dto.AuthenticationResponse;
import com.r16a.metis.identity.dto.UserRegistrationRequest;
import com.r16a.metis.identity.models.User;
import com.r16a.metis.identity.services.AuthenticationService;
import com.r16a.metis.identity.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        Map<String, String> tokens = authenticationService.authenticate(request.getEmail(), request.getPassword());
        
        AuthenticationResponse response = AuthenticationResponse.builder()
                .accessToken(tokens.get("accessToken"))
                .refreshToken(tokens.get("refreshToken"))
                .tokenType(tokens.get("tokenType"))
                .build();
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody UserRegistrationRequest request) {
        User user = userService.registerUser(
                request.getEmail(),
                request.getPassword(),
                request.getName(),
                request.getSurname(),
                request.getTenantId()
        );
        return ResponseEntity.ok(user);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        Map<String, String> tokens = authenticationService.refreshToken(refreshToken);
        
        AuthenticationResponse response = AuthenticationResponse.builder()
                .accessToken(tokens.get("accessToken"))
                .refreshToken(tokens.get("refreshToken"))
                .tokenType(tokens.get("tokenType"))
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestParam String token) {
        // This endpoint can be used to validate tokens on the client side
        // The actual validation happens in the JWT filter
        return ResponseEntity.ok(Map.of("valid", true));
    }
}
