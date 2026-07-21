package com.example.gymdemo.controller;

import com.example.gymdemo.dto.ApiResponse;
import com.example.gymdemo.dto.AttendanceRequest;
import com.example.gymdemo.entity.Attendance;
import com.example.gymdemo.service.AdminService;
import com.example.gymdemo.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/attendance")
@Tag(name = "Attendance", description = "Attendance management APIs")
public class AttendanceController {

    private final AdminService adminService;
    private final MemberService memberService;

    public AttendanceController(AdminService adminService, MemberService memberService) {
        this.adminService = adminService;
        this.memberService = memberService;
    }

    @PostMapping
    @Operation(summary = "Mark Attendance", description = "Mark daily attendance for a member")
    public ResponseEntity<ApiResponse<Attendance>> markAttendance(
            @Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Attendance marked successfully",
                adminService.markAttendance(request)));
    }

    @GetMapping("/{memberId}")
    @Operation(summary = "Get Attendance History", description = "Get attendance history for a member")
    public ResponseEntity<ApiResponse<List<Attendance>>> getAttendanceHistory(
            @PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getAttendanceHistory(memberId)));
    }
}

