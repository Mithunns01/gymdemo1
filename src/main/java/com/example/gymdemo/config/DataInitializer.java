package com.example.gymdemo.config;

import com.example.gymdemo.entity.*;
import com.example.gymdemo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final TrainerRepository trainerRepository;
    private final MemberRepository memberRepository;
    private final MembershipPlanRepository planRepository;
    private final MemberMembershipRepository membershipRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           TrainerRepository trainerRepository,
                           MemberRepository memberRepository,
                           MembershipPlanRepository planRepository,
                           MemberMembershipRepository membershipRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.trainerRepository = trainerRepository;
        this.memberRepository = memberRepository;
        this.planRepository = planRepository;
        this.membershipRepository = membershipRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            logger.info("Database already initialized. Skipping data initialization.");
            return;
        }

        logger.info("Initializing database with default data...");

        // Create Admin
        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .name("System Admin")
                .email("admin@gymdemo.com")
                .phone("9876543210")
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(admin);
        logger.info("Admin user created: admin / admin123");

        // Create Plans
        MembershipPlan basic = MembershipPlan.builder()
                .name("Basic Monthly")
                .description("Basic gym access for 1 month")
                .durationDays(30)
                .price(new BigDecimal("999.00"))
                .active(true)
                .build();
        planRepository.save(basic);

        MembershipPlan standard = MembershipPlan.builder()
                .name("Standard Quarterly")
                .description("Standard gym access for 3 months")
                .durationDays(90)
                .price(new BigDecimal("2499.00"))
                .active(true)
                .build();
        planRepository.save(standard);

        MembershipPlan premium = MembershipPlan.builder()
                .name("Premium Yearly")
                .description("Premium gym access with personal trainer for 12 months")
                .durationDays(365)
                .price(new BigDecimal("7999.00"))
                .active(true)
                .build();
        planRepository.save(premium);
        logger.info("Membership plans created");

        // Create Trainers
        User trainerUser1 = User.builder()
                .username("trainer1")
                .password(passwordEncoder.encode("trainer123"))
                .name("Rajesh Kumar")
                .email("rajesh@gymdemo.com")
                .phone("9876543211")
                .role(Role.TRAINER)
                .enabled(true)
                .build();
        userRepository.save(trainerUser1);

        Trainer trainer1 = Trainer.builder()
                .user(trainerUser1)
                .specialization("Strength Training & Bodybuilding")
                .experience(8)
                .bio("Certified strength and conditioning specialist with 8 years experience")
                .active(true)
                .build();
        trainerRepository.save(trainer1);

        User trainerUser2 = User.builder()
                .username("trainer2")
                .password(passwordEncoder.encode("trainer123"))
                .name("Priya Sharma")
                .email("priya@gymdemo.com")
                .phone("9876543212")
                .role(Role.TRAINER)
                .enabled(true)
                .build();
        userRepository.save(trainerUser2);

        Trainer trainer2 = Trainer.builder()
                .user(trainerUser2)
                .specialization("Yoga & Flexibility")
                .experience(5)
                .bio("Certified yoga instructor with 5 years of teaching experience")
                .active(true)
                .build();
        trainerRepository.save(trainer2);
        logger.info("Trainers created: trainer1 / trainer123, trainer2 / trainer123");

        // Create Members
        User memberUser1 = User.builder()
                .username("member1")
                .password(passwordEncoder.encode("member123"))
                .name("Amit Patel")
                .email("amit@gymdemo.com")
                .phone("9876543213")
                .role(Role.MEMBER)
                .enabled(true)
                .build();
        userRepository.save(memberUser1);

        Member member1 = Member.builder()
                .user(memberUser1)
                .gender("Male")
                .dateOfBirth(LocalDate.of(1995, 5, 15))
                .height(new BigDecimal("175"))
                .weight(new BigDecimal("78"))
                .emergencyContact("9876543214")
                .assignedTrainer(trainer1)
                .active(true)
                .build();
        memberRepository.save(member1);

        User memberUser2 = User.builder()
                .username("member2")
                .password(passwordEncoder.encode("member123"))
                .name("Sneha Reddy")
                .email("sneha@gymdemo.com")
                .phone("9876543215")
                .role(Role.MEMBER)
                .enabled(true)
                .build();
        userRepository.save(memberUser2);

        Member member2 = Member.builder()
                .user(memberUser2)
                .gender("Female")
                .dateOfBirth(LocalDate.of(1998, 8, 22))
                .height(new BigDecimal("162"))
                .weight(new BigDecimal("58"))
                .emergencyContact("9876543216")
                .assignedTrainer(trainer2)
                .active(true)
                .build();
        memberRepository.save(member2);
        logger.info("Members created: member1 / member123, member2 / member123");

        // Create active memberships
        MemberMembership m1 = MemberMembership.builder()
                .member(member1)
                .plan(premium)
                .startDate(LocalDate.now().minusMonths(2))
                .endDate(LocalDate.now().plusMonths(10))
                .active(true)
                .amountPaid(premium.getPrice())
                .paymentStatus("COMPLETED")
                .build();
        membershipRepository.save(m1);

        MemberMembership m2 = MemberMembership.builder()
                .member(member2)
                .plan(standard)
                .startDate(LocalDate.now().minusMonths(1))
                .endDate(LocalDate.now().plusMonths(2))
                .active(true)
                .amountPaid(standard.getPrice())
                .paymentStatus("COMPLETED")
                .build();
        membershipRepository.save(m2);
        logger.info("Memberships created");

        logger.info("Database initialization completed!");
    }
}

