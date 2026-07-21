package com.example.gymdemo.repository;

import com.example.gymdemo.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByMemberIdOrderByPaymentDateDesc(Long memberId);
    List<Payment> findByPaymentDateBetweenOrderByPaymentDate(LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentDate BETWEEN :start AND :end")
    BigDecimal getRevenueBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT FUNCTION('MONTH', p.paymentDate), FUNCTION('YEAR', p.paymentDate), " +
           "COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.paymentDate BETWEEN :start AND :end " +
           "GROUP BY FUNCTION('YEAR', p.paymentDate), FUNCTION('MONTH', p.paymentDate) " +
           "ORDER BY FUNCTION('YEAR', p.paymentDate), FUNCTION('MONTH', p.paymentDate)")
    List<Object[]> getMonthlyRevenueReport(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
