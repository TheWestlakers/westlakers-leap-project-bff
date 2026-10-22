package com.westlakers.leap_bff.services;

import java.util.List;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.westlakers.leap_bff.dtos.RankingPageDTO;
import com.westlakers.leap_bff.dtos.TopInstrumentDTO;
import com.westlakers.leap_bff.dtos.TopInvestorDTO;
import com.westlakers.leap_bff.exceptions.ApiException;
import com.westlakers.leap_bff.exceptions.ErrorCode;
import com.westlakers.leap_bff.repositories.TopAnalyticsRepository;

@Service
public class TopAnalyticsService {

    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 100;

    private final TopAnalyticsRepository topAnalyticsRepository;

    public TopAnalyticsService(TopAnalyticsRepository topAnalyticsRepository) {
        this.topAnalyticsRepository = topAnalyticsRepository;
    }

    public RankingPageDTO<TopInvestorDTO> getInvestorRankings(int page, int size) {
        validatePage(page, size);

        try {
            return toPage(page, size, topAnalyticsRepository::countInvestors,
                    () -> topAnalyticsRepository.findTopInvestors(size, page * size));
        } catch (DataAccessException exception) {
            throw databaseException("Failed to retrieve investor rankings", exception);
        }
    }

    public RankingPageDTO<TopInstrumentDTO> getInstrumentRankings(int page, int size) {
        validatePage(page, size);

        try {
            return toPage(page, size, topAnalyticsRepository::countInstruments,
                    () -> topAnalyticsRepository.findTopInstruments(size, page * size));
        } catch (DataAccessException exception) {
            throw databaseException("Failed to retrieve instrument rankings", exception);
        }
    }

    private <T> RankingPageDTO<T> toPage(int page, int size, LongSupplier count, Supplier<List<T>> content) {
        long totalElements = count.getAsLong();
        int totalPages = (int) ((totalElements + size - 1) / size);
        List<T> rows = (long) page * size >= totalElements ? List.of() : content.get();

        return new RankingPageDTO<>(rows, page, size, totalElements, totalPages);
    }

    private void validatePage(int page, int size) {
        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw new ApiException(
                    ErrorCode.INVALID_INPUT,
                    "Size must be between " + MIN_SIZE + " and " + MAX_SIZE);
        }

        if (page < 0) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Page must not be negative");
        }
    }

    private ApiException databaseException(String operation, DataAccessException exception) {
        return new ApiException(ErrorCode.DATABASE_ERROR, operation + ": " + exception.getMostSpecificCause().getMessage());
    }
}
