package com.example.gymdemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MembershipPlanRequest {
    @NotBlank
    private String name;

    private String description;

    @Positive
    private Integer durationDays;

    @Positive
    private BigDecimal price;

    private boolean active = true;
}

