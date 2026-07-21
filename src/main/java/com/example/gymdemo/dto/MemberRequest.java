package com.example.gymdemo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MemberRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @Email
    @NotBlank
    private String email;

    private String phone;
    private String gender;
    private LocalDate dateOfBirth;
    private BigDecimal height;
    private BigDecimal weight;
    private String emergencyContact;
    private String medicalConditions;
    private Long assignedTrainerId;
}

