package com.example.gymdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String name;
    private String role;
    private String email;
    private List<String> permissions;

    public LoginResponse(String token, Long userId, String username, String name,
                         String role, String email) {
        this.token = token;
        this.tokenType = "Bearer";
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.role = role;
        this.email = email;
    }
}

