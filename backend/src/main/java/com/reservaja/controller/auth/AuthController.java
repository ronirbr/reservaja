package com.reservaja.controller.auth;

import com.reservaja.dto.auth.AuthResponse;
import com.reservaja.dto.auth.LoginRequest;
import com.reservaja.dto.auth.RegisterRequest;
import com.reservaja.model.entity.User;
import com.reservaja.repository.UserRepository;
import com.reservaja.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        // se chegar aqui, autenticação OK
        String jwt = tokenProvider.generateToken(authentication.getName());
        return ResponseEntity.ok(new AuthResponse(jwt, "Bearer"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email já está em uso.");
        }

        // define role parão caso nulo
        User.Role role = registerRequest.getRole() == null ? User.Role.ROLE_USER : registerRequest.getRole();

        User user = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(role)
                .build();

        User saved = userRepository.save(user);

        // Retorna 201 Created com location opcional
        URI location = URI.create(String.format("/api/users/%d", saved.getId()));
        return ResponseEntity.created(location).build();
    }
}
