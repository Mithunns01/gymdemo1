package com.example.gymdemo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberProgressRequest {
    @NotNull
    private Long memberId;

    private BigDecimal weight;
    private BigDecimal bmi;
    private String notes;
}

