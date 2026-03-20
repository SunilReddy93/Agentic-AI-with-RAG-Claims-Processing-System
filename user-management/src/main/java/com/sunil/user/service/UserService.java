package com.sunil.user.service;

import com.sunil.user.dto.UserResponse;
import com.sunil.user.entity.User;
import com.sunil.user.exception.UserNotFoundException;
import com.sunil.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + id, HttpStatus.NOT_FOUND));

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .status(user.getStatus())
                .build();
    }
}
