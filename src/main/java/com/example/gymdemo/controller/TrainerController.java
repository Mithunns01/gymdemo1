package com.example.gymdemo.controller;

import com.example.gymdemo.dto.*;
import com.example.gymdemo.entity.*;
import com.example.gymdemo.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trainers")
@Tag(name = "Trainers", description = "Trainer management APIs")
public class TrainerController {

    private final TrainerService trainerService;

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @GetMapping
    @Operation(summary = "Get All Trainers", description = "Retrieve all active trainers")
    public ResponseEntity<ApiResponse<List<Trainer>>> getAllTrainers() {
        return ResponseEntity.ok(ApiResponse.success(trainerService.getAllTrainers()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Trainer by ID", description = "Retrieve a specific trainer by ID")
    public ResponseEntity<ApiResponse<Trainer>> getTrainerById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(trainerService.getTrainerById(id)));
    }

    @PostMapping
    @Operation(summary = "Create Trainer", description = "Create a new trainer (Admin only)")
    public ResponseEntity<ApiResponse<Trainer>> createTrainer(@Valid @RequestBody TrainerRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Trainer created successfully",
                trainerService.createTrainer(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Trainer", description = "Update trainer information")
    public ResponseEntity<ApiResponse<Trainer>> updateTrainer(
            @PathVariable Long id,
            @Valid @RequestBody TrainerRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Trainer updated successfully",
                trainerService.updateTrainer(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Trainer", description = "Soft delete / deactivate a trainer")
    public ResponseEntity<ApiResponse<Void>> deleteTrainer(@PathVariable Long id) {
        trainerService.deleteTrainer(id);
        return ResponseEntity.ok(ApiResponse.success("Trainer deleted successfully", null));
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "Get Assigned Members", description = "Get all members assigned to a trainer")
    public ResponseEntity<ApiResponse<List<Member>>> getAssignedMembers(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(trainerService.getAssignedMembers(id)));
    }

    @PostMapping("/workout")
    @Operation(summary = "Create Workout Plan", description = "Create a workout plan for a member")
    public ResponseEntity<ApiResponse<WorkoutPlan>> createWorkoutPlan(
            @Valid @RequestBody WorkoutPlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Workout plan created successfully",
                trainerService.createWorkoutPlan(request)));
    }

    @PutMapping("/workout/{id}")
    @Operation(summary = "Update Workout Plan", description = "Update an existing workout plan")
    public ResponseEntity<ApiResponse<WorkoutPlan>> updateWorkoutPlan(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutPlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Workout plan updated successfully",
                trainerService.updateWorkoutPlan(id, request)));
    }

    @GetMapping("/{id}/workouts")
    @Operation(summary = "Get Trainer Workout Plans", description = "Get all workout plans created by a trainer")
    public ResponseEntity<ApiResponse<List<WorkoutPlan>>> getWorkoutPlans(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(trainerService.getWorkoutPlansForTrainer(id)));
    }

    @GetMapping("/member-count")
    @Operation(summary = "Trainer Member Count", description = "Get count of members per trainer")
    public ResponseEntity<ApiResponse<List<Object[]>>> getTrainerMemberCount() {
        return ResponseEntity.ok(ApiResponse.success(trainerService.getTrainerMemberCount()));
    }

}

