package com.example.gymdemo.controller;

import com.example.gymdemo.dto.*;
import com.example.gymdemo.entity.*;
import com.example.gymdemo.service.AdminService;
import com.example.gymdemo.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin", description = "Admin management APIs")
public class AdminController {

    private final AdminService adminService;
    private final MemberService memberService;

    public AdminController(AdminService adminService, MemberService memberService) {
        this.adminService = adminService;
        this.memberService = memberService;
    }

    // ---- Membership Plans ----
    @GetMapping("/plans")
    @Operation(summary = "Get All Plans", description = "Get all membership plans")
    public ResponseEntity<ApiResponse<List<MembershipPlan>>> getAllPlans() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllPlans()));
    }

    @PostMapping("/plans")
    @Operation(summary = "Create Membership Plan", description = "Create a new membership plan")
    public ResponseEntity<ApiResponse<MembershipPlan>> createPlan(
            @Valid @RequestBody MembershipPlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Plan created successfully",
                adminService.createPlan(request)));
    }

    @PutMapping("/plans/{id}")
    @Operation(summary = "Update Membership Plan", description = "Update an existing membership plan")
    public ResponseEntity<ApiResponse<MembershipPlan>> updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody MembershipPlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Plan updated successfully",
                adminService.updatePlan(id, request)));
    }

    // ---- Membership Renewals ----
    @PostMapping("/renew")
    @Operation(summary = "Renew Membership", description = "Renew a member's membership plan")
    public ResponseEntity<ApiResponse<MemberMembership>> renewMembership(
            @Valid @RequestBody RenewalRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Membership renewed successfully",
                adminService.renewMembership(request)));
    }

    @GetMapping("/members/expiring")
    @Operation(summary = "Expiring Memberships", description = "Get memberships expiring within given days")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getExpiringMemberships(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getExpiringMemberships(days)));
    }

    // ---- Payments ----
    @GetMapping("/payments")
    @Operation(summary = "Get All Payments", description = "Get all payment records")
    public ResponseEntity<ApiResponse<List<Payment>>> getAllPayments() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllPayments()));
    }

    @PostMapping("/payments")
    @Operation(summary = "Record Payment", description = "Record a new payment")
    public ResponseEntity<ApiResponse<Payment>> recordPayment(
            @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment recorded successfully",
                adminService.recordPayment(request)));
    }

    @GetMapping("/payments/revenue")
    @Operation(summary = "Get Revenue", description = "Get revenue between dates")
    public ResponseEntity<ApiResponse<BigDecimal>> getRevenue(
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end) {
        if (start == null) start = LocalDate.now().withDayOfMonth(1);
        if (end == null) end = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(adminService.getRevenueBetween(start, end)));
    }

    @GetMapping("/payments/report")
    @Operation(summary = "Monthly Revenue Report", description = "Get monthly revenue report for a year")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRevenueReport(
            @RequestParam(required = false) Integer year) {
        if (year == null) year = LocalDate.now().getYear();
        return ResponseEntity.ok(ApiResponse.success(adminService.getMonthlyRevenueReport(year)));
    }

    // ---- Attendance ----
    @PostMapping("/attendance")
    @Operation(summary = "Mark Attendance", description = "Mark attendance for a member")
    public ResponseEntity<ApiResponse<Attendance>> markAttendance(
            @Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Attendance marked successfully",
                adminService.markAttendance(request)));
    }

    @GetMapping("/attendance/daily")
    @Operation(summary = "Daily Attendance Report", description = "Get attendance report for a date")
    public ResponseEntity<ApiResponse<List<Object[]>>> getDailyAttendance(
            @RequestParam(required = false) LocalDate date) {
        if (date == null) date = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(adminService.getDailyAttendanceReport(date)));
    }

    // ---- Member Progress ----
    @PostMapping("/members/{id}/progress")
    @Operation(summary = "Update Member Progress", description = "Update fitness progress for a member")
    public ResponseEntity<ApiResponse<Member>> updateProgress(
            @PathVariable Long id,
            @RequestBody MemberProgressRequest request) {
        request.setMemberId(id);
        return ResponseEntity.ok(ApiResponse.success("Progress updated successfully",
                memberService.updateMemberProgress(id, request)));
    }

    // ---- Dashboard ----
    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard Statistics", description = "Get dashboard statistics and charts")
    public ResponseEntity<ApiResponse<DashboardStats>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDashboardStats()));
    }

    // ---- Search ----
    @GetMapping("/members/search")
    @Operation(summary = "Search Members", description = "Admin search members by keyword")
    public ResponseEntity<ApiResponse<List<Member>>> searchMembers(@RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.success(memberService.searchMembers(keyword)));
    }
}

