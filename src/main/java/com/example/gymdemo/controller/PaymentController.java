package com.example.gymdemo.controller;

import com.example.gymdemo.dto.ApiResponse;
import com.example.gymdemo.dto.PaymentRequest;
import com.example.gymdemo.entity.Payment;
import com.example.gymdemo.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payments", description = "Payment management APIs")
public class PaymentController {

    private final AdminService adminService;

    public PaymentController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    @Operation(summary = "Get All Payments", description = "Retrieve all payment records")
    public ResponseEntity<ApiResponse<List<Payment>>> getAllPayments() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllPayments()));
    }

    @PostMapping
    @Operation(summary = "Record Payment", description = "Record a new payment transaction")
    public ResponseEntity<ApiResponse<Payment>> recordPayment(
            @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment recorded successfully",
                adminService.recordPayment(request)));
    }

    @GetMapping("/report")
    @Operation(summary = "Monthly Revenue Report", description = "Get monthly revenue report for dashboard charts")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRevenueReport(
            @RequestParam(required = false) Integer year) {
        if (year == null) year = LocalDate.now().getYear();
        return ResponseEntity.ok(ApiResponse.success(adminService.getMonthlyRevenueReport(year)));
    }
}

