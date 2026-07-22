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
import java.time.LocalTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final TrainerRepository trainerRepository;
    private final MemberRepository memberRepository;
    private final MembershipPlanRepository planRepository;
    private final MemberMembershipRepository membershipRepository;
    private final AttendanceRepository attendanceRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           TrainerRepository trainerRepository,
                           MemberRepository memberRepository,
                           MembershipPlanRepository planRepository,
                           MemberMembershipRepository membershipRepository,
                           AttendanceRepository attendanceRepository,
                           WorkoutPlanRepository workoutPlanRepository,
                           PaymentRepository paymentRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.trainerRepository = trainerRepository;
        this.memberRepository = memberRepository;
        this.planRepository = planRepository;
        this.membershipRepository = membershipRepository;
        this.attendanceRepository = attendanceRepository;
        this.workoutPlanRepository = workoutPlanRepository;
        this.paymentRepository = paymentRepository;
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

        // ========== Create 3 Additional Members ==========

        // Member 3 - Vikram Singh
        User memberUser3 = User.builder()
                .username("member3")
                .password(passwordEncoder.encode("member123"))
                .name("Vikram Singh")
                .email("vikram@gymdemo.com")
                .phone("9876543217")
                .role(Role.MEMBER)
                .enabled(true)
                .build();
        userRepository.save(memberUser3);

        Member member3 = Member.builder()
                .user(memberUser3)
                .gender("Male")
                .dateOfBirth(LocalDate.of(1990, 11, 3))
                .height(new BigDecimal("180"))
                .weight(new BigDecimal("85"))
                .emergencyContact("9876543218")
                .assignedTrainer(trainer1)
                .active(true)
                .build();
        memberRepository.save(member3);

        // Member 4 - Ananya Gupta
        User memberUser4 = User.builder()
                .username("member4")
                .password(passwordEncoder.encode("member123"))
                .name("Ananya Gupta")
                .email("ananya@gymdemo.com")
                .phone("9876543219")
                .role(Role.MEMBER)
                .enabled(true)
                .build();
        userRepository.save(memberUser4);

        Member member4 = Member.builder()
                .user(memberUser4)
                .gender("Female")
                .dateOfBirth(LocalDate.of(2000, 2, 14))
                .height(new BigDecimal("158"))
                .weight(new BigDecimal("52"))
                .emergencyContact("9876543220")
                .assignedTrainer(trainer2)
                .active(true)
                .build();
        memberRepository.save(member4);

        // Member 5 - Rohan Deshmukh
        User memberUser5 = User.builder()
                .username("member5")
                .password(passwordEncoder.encode("member123"))
                .name("Rohan Deshmukh")
                .email("rohan@gymdemo.com")
                .phone("9876543221")
                .role(Role.MEMBER)
                .enabled(true)
                .build();
        userRepository.save(memberUser5);

        Member member5 = Member.builder()
                .user(memberUser5)
                .gender("Male")
                .dateOfBirth(LocalDate.of(1988, 7, 9))
                .height(new BigDecimal("170"))
                .weight(new BigDecimal("92"))
                .emergencyContact("9876543222")
                .assignedTrainer(trainer1)
                .active(true)
                .build();
        memberRepository.save(member5);
        logger.info("Additional members created: member3, member4, member5 / member123");

        // ========== Create Memberships for New Members ==========

        MemberMembership m3 = MemberMembership.builder()
                .member(member3)
                .plan(basic)
                .startDate(LocalDate.now().minusMonths(3))
                .endDate(LocalDate.now().minusDays(5))
                .active(false)
                .amountPaid(basic.getPrice())
                .paymentStatus("COMPLETED")
                .build();
        membershipRepository.save(m3);

        // Renewal for member3 - upgraded to standard
        MemberMembership m3Renewal = MemberMembership.builder()
                .member(member3)
                .plan(standard)
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusMonths(2).minusDays(5))
                .active(true)
                .amountPaid(standard.getPrice())
                .paymentStatus("COMPLETED")
                .build();
        membershipRepository.save(m3Renewal);

        MemberMembership m4 = MemberMembership.builder()
                .member(member4)
                .plan(basic)
                .startDate(LocalDate.now().minusMonths(1))
                .endDate(LocalDate.now().plusMonths(1).minusDays(5))
                .active(true)
                .amountPaid(basic.getPrice())
                .paymentStatus("COMPLETED")
                .build();
        membershipRepository.save(m4);

        MemberMembership m5 = MemberMembership.builder()
                .member(member5)
                .plan(premium)
                .startDate(LocalDate.now().minusMonths(6))
                .endDate(LocalDate.now().plusMonths(6))
                .active(true)
                .amountPaid(premium.getPrice())
                .paymentStatus("COMPLETED")
                .build();
        membershipRepository.save(m5);
        logger.info("Additional memberships created with renewal");

        // ========== Create Workout Plans ==========

        WorkoutPlan wp1 = WorkoutPlan.builder()
                .member(member1)
                .trainer(trainer1)
                .title("Strength Building - Beginner")
                .description("Full body strength training program for beginners")
                .exercises("Squats: 3x12, Bench Press: 3x10, Deadlifts: 3x8, Push-ups: 3x15, Pull-ups: 3x8")
                .difficulty("BEGINNER")
                .durationWeeks(8)
                .startDate(LocalDate.now().minusMonths(2))
                .endDate(LocalDate.now().plusMonths(6))
                .status("ACTIVE")
                .build();
        workoutPlanRepository.save(wp1);

        WorkoutPlan wp2 = WorkoutPlan.builder()
                .member(member2)
                .trainer(trainer2)
                .title("Yoga & Flexibility")
                .description("Morning yoga routine for flexibility and mindfulness")
                .exercises("Sun Salutations: 10 rounds, Warrior poses: 5 min, Tree Pose: 3 min each side, Savasana: 10 min")
                .difficulty("BEGINNER")
                .durationWeeks(12)
                .startDate(LocalDate.now().minusMonths(1))
                .endDate(LocalDate.now().plusMonths(2))
                .status("ACTIVE")
                .build();
        workoutPlanRepository.save(wp2);

        WorkoutPlan wp3 = WorkoutPlan.builder()
                .member(member3)
                .trainer(trainer1)
                .title("Weight Loss Program")
                .description("High intensity interval training for fat loss")
                .exercises("Burpees: 3x15, Mountain Climbers: 3x30s, Jump Squats: 3x12, Plank: 3x45s, Box Jumps: 3x10")
                .difficulty("INTERMEDIATE")
                .durationWeeks(6)
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusWeeks(6).minusDays(5))
                .status("ACTIVE")
                .build();
        workoutPlanRepository.save(wp3);

        WorkoutPlan wp4 = WorkoutPlan.builder()
                .member(member4)
                .trainer(trainer2)
                .title("Toning & Sculpting")
                .description("Light weight training for muscle toning")
                .exercises("Dumbbell Curls: 3x12, Tricep Dips: 3x10, Lunges: 3x12 each, Leg Press: 3x15, Plank: 3x30s")
                .difficulty("BEGINNER")
                .durationWeeks(8)
                .startDate(LocalDate.now().minusMonths(1))
                .endDate(LocalDate.now().plusWeeks(8).minusMonths(1))
                .status("ACTIVE")
                .build();
        workoutPlanRepository.save(wp4);

        WorkoutPlan wp5 = WorkoutPlan.builder()
                .member(member5)
                .trainer(trainer1)
                .title("Advanced Strength Training")
                .description("Intensive strength and power building program")
                .exercises("Barbell Squats: 5x5, Deadlifts: 5x5, Overhead Press: 4x8, Bent Over Rows: 4x8, Pull-ups: 3x10")
                .difficulty("ADVANCED")
                .durationWeeks(12)
                .startDate(LocalDate.now().minusMonths(6))
                .endDate(LocalDate.now().plusMonths(6))
                .status("ACTIVE")
                .build();
        workoutPlanRepository.save(wp5);
        logger.info("Workout plans created for all members");

        // ========== Create Attendance Records ==========

        // Member 1 - Amit Patel (attendance for last 2 months)
        for (int i = 45; i >= 0; i -= 3) {
            LocalDate d = LocalDate.now().minusDays(i);
            attendanceRepository.save(Attendance.builder()
                    .member(member1)
                    .attendanceDate(d)
                    .checkInTime(LocalTime.of(6, 30))
                    .status("PRESENT")
                    .notes("Morning session")
                    .build());
        }

        // Member 2 - Sneha Reddy (attendance for last month)
        for (int i = 28; i >= 0; i -= 2) {
            LocalDate d = LocalDate.now().minusDays(i);
            attendanceRepository.save(Attendance.builder()
                    .member(member2)
                    .attendanceDate(d)
                    .checkInTime(LocalTime.of(7, 0))
                    .status("PRESENT")
                    .notes("Yoga session")
                    .build());
        }

        // Member 3 - Vikram Singh (attendance for last 3 weeks)
        for (int i = 18; i >= 0; i -= 2) {
            LocalDate d = LocalDate.now().minusDays(i);
            attendanceRepository.save(Attendance.builder()
                    .member(member3)
                    .attendanceDate(d)
                    .checkInTime(LocalTime.of(17, 30))
                    .status("PRESENT")
                    .notes("Evening workout")
                    .build());
        }

        // Member 4 - Ananya Gupta (attendance for last 2 weeks)
        for (int i = 12; i >= 0; i -= 2) {
            LocalDate d = LocalDate.now().minusDays(i);
            attendanceRepository.save(Attendance.builder()
                    .member(member4)
                    .attendanceDate(d)
                    .checkInTime(LocalTime.of(8, 0))
                    .status("PRESENT")
                    .notes("Morning toning session")
                    .build());
        }

        // Member 5 - Rohan Deshmukh (attendance for last 5 weeks)
        for (int i = 30; i >= 0; i -= 3) {
            LocalDate d = LocalDate.now().minusDays(i);
            attendanceRepository.save(Attendance.builder()
                    .member(member5)
                    .attendanceDate(d)
                    .checkInTime(LocalTime.of(18, 0))
                    .status("PRESENT")
                    .notes("Strength training")
                    .build());
        }
        logger.info("Attendance records created for all members");

        // ========== Create Payment Records ==========

        // Member 1 - Amit Patel (Premium Yearly)
        paymentRepository.save(Payment.builder()
                .member(member1)
                .membership(m1)
                .amount(premium.getPrice())
                .paymentDate(LocalDate.now().minusMonths(2))
                .paymentMode("CREDIT_CARD")
                .transactionId("TXN" + System.currentTimeMillis() + "01")
                .status("COMPLETED")
                .notes("Premium yearly membership payment")
                .build());

        // Member 2 - Sneha Reddy (Standard Quarterly)
        paymentRepository.save(Payment.builder()
                .member(member2)
                .membership(m2)
                .amount(standard.getPrice())
                .paymentDate(LocalDate.now().minusMonths(1))
                .paymentMode("UPI")
                .transactionId("TXN" + System.currentTimeMillis() + "02")
                .status("COMPLETED")
                .notes("Standard quarterly membership payment")
                .build());

        // Member 3 - Vikram Singh (Basic - expired, then renewal)
        paymentRepository.save(Payment.builder()
                .member(member3)
                .membership(m3)
                .amount(basic.getPrice())
                .paymentDate(LocalDate.now().minusMonths(3))
                .paymentMode("CASH")
                .transactionId("TXN" + System.currentTimeMillis() + "03")
                .status("COMPLETED")
                .notes("Basic monthly membership payment")
                .build());

        // Member 3 renewal payment
        paymentRepository.save(Payment.builder()
                .member(member3)
                .membership(m3Renewal)
                .amount(standard.getPrice())
                .paymentDate(LocalDate.now().minusDays(5))
                .paymentMode("CREDIT_CARD")
                .transactionId("TXN" + System.currentTimeMillis() + "04")
                .status("COMPLETED")
                .notes("Renewal - upgraded to Standard Quarterly")
                .build());

        // Member 4 - Ananya Gupta (Basic)
        paymentRepository.save(Payment.builder()
                .member(member4)
                .membership(m4)
                .amount(basic.getPrice())
                .paymentDate(LocalDate.now().minusMonths(1))
                .paymentMode("UPI")
                .transactionId("TXN" + System.currentTimeMillis() + "05")
                .status("COMPLETED")
                .notes("Basic monthly membership payment")
                .build());

        // Member 5 - Rohan Deshmukh (Premium Yearly)
        paymentRepository.save(Payment.builder()
                .member(member5)
                .membership(m5)
                .amount(premium.getPrice())
                .paymentDate(LocalDate.now().minusMonths(6))
                .paymentMode("DEBIT_CARD")
                .transactionId("TXN" + System.currentTimeMillis() + "06")
                .status("COMPLETED")
                .notes("Premium yearly membership payment")
                .build());

        logger.info("Payment records created for all members");

        logger.info("Database initialization completed with full seed data!");
    }
}

