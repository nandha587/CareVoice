package com.carevoice.service;

import com.carevoice.dto.AuthDtos;
import com.carevoice.entity.User;
import com.carevoice.exception.BadRequestException;
import com.carevoice.repository.UserRepository;
import com.carevoice.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditLogService auditLogService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BadRequestException("An account with this email already exists");
        }

        User user = new User(
                request.getFullName().trim(),
                request.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(request.getPassword()),
                "ROLE_CAREGIVER"
        );

        User savedUser = userRepository.save(user);
        auditLogService.log(savedUser.getId(), "CAREGIVER_REGISTERED", "User", savedUser.getId().toString(), "New caregiver registered: " + savedUser.getEmail());

        String token = tokenProvider.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
        return new AuthDtos.AuthResponse(token, savedUser.getId().toString(), savedUser.getEmail(), savedUser.getFullName(), savedUser.getRole());
    }

    @Transactional(readOnly = true)
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password");
        }

        auditLogService.log(user.getId(), "CAREGIVER_LOGGED_IN", "User", user.getId().toString(), "Caregiver login success: " + user.getEmail());

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthDtos.AuthResponse(token, user.getId().toString(), user.getEmail(), user.getFullName(), user.getRole());
    }
}
