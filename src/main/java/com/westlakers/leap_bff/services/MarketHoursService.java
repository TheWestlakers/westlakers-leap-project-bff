package com.westlakers.leap_bff.services;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.westlakers.leap_bff.entities.Market;
import com.westlakers.leap_bff.exceptions.ApiException;
import com.westlakers.leap_bff.exceptions.ErrorCode;
import com.westlakers.leap_bff.mappers.MarketMapper;

@Service
public class MarketHoursService {

    private static final String CRYPTO_MARKET_CODE = "CRYPTO";
    private static final String FOREX_MARKET_CODE = "FOREX";
    private static final String FX_MARKET_CODE = "FX";
    private static final long CRYPTO_ASSET_CLASS_ID = 3L;
    private static final long FOREX_ASSET_CLASS_ID = 2L;
    private static final LocalTime FOREX_OPEN_TIME = LocalTime.of(17, 0);
    private static final LocalTime FOREX_CLOSE_TIME = LocalTime.of(17, 0);
    private static final ZoneId FOREX_TIME_ZONE = ZoneId.of("America/New_York");

    private final MarketMapper marketMapper;
    private final Clock clock;

    @Autowired
    public MarketHoursService(MarketMapper marketMapper) {
        this(marketMapper, Clock.systemUTC());
    }

    MarketHoursService(MarketMapper marketMapper, Clock clock) {
        this.marketMapper = marketMapper;
        this.clock = clock;
    }

    public void requireOpen(Long marketId) {
        requireOpen(marketId, null);
    }

    public void requireOpen(Long marketId, Long assetClassId) {
        Market market = marketMapper.findById(marketId);
        if (market == null) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Market not found with id: " + marketId);
        }

        if (!isOpen(market, assetClassId)) {
            throw new ApiException(ErrorCode.MARKET_CLOSED,
                    String.format("Market %s is closed", market.getMarketCode()));
        }
    }

    boolean isOpen(Market market) {
        return isOpen(market, null);
    }

    boolean isOpen(Market market, Long assetClassId) {
        if (CRYPTO_MARKET_CODE.equalsIgnoreCase(market.getMarketCode())
                || Long.valueOf(CRYPTO_ASSET_CLASS_ID).equals(assetClassId)) {
            return true;
        }

        boolean forexMarket = isForexMarket(market, assetClassId);
        if (market.getTimezone() == null && !forexMarket) {
            return false;
        }

        ZoneId marketTimeZone = forexMarket
                ? FOREX_TIME_ZONE
                : ZoneId.of(market.getTimezone());
        LocalDateTime localNow = LocalDateTime.now(clock.withZone(marketTimeZone));
        DayOfWeek day = localNow.getDayOfWeek();
        LocalTime time = localNow.toLocalTime();

        if (forexMarket) {
            return isForexOpen(day, time);
        }

        if (market.getOpenTime() == null || market.getCloseTime() == null) {
            return false;
        }

        boolean weekday = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
        return weekday
                && !time.isBefore(market.getOpenTime())
                && time.isBefore(market.getCloseTime());
    }

    private boolean isForexMarket(Market market, Long assetClassId) {
        return FOREX_MARKET_CODE.equalsIgnoreCase(market.getMarketCode())
                || FX_MARKET_CODE.equalsIgnoreCase(market.getMarketCode())
                || Long.valueOf(FOREX_ASSET_CLASS_ID).equals(assetClassId);
    }

    private boolean isForexOpen(DayOfWeek day, LocalTime time) {
        if (day == DayOfWeek.SATURDAY) {
            return false;
        }
        if (day == DayOfWeek.SUNDAY) {
            return !time.isBefore(FOREX_OPEN_TIME);
        }
        if (day == DayOfWeek.FRIDAY) {
            return time.isBefore(FOREX_CLOSE_TIME);
        }
        return true;
    }
}