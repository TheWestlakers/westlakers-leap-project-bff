package com.westlakers.leap_bff.services;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.westlakers.leap_bff.exceptions.ApiException;
import com.westlakers.leap_bff.exceptions.ErrorCode;
import com.westlakers.leap_bff.mappers.AccountMapper;
import com.westlakers.leap_bff.mappers.WatchlistMapper;

@Service
public class WatchlistService {

    private final AccountMapper accountMapper;
    private final WatchlistMapper watchlistMapper;

    public WatchlistService(AccountMapper accountMapper, WatchlistMapper watchlistMapper) {
        this.accountMapper = accountMapper;
        this.watchlistMapper = watchlistMapper;
    }

    @Transactional
    public Set<String> syncWatchlist(Long accountId, Collection<String> tickers) {
        if (accountId == null || accountId <= 0) {
            throw new ApiException(ErrorCode.ACCOUNT_ID_REQUIRED);
        }
        if (accountMapper.findById(accountId) == null) {
            throw new ApiException(ErrorCode.ACCOUNT_NOT_FOUND, "Account not found with id: " + accountId);
        }

        Set<String> normalizedTickers = new LinkedHashSet<>();
        if (tickers != null) {
            tickers.stream()
                .filter(ticker -> ticker != null && !ticker.trim().isEmpty())
                .map(ticker -> ticker.trim().toUpperCase(Locale.ROOT))
                .forEach(normalizedTickers::add);
        }

        watchlistMapper.deleteByAccountId(accountId);
        normalizedTickers.forEach(ticker -> watchlistMapper.insert(accountId, ticker));
        return normalizedTickers;
    }

    @Transactional(readOnly = true)
    public List<String> getWatchlist(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new ApiException(ErrorCode.ACCOUNT_ID_REQUIRED);
        }
        if (accountMapper.findById(accountId) == null) {
            throw new ApiException(ErrorCode.ACCOUNT_NOT_FOUND, "Account not found with id: " + accountId);
        }
        return watchlistMapper.findTickersByAccountId(accountId);
    }
}