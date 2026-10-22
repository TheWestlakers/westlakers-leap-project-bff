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

    @Test
    void requireOpen_acceptsCryptoMarketOutsideWeekdaySession() {
        market = new Market(2L, "Crypto", "CRYPTO", "UTC",
                LocalTime.MIDNIGHT, LocalTime.MIDNIGHT);
        when(marketMapper.findById(2L)).thenReturn(market);
        MarketHoursService service = serviceAt("2026-10-17T15:00:00Z");

        assertDoesNotThrow(() -> service.requireOpen(2L));
    }

    @Test
    void requireOpen_acceptsCryptoAssetMappedToStockMarket() {
        MarketHoursService service = serviceAt("2026-10-17T15:00:00Z");

        assertDoesNotThrow(() -> service.requireOpen(1L, 3L));
    }

        @Test
        void requireOpen_acceptsForexFromSundayOpenThroughThursday() {
        market = new Market(5L, "Foreign Exchange Market", "FOREX",
            "America/New_York", LocalTime.of(17, 0), LocalTime.of(17, 0));
        when(marketMapper.findById(5L)).thenReturn(market);

        assertDoesNotThrow(() -> serviceAt("2026-10-18T21:00:00Z").requireOpen(5L));
        assertDoesNotThrow(() -> serviceAt("2026-10-22T20:00:00Z").requireOpen(5L));
        }

        @Test
        void requireOpen_rejectsForexBeforeSundayOpenAndAtFridayClose() {
        market = new Market(5L, "Foreign Exchange Market", "FOREX",
                "UTC", null, null);
        when(marketMapper.findById(5L)).thenReturn(market);

        assertThrows(ApiException.class,
            () -> serviceAt("2026-10-18T20:59:59Z").requireOpen(5L));
        assertThrows(ApiException.class,
            () -> serviceAt("2026-10-23T21:00:00Z").requireOpen(5L));
        assertThrows(ApiException.class,
            () -> serviceAt("2026-10-24T15:00:00Z").requireOpen(5L));
        }

    private MarketHoursService serviceAt(String instant) {
        Clock clock = Clock.fixed(Instant.parse(instant), ZoneOffset.UTC);
        return new MarketHoursService(marketMapper, clock);
    }
}