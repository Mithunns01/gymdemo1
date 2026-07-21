package com.example.gymdemo.service;

import com.example.gymdemo.dto.BmiRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Test
    void calculateBmi_shouldReturnCorrectValue() {
        BmiRequest request = new BmiRequest();
        request.setHeight(new BigDecimal("175"));
        request.setWeight(new BigDecimal("70"));

        BigDecimal bmi = memberService.calculateBmi(request);
        assertNotNull(bmi);
        assertTrue(bmi.compareTo(BigDecimal.ZERO) > 0);
        assertEquals(22.86, bmi.doubleValue(), 0.1);
    }

    @Test
    void searchMembers_shouldReturnResults() {
        var members = memberService.searchMembers("amit");
        assertNotNull(members);
    }

    @Test
    void getAllMembers_shouldReturnList() {
        var members = memberService.getAllMembers();
        assertNotNull(members);
    }
}

