package com.example.gymdemo.service;

import com.example.gymdemo.dto.*;
import com.example.gymdemo.entity.*;
import com.example.gymdemo.exception.BadRequestException;
import com.example.gymdemo.exception.ResourceNotFoundException;
import com.example.gymdemo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Service
public class AdminService {

    private final MemberRepository memberRepository;
    private final TrainerRepository trainerRepository;
    private final MembershipPlanRepository planRepository;
    private final MemberMembershipRepository membershipRepository;
    private final PaymentRepository paymentRepository;
    private final AttendanceRepository attendanceRepository;
    private final WorkoutPlanRepository workoutPlanRepository;

    public AdminService(MemberRepository memberRepository,
                        TrainerRepository trainerRepository,
                        MembershipPlanRepository planRepository,
                        MemberMembershipRepository membershipRepository,
                        PaymentRepository paymentRepository,
                        AttendanceRepository attendanceRepository,
                        WorkoutPlanRepository workoutPlanRepository) {
        this.memberRepository = memberRepository;
        this.trainerRepository = trainerRepository;
        this.planRepository = planRepository;
        this.membershipRepository = membershipRepository;
        this.paymentRepository = paymentRepository;
        this.attendanceRepository = attendanceRepository;
        this.workoutPlanRepository = workoutPlanRepository;
    }

    // ---- Membership Plans ----
    public List<MembershipPlan> getAllPlans() {
        return planRepository.findByActiveTrue();
    }

    public MembershipPlan getPlanById(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MembershipPlan", id));
    }

    @Transactional
    public MembershipPlan createPlan(MembershipPlanRequest request) {
        MembershipPlan plan = MembershipPlan.builder()
                .name(request.getName())
                .description(request.getDescription())
                .durationDays(request.getDurationDays())
                .price(request.getPrice())
                .active(request.isActive())
                .build();
        return planRepository.save(plan);
    }

    @Transactional
    public MembershipPlan updatePlan(Long id, MembershipPlanRequest request) {
        MembershipPlan plan = getPlanById(id);
        if (request.getName() != null) plan.setName(request.getName());
        if (request.getDescription() != null) plan.setDescription(request.getDescription());
        if (request.getDurationDays() != null) plan.setDurationDays(request.getDurationDays());
        if (request.getPrice() != null) plan.setPrice(request.getPrice());
        plan.setActive(request.isActive());
        return planRepository.save(plan);
    }

    // ---- Membership Renewal ----
    @Transactional
    public MemberMembership renewMembership(RenewalRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member", request.getMemberId()));
        MembershipPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("MembershipPlan", request.getPlanId()));

        // Deactivate existing active memberships
        List<MemberMembership> existing = membershipRepository.findByMemberId(member.getId());
        existing.forEach(m -> m.setActive(false));
        membershipRepository.saveAll(existing);

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(plan.getDurationDays());

        MemberMembership membership = MemberMembership.builder()
                .member(member)
                .plan(plan)
                .startDate(startDate)
                .endDate(endDate)
                .active(true)
                .amountPaid(plan.getPrice())
                .paymentStatus("COMPLETED")
                .build();
        membership = membershipRepository.save(membership);

        // Create payment record
        Payment payment = Payment.builder()
                .member(member)
                .membership(membership)
                .amount(plan.getPrice())
                .paymentDate(LocalDate.now())
                .paymentMode(request.getPaymentMode() != null ? request.getPaymentMode() : "CASH")
                .transactionId(request.getTransactionId())
                .status("COMPLETED")
                .notes("Membership renewal - " + plan.getName())
                .build();
        paymentRepository.save(payment);

        return membership;
    }

    // ---- Payment Reports ----
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public List<Payment> getPaymentsByDateRange(LocalDate start, LocalDate end) {
        return paymentRepository.findByPaymentDateBetweenOrderByPaymentDate(start, end);
    }

    public BigDecimal getRevenueBetween(LocalDate start, LocalDate end) {
        return paymentRepository.getRevenueBetween(start, end);
    }

    public List<Map<String, Object>> getMonthlyRevenueReport(int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        List<Object[]> data = paymentRepository.getMonthlyRevenueReport(start, end);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : data) {
            Map<String, Object> map = new HashMap<>();
            map.put("month", row[0]);
            map.put("year", row[1]);
            map.put("revenue", row[2]);
            result.add(map);
        }
        return result;
    }

    // ---- Attendance Reports ----
    public List<Object[]> getDailyAttendanceReport(LocalDate date) {
        return attendanceRepository.findDailyAttendanceReport(date);
    }

    public long getTodayAttendanceCount() {
        return attendanceRepository.countByAttendanceDate(LocalDate.now());
    }

    public List<Map<String, Object>> getMonthlyAttendanceChart(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        List<Object[]> data = attendanceRepository.findTopActiveMembers(start, end);
        List<Map<String, Object>> result = new ArrayList<>();
        int limit = Math.min(data.size(), 10);
        for (int i = 0; i < limit; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("memberId", data.get(i)[0]);
            map.put("name", data.get(i)[1]);
            map.put("count", data.get(i)[2]);
            result.add(map);
        }
        return result;
    }

    // ---- Dashboard ----
    public DashboardStats getDashboardStats() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        long totalMembers = memberRepository.count();
        long activeMembers = membershipRepository.countActiveMemberships(today);
        long expiredMemberships = membershipRepository.countExpiredMemberships(today);
        long totalTrainers = trainerRepository.countByActiveTrue();
        long todayAttendance = attendanceRepository.countByAttendanceDate(today);
        BigDecimal monthlyRevenue = paymentRepository.getRevenueBetween(monthStart, monthEnd);
        long expiringThisMonth = membershipRepository.countMembershipsExpiringBetween(today, monthEnd);

        List<Map<String, Object>> monthlyRevenueChart = getMonthlyRevenueReport(today.getYear());
        List<Map<String, Object>> attendanceChart = getMonthlyAttendanceChart(today.getYear(), today.getMonthValue());

        return DashboardStats.builder()
                .totalMembers(totalMembers)
                .activeMembers(activeMembers)
                .expiredMemberships(expiredMemberships)
                .totalTrainers(totalTrainers)
                .todayAttendance(todayAttendance)
                .monthlyRevenue(monthlyRevenue)
                .expiringThisMonth(expiringThisMonth)
                .monthlyRevenueChart(monthlyRevenueChart)
                .attendanceChart(attendanceChart)
                .build();
    }

    @Transactional
    public Payment recordPayment(PaymentRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member", request.getMemberId()));

        MemberMembership membership = null;
        if (request.getMembershipId() != null) {
            membership = membershipRepository.findById(request.getMembershipId())
                    .orElse(null);
        }

        Payment payment = Payment.builder()
                .member(member)
                .membership(membership)
                .amount(request.getAmount())
                .paymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now())
                .paymentMode(request.getPaymentMode())
                .transactionId(request.getTransactionId())
                .status("COMPLETED")
                .notes(request.getNotes())
                .build();
        return paymentRepository.save(payment);
    }

    @Transactional
    public Attendance markAttendance(AttendanceRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member", request.getMemberId()));

        // Check if membership is active
        MemberMembership activeMembership = membershipRepository
                .findTopByMemberIdAndActiveTrueOrderByEndDateDesc(member.getId())
                .orElseThrow(() -> new BadRequestException("No active membership found"));

        if (activeMembership.getEndDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Membership has expired. Please renew.");
        }

        LocalDate today = request.getAttendanceDate() != null ? request.getAttendanceDate() : LocalDate.now();

        // Check duplicate attendance
        Optional<Attendance> existing = attendanceRepository
                .findByMemberIdAndAttendanceDate(member.getId(), today);
        if (existing.isPresent()) {
            throw new BadRequestException("Attendance already marked for today");
        }

        Attendance attendance = Attendance.builder()
                .member(member)
                .attendanceDate(today)
                .checkInTime(java.time.LocalTime.now())
                .status("PRESENT")
                .notes(request.getNotes())
                .build();
        return attendanceRepository.save(attendance);
    }

    public List<Map<String, Object>> getExpiringMemberships(int days) {
        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(days);
        List<MemberMembership> expiring = membershipRepository.findMembershipsExpiringBetween(today, end);
        List<Map<String, Object>> result = new ArrayList<>();
        for (MemberMembership mm : expiring) {
            Map<String, Object> map = new HashMap<>();
            map.put("membershipId", mm.getId());
            map.put("memberId", mm.getMember().getId());
            map.put("memberName", mm.getMember().getUser().getName());
            map.put("planName", mm.getPlan().getName());
            map.put("endDate", mm.getEndDate());
            map.put("daysRemaining", java.time.temporal.ChronoUnit.DAYS.between(today, mm.getEndDate()));
            result.add(map);
        }
        return result;
    }
}

