package com.example.gymdemo.repository;

import com.example.gymdemo.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUserId(Long userId);
    List<Member> findByAssignedTrainerId(Long trainerId);
    List<Member> findByActiveTrue();

    @Query("SELECT m FROM Member m WHERE m.active = true AND m.assignedTrainer.id = :trainerId")
    List<Member> findActiveByTrainerId(@Param("trainerId") Long trainerId);

    @Query("SELECT m FROM Member m WHERE LOWER(m.user.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(m.user.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(m.user.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Member> searchMembers(@Param("keyword") String keyword);

    long countByActiveTrue();
}
