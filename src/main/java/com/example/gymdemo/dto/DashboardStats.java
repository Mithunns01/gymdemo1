package com.example.gymdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStats {
    private long totalMembers;
    private long activeMembers;
    private long expiredMemberships;
    private long totalTrainers;
    private long todayAttendance;
    private BigDecimal monthlyRevenue;
    private long expiringThisMonth;
    private List<Map<String, Object>> monthlyRevenueChart;
    private List<Map<String, Object>> attendanceChart;
}

