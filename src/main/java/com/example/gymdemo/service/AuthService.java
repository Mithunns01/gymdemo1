package com.example.gymdemo.service;

import com.example.gymdemo.dto.LoginRequest;
import com.example.gymdemo.dto.LoginResponse;
import com.example.gymdemo.dto.MemberRequest;
import com.example.gymdemo.entity.*;
import com.example.gymdemo.exception.BadRequestException;
import com.example.gymdemo.repository.*;
import com.example.gymdemo.security.JwtTokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final TrainerRepository trainerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       MemberRepository memberRepository,
                       TrainerRepository trainerRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.trainerRepository = trainerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        if (!user.isEnabled()) {
            throw new BadCredentialsException("Account is disabled");
        }

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().name());
        Long userId = getUserIdByRole(user);

        return new LoginResponse(token, userId, user.getUsername(),
                user.getName(), user.getRole().name(), user.getEmail());
    }

    @Transactional
    public LoginResponse registerMember(MemberRequest request) {
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
                .role(Role.MEMBER)
                .enabled(true)
                .build();
        user = userRepository.save(user);

        Trainer trainer = null;
        if (request.getAssignedTrainerId() != null) {
            trainer = trainerRepository.findById(request.getAssignedTrainerId())
                    .orElseThrow(() -> new BadRequestException("Trainer not found"));
        }

        Member member = Member.builder()
                .user(user)
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .height(request.getHeight())
                .weight(request.getWeight())
                .emergencyContact(request.getEmergencyContact())
                .medicalConditions(request.getMedicalConditions())
                .assignedTrainer(trainer)
                .active(true)
                .build();
        memberRepository.save(member);

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().name());
        return new LoginResponse(token, member.getId(), user.getUsername(),
                user.getName(), user.getRole().name(), user.getEmail());
    }

    private Long getUserIdByRole(User user) {
        switch (user.getRole()) {
            case MEMBER:
                return memberRepository.findByUserId(user.getId())
                        .map(Member::getId)
                        .orElse(null);
            case TRAINER:
                return trainerRepository.findByUserId(user.getId())
                        .map(Trainer::getId)
                        .orElse(null);
            default:
                return user.getId();
        }
    }
}

