package com.example.gymdemo.controller;

import com.example.gymdemo.dto.*;
import com.example.gymdemo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication APIs for Login and Registration")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticate user and return JWT token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/register")
    @Operation(summary = "Register New Member", description = "Register a new gym member")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody MemberRequest request) {
        LoginResponse response = authService.registerMember(request);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }
}

