package com.westlakers.leap_bff.services;

import com.westlakers.leap_bff.dtos.RankingPageDTO;
import com.westlakers.leap_bff.dtos.AllTimeAnalyticsSummaryDTO;
import com.westlakers.leap_bff.dtos.TopInstrumentDTO;
import com.westlakers.leap_bff.dtos.TopInvestorDTO;
import com.westlakers.leap_bff.exceptions.ApiException;
import com.westlakers.leap_bff.exceptions.ErrorCode;
import com.westlakers.leap_bff.repositories.TopAnalyticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessResourceFailureException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class TopAnalyticsServiceTest {

    @Mock
    private TopAnalyticsRepository topAnalyticsRepository;

    @InjectMocks
    private TopAnalyticsService topAnalyticsService;

    private TopInvestorDTO testInvestor;
    private TopInstrumentDTO testInstrument;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testInvestor = new TopInvestorDTO(1L, "Ada", "Lovelace", "RETAIL", 10L,
                new BigDecimal("100"), new BigDecimal("5000.00"));
        testInstrument = new TopInstrumentDTO(1, "AAPL", "Apple Inc.", "EQUITY", 10L,
                new BigDecimal("100"), new BigDecimal("5000.00"));
    }

    @Test
    void testGetInvestorRankings_UsesOffsetAndTotals() {
        when(topAnalyticsRepository.countInvestors()).thenReturn(45L);
        when(topAnalyticsRepository.findTopInvestors(20, 40)).thenReturn(List.of(testInvestor));

        RankingPageDTO<TopInvestorDTO> result = topAnalyticsService.getInvestorRankings(2, 20);

        assertEquals(1, result.getContent().size());
        assertEquals(2, result.getPage());
        assertEquals(20, result.getSize());
        assertEquals(45L, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        verify(topAnalyticsRepository).findTopInvestors(20, 40);
    }

    @Test
    void testGetInstrumentRankings_UsesOffsetAndTotals() {
        when(topAnalyticsRepository.countInstruments()).thenReturn(20L);
        when(topAnalyticsRepository.findTopInstruments(10, 10)).thenReturn(List.of(testInstrument));

        RankingPageDTO<TopInstrumentDTO> result = topAnalyticsService.getInstrumentRankings(1, 10);

        assertEquals(List.of(testInstrument), result.getContent());
        assertEquals(20L, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
    }

    @Test
    void testGetInvestorRankings_PageBeyondEnd_SkipsQuery() {
        when(topAnalyticsRepository.countInvestors()).thenReturn(5L);

        RankingPageDTO<TopInvestorDTO> result = topAnalyticsService.getInvestorRankings(3, 20);

        assertTrue(result.getContent().isEmpty());
        assertEquals(1, result.getTotalPages());
        verify(topAnalyticsRepository, never()).findTopInvestors(anyInt(), anyInt());
    }

    @Test
    void testGetInvestorRankings_InvalidInput() {
        ApiException negativePage = assertThrows(ApiException.class,
                () -> topAnalyticsService.getInvestorRankings(-1, 20));
        ApiException oversized = assertThrows(ApiException.class,
                () -> topAnalyticsService.getInvestorRankings(0, 101));

        assertEquals(ErrorCode.INVALID_INPUT, negativePage.getErrorCode());
        assertEquals(ErrorCode.INVALID_INPUT, oversized.getErrorCode());
        verifyNoInteractions(topAnalyticsRepository);
    }

    @Test
    void testGetInstrumentRankings_DatabaseError() {
        when(topAnalyticsRepository.countInstruments())
                .thenThrow(new DataAccessResourceFailureException("connection refused"));

        ApiException exception = assertThrows(ApiException.class,
                () -> topAnalyticsService.getInstrumentRankings(0, 20));

        assertEquals(ErrorCode.DATABASE_ERROR, exception.getErrorCode());
    }

    @Test
    void testGetAllTimeSummary_ReturnsRepositorySummary() {
        AllTimeAnalyticsSummaryDTO summary = new AllTimeAnalyticsSummaryDTO(
                new BigDecimal("25.50"), 12L, new BigDecimal("3.25"), 8L, new BigDecimal("125000.00"));
        when(topAnalyticsRepository.getAllTimeSummary()).thenReturn(summary);

        AllTimeAnalyticsSummaryDTO result = topAnalyticsService.getAllTimeSummary();

        assertSame(summary, result);
        verify(topAnalyticsRepository).getAllTimeSummary();
    }

    @Test
    void testGetAllTimeSummary_DatabaseError() {
        when(topAnalyticsRepository.getAllTimeSummary())
                .thenThrow(new DataAccessResourceFailureException("connection refused"));

        ApiException exception = assertThrows(ApiException.class,
                () -> topAnalyticsService.getAllTimeSummary());

        assertEquals(ErrorCode.DATABASE_ERROR, exception.getErrorCode());
    }
}
