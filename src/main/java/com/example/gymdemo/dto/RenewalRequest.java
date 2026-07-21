package com.example.gymdemo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RenewalRequest {
    @NotNull
    private Long memberId;

    @NotNull
    private Long planId;

    private String paymentMode;
    private String transactionId;
}

