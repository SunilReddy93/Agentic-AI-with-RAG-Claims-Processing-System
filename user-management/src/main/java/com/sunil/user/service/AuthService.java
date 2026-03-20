package com.sunil.user.service;

import com.sunil.user.dto.AuthResponse;
import com.sunil.user.dto.LoginRequest;
import com.sunil.user.dto.RegisterRequest;
import com.sunil.user.dto.UserResponse;
import com.sunil.user.entity.User;
import com.sunil.user.enums.UserRole;
import com.sunil.user.enums.UserStatus;
import com.sunil.user.exception.BadCredentials;
import com.sunil.user.exception.UserAlreadyExists;
import com.sunil.user.exception.UserInactiveException;
import com.sunil.user.repository.UserRepository;
import com.sunil.user.security.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public UserResponse register(RegisterRequest registerRequest) {
        String username = registerRequest.getUsername();
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExists("User already exists with username : " + username, HttpStatus.CONFLICT);
        }

        String email = registerRequest.getEmail();
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExists("User already exists with email : " + email, HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.ROLE_USER)
                .build();

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    public UserResponse registerAdmin(RegisterRequest registerRequest) {
        String username = registerRequest.getUsername();
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExists("User already exists with username : " + username, HttpStatus.CONFLICT);
        }

        String email = registerRequest.getEmail();
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExists("User already exists with email : " + email, HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.ROLE_ADMIN)
                .build();

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        String usernameOrEmail = request.getUsernameOrEmail();
        User user = userRepository.findByEmailOrUsername(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new BadCredentials("Invalid username or email : " + usernameOrEmail,
                        HttpStatus.UNAUTHORIZED));

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new UserInactiveException("Account is inactive. Please contact support.", HttpStatus.FORBIDDEN);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentials("Invalid Password", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtUtils.generateToken(user.getUsername(), user.getRole().name(), user.getId());

        return AuthResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .token(token)
                .tokenType("Bearer")
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .status(user.getStatus())
                .email(user.getEmail())
                .username(user.getUsername())
                .build();
    }
}
