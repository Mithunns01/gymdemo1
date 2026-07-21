package com.example.gymdemo.repository;

import com.example.gymdemo.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    Optional<Trainer> findByUserId(Long userId);
    List<Trainer> findByActiveTrue();
    long countByActiveTrue();

    @Query("SELECT t.id, t.user.name, COUNT(m.id) FROM Trainer t " +
           "LEFT JOIN Member m ON m.assignedTrainer.id = t.id " +
           "GROUP BY t.id, t.user.name")
    List<Object[]> findTrainerMemberCount();
}
