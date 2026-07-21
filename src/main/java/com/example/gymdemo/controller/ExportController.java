package com.example.gymdemo.controller;

import com.example.gymdemo.service.AdminService;
import com.example.gymdemo.service.ExcelExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/export")
@Tag(name = "Export", description = "Data export APIs (Excel)")
public class ExportController {

    private final AdminService adminService;
    private final ExcelExportService excelExportService;

    public ExportController(AdminService adminService, ExcelExportService excelExportService) {
        this.adminService = adminService;
        this.excelExportService = excelExportService;
    }

    @GetMapping("/attendance")
    @Operation(summary = "Export Attendance Report", description = "Export daily attendance report to Excel")
    public ResponseEntity<byte[]> exportAttendance(
            @RequestParam(required = false) LocalDate date) throws Exception {
        if (date == null) date = LocalDate.now();
        byte[] data = excelExportService.exportAttendanceReport(
                adminService.getDailyAttendanceReport(date));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=attendance_report_" + date + ".xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
}

