package com.westlakers.leap_bff.services;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.westlakers.leap_bff.dtos.TopInstrumentDTO;
import com.westlakers.leap_bff.dtos.TopInvestorDTO;
import com.westlakers.leap_bff.exceptions.ApiException;
import com.westlakers.leap_bff.exceptions.ErrorCode;
import com.westlakers.leap_bff.repositories.TopAnalyticsRepository;

@Service
public class TopAnalyticsService {

    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 100;

    private final TopAnalyticsRepository topAnalyticsRepository;

    public TopAnalyticsService(TopAnalyticsRepository topAnalyticsRepository) {
        this.topAnalyticsRepository = topAnalyticsRepository;
    }

    public List<TopInvestorDTO> getTopInvestors(int limit) {
        validateLimit(limit);

        try {
            return topAnalyticsRepository.findTopInvestors(limit);
        } catch (DataAccessException exception) {
            throw databaseException("Failed to retrieve top investors", exception);
        }
    }

    public List<TopInstrumentDTO> getTopInstruments(int limit) {
        validateLimit(limit);

        try {
            return topAnalyticsRepository.findTopInstruments(limit);
        } catch (DataAccessException exception) {
            throw databaseException("Failed to retrieve top instruments", exception);
        }
    }

    private void validateLimit(int limit) {
        if (limit < MIN_LIMIT || limit > MAX_LIMIT) {
            throw new ApiException(
                    ErrorCode.INVALID_INPUT,
                    "Limit must be between " + MIN_LIMIT + " and " + MAX_LIMIT);
        }
    }

    private ApiException databaseException(String operation, DataAccessException exception) {
        return new ApiException(ErrorCode.DATABASE_ERROR, operation + ": " + exception.getMostSpecificCause().getMessage());
    }
}