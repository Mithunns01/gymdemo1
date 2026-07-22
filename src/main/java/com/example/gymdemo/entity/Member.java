package com.example.gymdemo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 20)
    private String gender;

    private LocalDate dateOfBirth;

    @Column(precision = 5, scale = 2)
    private BigDecimal height;

    @Column(precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(precision = 5, scale = 2)
    private BigDecimal bmi;

    @Column(length = 20)
    private String emergencyContact;

    @Column(columnDefinition = "TEXT")
    private String medicalConditions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_trainer_id")
    private Trainer assignedTrainer;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}

