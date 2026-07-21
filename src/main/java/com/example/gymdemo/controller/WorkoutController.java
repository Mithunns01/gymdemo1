package com.example.gymdemo.controller;

import com.example.gymdemo.dto.ApiResponse;
import com.example.gymdemo.dto.WorkoutPlanRequest;
import com.example.gymdemo.entity.WorkoutPlan;
import com.example.gymdemo.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workout")
@Tag(name = "Workout Plans", description = "Workout plan management APIs")
public class WorkoutController {

    private final TrainerService trainerService;

    public WorkoutController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @PostMapping
    @Operation(summary = "Create Workout Plan", description = "Create a new workout plan for a member")
    public ResponseEntity<ApiResponse<WorkoutPlan>> createWorkoutPlan(
            @Valid @RequestBody WorkoutPlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Workout plan created successfully",
                trainerService.createWorkoutPlan(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Workout Plan", description = "Update an existing workout plan")
    public ResponseEntity<ApiResponse<WorkoutPlan>> updateWorkoutPlan(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutPlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Workout plan updated successfully",
                trainerService.updateWorkoutPlan(id, request)));
    }
}

