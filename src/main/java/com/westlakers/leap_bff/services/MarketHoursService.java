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
        Market market = marketMapper.findById(marketId);
        if (market == null) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Market not found with id: " + marketId);
        }

        if (!isOpen(market)) {
            throw new ApiException(ErrorCode.MARKET_CLOSED,
                    String.format("Market %s is closed", market.getMarketCode()));
        }
    }

    boolean isOpen(Market market) {
        if (market.getTimezone() == null || market.getOpenTime() == null || market.getCloseTime() == null) {
            return false;
        }

        LocalDateTime localNow = LocalDateTime.now(clock.withZone(ZoneId.of(market.getTimezone())));
        DayOfWeek day = localNow.getDayOfWeek();
        LocalTime time = localNow.toLocalTime();

        boolean weekday = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
        return weekday
                && !time.isBefore(market.getOpenTime())
                && time.isBefore(market.getCloseTime());
    }
}