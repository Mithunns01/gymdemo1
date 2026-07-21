package com.example.gymdemo.service;

import com.example.gymdemo.dto.*;
import com.example.gymdemo.entity.*;
import com.example.gymdemo.exception.BadRequestException;
import com.example.gymdemo.exception.ResourceNotFoundException;
import com.example.gymdemo.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final TrainerRepository trainerRepository;
    private final MemberMembershipRepository membershipRepository;
    private final AttendanceRepository attendanceRepository;
    private final PaymentRepository paymentRepository;
    private final WorkoutPlanRepository workoutPlanRepository;

    public MemberService(MemberRepository memberRepository,
                         UserRepository userRepository,
                         TrainerRepository trainerRepository,
                         MemberMembershipRepository membershipRepository,
                         AttendanceRepository attendanceRepository,
                         PaymentRepository paymentRepository,
                         WorkoutPlanRepository workoutPlanRepository) {
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.trainerRepository = trainerRepository;
        this.membershipRepository = membershipRepository;
        this.attendanceRepository = attendanceRepository;
        this.paymentRepository = paymentRepository;
        this.workoutPlanRepository = workoutPlanRepository;
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", id));
    }

    public Member getMemberByUserId(Long userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member for user", userId));
    }

    public BigDecimal calculateBmi(BmiRequest request) {
        if (request.getHeight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Height must be positive");
        }
        BigDecimal heightInMeters = request.getHeight().divide(BigDecimal.valueOf(100));
        BigDecimal bmi = request.getWeight().divide(
                heightInMeters.multiply(heightInMeters), 2, java.math.RoundingMode.HALF_UP);
        return bmi;
    }

    @Transactional
    public Member updateMember(Long id, MemberRequest request) {
        Member member = getMemberById(id);
        User user = member.getUser();

        if (request.getName() != null) user.setName(request.getName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getEmail() != null) {
            if (!user.getEmail().equals(request.getEmail()) &&
                    userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getGender() != null) member.setGender(request.getGender());
        if (request.getDateOfBirth() != null) member.setDateOfBirth(request.getDateOfBirth());
        if (request.getHeight() != null) member.setHeight(request.getHeight());
        if (request.getWeight() != null) member.setWeight(request.getWeight());
        if (request.getEmergencyContact() != null) member.setEmergencyContact(request.getEmergencyContact());
        if (request.getMedicalConditions() != null) member.setMedicalConditions(request.getMedicalConditions());
        if (request.getAssignedTrainerId() != null) {
            Trainer trainer = trainerRepository.findById(request.getAssignedTrainerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Trainer", request.getAssignedTrainerId()));
            member.setAssignedTrainer(trainer);
        }

        userRepository.save(user);
        return memberRepository.save(member);
    }

    @Transactional
    public void deleteMember(Long id) {
        Member member = getMemberById(id);
        User user = member.getUser();
        user.setEnabled(false);
        member.setActive(false);
        userRepository.save(user);
        memberRepository.save(member);
    }

    public List<Member> getMembersByTrainer(Long trainerId) {
        return memberRepository.findActiveByTrainerId(trainerId);
    }

    public MemberMembership getActiveMembership(Long memberId) {
        return membershipRepository.findTopByMemberIdAndActiveTrueOrderByEndDateDesc(memberId)
                .orElse(null);
    }

    public List<MemberMembership> getMembershipHistory(Long memberId) {
        return membershipRepository.findByMemberId(memberId);
    }

    public Trainer getAssignedTrainer(Long memberId) {
        Member member = getMemberById(memberId);
        return member.getAssignedTrainer();
    }

    public List<WorkoutPlan> getWorkoutPlans(Long memberId) {
        return workoutPlanRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    public List<Attendance> getAttendanceHistory(Long memberId) {
        return attendanceRepository.findByMemberIdOrderByAttendanceDateDesc(memberId);
    }

    public List<Payment> getPaymentHistory(Long memberId) {
        return paymentRepository.findByMemberIdOrderByPaymentDateDesc(memberId);
    }

    public List<Member> searchMembers(String keyword) {
        return memberRepository.searchMembers(keyword);
    }

    @Transactional
    public Member updateMemberProgress(Long memberId, MemberProgressRequest request) {
        Member member = getMemberById(memberId);
        if (request.getWeight() != null) member.setWeight(request.getWeight());
        if (request.getBmi() != null) member.setBmi(request.getBmi());
        return memberRepository.save(member);
    }
}

