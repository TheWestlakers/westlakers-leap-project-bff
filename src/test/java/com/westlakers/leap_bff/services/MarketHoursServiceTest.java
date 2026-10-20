package com.westlakers.leap_bff.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.westlakers.leap_bff.entities.Market;
import com.westlakers.leap_bff.exceptions.ApiException;
import com.westlakers.leap_bff.exceptions.ErrorCode;
import com.westlakers.leap_bff.mappers.MarketMapper;

class MarketHoursServiceTest {

    @Mock
    private MarketMapper marketMapper;

    private Market market;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        market = new Market(1L, "New York Stock Exchange", "NYSE",
                "America/New_York", LocalTime.of(9, 30), LocalTime.of(16, 0));
        when(marketMapper.findById(1L)).thenReturn(market);
    }

    @Test
    void requireOpen_acceptsOpeningTimeInMarketTimezone() {
        MarketHoursService service = serviceAt("2026-10-19T13:30:00Z");

        assertDoesNotThrow(() -> service.requireOpen(1L));
    }

    @Test
    void requireOpen_rejectsClosingTime() {
        MarketHoursService service = serviceAt("2026-10-19T20:00:00Z");

        ApiException exception = assertThrows(ApiException.class, () -> service.requireOpen(1L));

        assertEquals(ErrorCode.MARKET_CLOSED, exception.getErrorCode());
    }

    @Test
    void requireOpen_rejectsWeekend() {
        MarketHoursService service = serviceAt("2026-10-17T15:00:00Z");

        ApiException exception = assertThrows(ApiException.class, () -> service.requireOpen(1L));

        assertEquals(ErrorCode.MARKET_CLOSED, exception.getErrorCode());
    }

    private MarketHoursService serviceAt(String instant) {
        Clock clock = Clock.fixed(Instant.parse(instant), ZoneOffset.UTC);
        return new MarketHoursService(marketMapper, clock);
    }
}