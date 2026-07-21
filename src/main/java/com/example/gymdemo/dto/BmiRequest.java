package com.example.gymdemo.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BmiRequest {
    @Positive
    private BigDecimal height;

    @Positive
    private BigDecimal weight;
}

