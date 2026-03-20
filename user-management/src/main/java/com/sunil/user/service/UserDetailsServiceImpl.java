package com.sunil.user.service;

import com.sunil.user.entity.User;
import com.sunil.user.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByEmailOrUsername(usernameOrEmail, usernameOrEmail)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email : " + usernameOrEmail));

        // Build and return Spring Security UserDetails object
        return new org.springframework.security.core.userdetails.User (
                user.getUsername(),
                user.getPassword(),
                List.of((new SimpleGrantedAuthority(user.getRole().name())))
        );
    }
}
