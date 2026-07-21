package com.example.gymdemo.repository;

import com.example.gymdemo.entity.WorkoutPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {
    List<WorkoutPlan> findByMemberIdOrderByCreatedAtDesc(Long memberId);
    List<WorkoutPlan> findByTrainerIdOrderByCreatedAtDesc(Long trainerId);
    List<WorkoutPlan> findByMemberIdAndStatusOrderByCreatedAtDesc(Long memberId, String status);
}
