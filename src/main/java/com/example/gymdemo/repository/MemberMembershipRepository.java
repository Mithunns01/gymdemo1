package com.example.gymdemo.repository;

import com.example.gymdemo.entity.MemberMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberMembershipRepository extends JpaRepository<MemberMembership, Long> {
    List<MemberMembership> findByMemberId(Long memberId);
    Optional<MemberMembership> findTopByMemberIdAndActiveTrueOrderByEndDateDesc(Long memberId);

    @Query("SELECT mm FROM MemberMembership mm WHERE mm.active = true AND mm.endDate < :date")
    List<MemberMembership> findExpiredMemberships(@Param("date") LocalDate date);

    @Query("SELECT mm FROM MemberMembership mm WHERE mm.active = true AND mm.endDate BETWEEN :start AND :end")
    List<MemberMembership> findMembershipsExpiringBetween(@Param("start") LocalDate start,
                                                           @Param("end") LocalDate end);

    @Query("SELECT COUNT(mm) FROM MemberMembership mm WHERE mm.active = true AND mm.endDate >= :date")
    long countActiveMemberships(@Param("date") LocalDate date);

    @Query("SELECT COUNT(mm) FROM MemberMembership mm WHERE mm.active = true AND mm.endDate < :date")
    long countExpiredMemberships(@Param("date") LocalDate date);

    @Query("SELECT COUNT(mm) FROM MemberMembership mm WHERE mm.active = true AND " +
           "mm.endDate BETWEEN :start AND :end")
    long countMembershipsExpiringBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
