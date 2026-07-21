package com.example.gymdemo.service;

import com.example.gymdemo.dto.TrainerRequest;
import com.example.gymdemo.dto.WorkoutPlanRequest;
import com.example.gymdemo.entity.*;
import com.example.gymdemo.exception.BadRequestException;
import com.example.gymdemo.exception.ResourceNotFoundException;
import com.example.gymdemo.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final PasswordEncoder passwordEncoder;

    public TrainerService(TrainerRepository trainerRepository,
                          UserRepository userRepository,
                          MemberRepository memberRepository,
                          WorkoutPlanRepository workoutPlanRepository,
                          PasswordEncoder passwordEncoder) {
        this.trainerRepository = trainerRepository;
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.workoutPlanRepository = workoutPlanRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Trainer> getAllTrainers() {
        return trainerRepository.findByActiveTrue();
    }

    public Trainer getTrainerById(Long id) {
        return trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", id));
    }

    public Trainer getTrainerByUserId(Long userId) {
        return trainerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer for user", userId));
    }

    @Transactional
    public Trainer createTrainer(TrainerRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(Role.TRAINER)
                .enabled(true)
                .build();
        user = userRepository.save(user);

        Trainer trainer = Trainer.builder()
                .user(user)
                .specialization(request.getSpecialization())
                .experience(request.getExperience())
                .bio(request.getBio())
                .active(true)
                .build();
        return trainerRepository.save(trainer);
    }

    @Transactional
    public Trainer updateTrainer(Long id, TrainerRequest request) {
        Trainer trainer = getTrainerById(id);
        User user = trainer.getUser();

        if (request.getName() != null) user.setName(request.getName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getEmail() != null) {
            if (!user.getEmail().equals(request.getEmail()) &&
                    userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getSpecialization() != null) trainer.setSpecialization(request.getSpecialization());
        if (request.getExperience() != null) trainer.setExperience(request.getExperience());
        if (request.getBio() != null) trainer.setBio(request.getBio());

        userRepository.save(user);
        return trainerRepository.save(trainer);
    }

    @Transactional
    public void deleteTrainer(Long id) {
        Trainer trainer = getTrainerById(id);
        User user = trainer.getUser();
        user.setEnabled(false);
        trainer.setActive(false);
        userRepository.save(user);
        trainerRepository.save(trainer);
    }

    public List<Member> getAssignedMembers(Long trainerId) {
        return memberRepository.findActiveByTrainerId(trainerId);
    }

    @Transactional
    public WorkoutPlan createWorkoutPlan(WorkoutPlanRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member", request.getMemberId()));
        Trainer trainer = member.getAssignedTrainer();
        if (trainer == null) {
            throw new BadRequestException("Member has no assigned trainer");
        }

        WorkoutPlan plan = WorkoutPlan.builder()
                .member(member)
                .trainer(trainer)
                .title(request.getTitle())
                .description(request.getDescription())
                .exercises(request.getExercises())
                .difficulty(request.getDifficulty())
                .durationWeeks(request.getDurationWeeks())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .notes(request.getNotes())
                .build();
        return workoutPlanRepository.save(plan);
    }

    @Transactional
    public WorkoutPlan updateWorkoutPlan(Long id, WorkoutPlanRequest request) {
        WorkoutPlan plan = workoutPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutPlan", id));

        if (request.getTitle() != null) plan.setTitle(request.getTitle());
        if (request.getDescription() != null) plan.setDescription(request.getDescription());
        if (request.getExercises() != null) plan.setExercises(request.getExercises());
        if (request.getDifficulty() != null) plan.setDifficulty(request.getDifficulty());
        if (request.getDurationWeeks() != null) plan.setDurationWeeks(request.getDurationWeeks());
        if (request.getStartDate() != null) plan.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) plan.setEndDate(request.getEndDate());
        if (request.getStatus() != null) plan.setStatus(request.getStatus());
        if (request.getNotes() != null) plan.setNotes(request.getNotes());

        return workoutPlanRepository.save(plan);
    }

    public List<WorkoutPlan> getWorkoutPlansForTrainer(Long trainerId) {
        return workoutPlanRepository.findByTrainerIdOrderByCreatedAtDesc(trainerId);
    }

    public List<Object[]> getTrainerMemberCount() {
        return trainerRepository.findTrainerMemberCount();
    }
}

