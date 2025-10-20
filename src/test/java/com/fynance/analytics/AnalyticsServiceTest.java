package com.fynance.analytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Map;

class AnalyticsServiceTest {

    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService();
    }

    @Test
    void testGetExpensesByCategory() {
        // This is a simple integration test, assuming there's data in the database
        Map<String, Double> result = analyticsService.getExpensesByCategory(1, LocalDate.now().withDayOfMonth(1), LocalDate.now());
        assertNotNull(result);
    }

    @Test
    void testGetMonthlyExpenseSummary() {
        Map<String, Double> result = analyticsService.getMonthlyExpenseSummary(1, 6);
        assertNotNull(result);
    }
}
