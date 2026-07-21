package com.example.gymdemo.controller;

import com.example.gymdemo.dto.*;
import com.example.gymdemo.entity.*;
import com.example.gymdemo.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/members")
@Tag(name = "Members", description = "Member management APIs")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    @Operation(summary = "Get All Members", description = "Retrieve all registered members")
    public ResponseEntity<ApiResponse<List<Member>>> getAllMembers() {
        return ResponseEntity.ok(ApiResponse.success(memberService.getAllMembers()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Member by ID", description = "Retrieve a specific member by ID")
    public ResponseEntity<ApiResponse<Member>> getMemberById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getMemberById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Member", description = "Update member profile information")
    public ResponseEntity<ApiResponse<Member>> updateMember(
            @PathVariable Long id,
            @Valid @RequestBody MemberRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Member updated successfully",
                memberService.updateMember(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate Member", description = "Soft delete / deactivate a member")
    public ResponseEntity<ApiResponse<Void>> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok(ApiResponse.success("Member deactivated successfully", null));
    }

    @GetMapping("/{id}/trainer")
    @Operation(summary = "Get Assigned Trainer", description = "Get the trainer assigned to a member")
    public ResponseEntity<ApiResponse<Trainer>> getAssignedTrainer(@PathVariable Long id) {
        Trainer trainer = memberService.getAssignedTrainer(id);
        return ResponseEntity.ok(ApiResponse.success(trainer));
    }

    @GetMapping("/{id}/membership")
    @Operation(summary = "Get Active Membership", description = "Get current active membership details")
    public ResponseEntity<ApiResponse<MemberMembership>> getActiveMembership(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getActiveMembership(id)));
    }

    @GetMapping("/{id}/memberships")
    @Operation(summary = "Get Membership History", description = "Get all memberships history for a member")
    public ResponseEntity<ApiResponse<List<MemberMembership>>> getMembershipHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getMembershipHistory(id)));
    }

    @GetMapping("/{id}/workouts")
    @Operation(summary = "Get Workout Plans", description = "Get all workout plans for a member")
    public ResponseEntity<ApiResponse<List<WorkoutPlan>>> getWorkoutPlans(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getWorkoutPlans(id)));
    }

    @GetMapping("/{id}/attendance")
    @Operation(summary = "Get Attendance History", description = "Get attendance history for a member")
    public ResponseEntity<ApiResponse<List<Attendance>>> getAttendanceHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getAttendanceHistory(id)));
    }

    @GetMapping("/{id}/payments")
    @Operation(summary = "Get Payment History", description = "Get payment history for a member")
    public ResponseEntity<ApiResponse<List<Payment>>> getPaymentHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getPaymentHistory(id)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search Members", description = "Search members by name, phone, or username")
    public ResponseEntity<ApiResponse<List<Member>>> searchMembers(@RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.success(memberService.searchMembers(keyword)));
    }

    @PostMapping("/bmi")
    @Operation(summary = "Calculate BMI", description = "Calculate BMI based on height and weight")
    public ResponseEntity<ApiResponse<Double>> calculateBmi(@Valid @RequestBody BmiRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "BMI calculated successfully",
                memberService.calculateBmi(request).doubleValue()));
    }

    @GetMapping("/by-trainer/{trainerId}")
    @Operation(summary = "Get Members by Trainer", description = "Get all members assigned to a specific trainer")
    public ResponseEntity<ApiResponse<List<Member>>> getMembersByTrainer(@PathVariable Long trainerId) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getMembersByTrainer(trainerId)));
    }
}

