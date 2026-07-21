package com.example.gymdemo.repository;

import com.example.gymdemo.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByMemberIdAndAttendanceDate(Long memberId, LocalDate date);
    List<Attendance> findByMemberIdOrderByAttendanceDateDesc(Long memberId);
    List<Attendance> findByAttendanceDateOrderByCheckInTime(LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.attendanceDate = :date")
    long countByAttendanceDate(@Param("date") LocalDate date);

    @Query("SELECT a.member.user.name, a.member.user.email, a.checkInTime FROM Attendance a " +
           "WHERE a.attendanceDate = :date ORDER BY a.checkInTime")
    List<Object[]> findDailyAttendanceReport(@Param("date") LocalDate date);

    @Query("SELECT a.member.id, a.member.user.name, COUNT(a) as cnt FROM Attendance a " +
           "WHERE a.attendanceDate BETWEEN :start AND :end GROUP BY a.member.id, a.member.user.name " +
           "ORDER BY cnt DESC")
    List<Object[]> findTopActiveMembers(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
