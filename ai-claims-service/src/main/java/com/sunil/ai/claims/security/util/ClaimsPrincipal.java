package com.sunil.ai.claims.security.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClaimsPrincipal {

    private Long userId;
    private String username;
    private String role;
}