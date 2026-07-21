package com.example.gymdemo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AttendanceRequest {
    @NotNull
    private Long memberId;

    private LocalDate attendanceDate;
    private String notes;
}

