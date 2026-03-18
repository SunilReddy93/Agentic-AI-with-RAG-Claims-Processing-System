package com.sunil.ai.claims.client;

import com.sunil.ai.claims.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-management", url = "${user.management.service.url}")
public interface UserManagementClient {

    @GetMapping("/api/internal/users/{id}")
    UserResponse getUserById(
            @PathVariable("id") Long userId,
            @RequestHeader("X-Internal-Api-Key") String apiKey
    );
}