package com.example.gymdemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class WorkoutPlanRequest {
    @NotNull
    private Long memberId;

    @NotBlank
    private String title;

    private String description;
    private String exercises;
    private String difficulty;
    private Integer durationWeeks;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String notes;
}

