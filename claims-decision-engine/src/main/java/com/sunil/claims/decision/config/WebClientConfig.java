package com.sunil.claims.decision.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${user.management.service.url}")
    private String userManagementUrl;

    @Value("${ai.claims.service.url}")
    private String aiClaimsServiceUrl;

    @Bean
    public WebClient userManagementWebClient() {
        return WebClient.builder()
                .baseUrl(userManagementUrl)
                .build();
    }

    @Bean
    public WebClient aiClaimsWebClient() {
        return WebClient.builder()
                .baseUrl(aiClaimsServiceUrl)
                .build();
    }
}