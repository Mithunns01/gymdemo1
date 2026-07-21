package com.example.gymdemo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentRequest {
    @NotNull
    private Long memberId;

    private Long membershipId;

    @Positive
    @NotNull
    private BigDecimal amount;

    private LocalDate paymentDate;

    @NotNull
    private String paymentMode;

    private String transactionId;
    private String notes;
}

