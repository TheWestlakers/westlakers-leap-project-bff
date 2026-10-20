package com.westlakers.leap_bff.services;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import com.westlakers.leap_bff.entities.Instrument;
import com.westlakers.leap_bff.mappers.InstrumentMapper;
import com.westlakers.leap_bff.exceptions.ApiException;
import com.westlakers.leap_bff.exceptions.ErrorCode;

@Service 
public class InstrumentService {
    private static final int SEARCH_LIMIT = 7;
    private static final long DEFAULT_MARKET_ID = 2L;
    private static final long EQUITY_ASSET_CLASS_ID = 1L;
    private static final long CRYPTO_ASSET_CLASS_ID = 3L;

    private final InstrumentMapper instrumentMapper;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public InstrumentService(InstrumentMapper instrumentMapper, ObjectMapper objectMapper) {
        this.instrumentMapper = instrumentMapper;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(3))
                .build();
    }

    public List<Instrument> getAllInstruments() {
        List<Instrument> instruments = this.instrumentMapper.findAll();
        return instruments;
    }

    public List<Instrument> searchInstruments(String query) {
        String normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.isEmpty()) {
            return List.of();
        }

        List<Instrument> matches = instrumentMapper.search(normalizedQuery, SEARCH_LIMIT);
        if (!matches.isEmpty()) {
            return matches;
        }

        return populateFromYahoo(normalizedQuery);
    }

    private List<Instrument> populateFromYahoo(String query) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://query1.finance.yahoo.com/v1/finance/search?q="
                + encodedQuery + "&quotesCount=" + SEARCH_LIMIT + "&newsCount=0";
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(java.time.Duration.ofSeconds(5))
                .header("User-Agent", "Paysprint/1.0")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return List.of();
            }

            JsonNode quotes = objectMapper.readTree(response.body()).path("quotes");
            List<Instrument> populated = new java.util.ArrayList<>();
            for (JsonNode quote : quotes) {
                String ticker = quote.path("symbol").asText("").trim();
                String name = quote.path("longname").asText(quote.path("shortname").asText("")).trim();
                if (ticker.isEmpty() || name.isEmpty() || instrumentMapper.findByTicker(ticker) != null) {
                    continue;
                }

                Instrument instrument = new Instrument(null, DEFAULT_MARKET_ID,
                        "CRYPTOCURRENCY".equalsIgnoreCase(quote.path("quoteType").asText())
                                ? CRYPTO_ASSET_CLASS_ID : EQUITY_ASSET_CLASS_ID,
                        ticker, name);
                if (instrumentMapper.createInstrument(instrument) > 0) {
                    populated.add(instrument);
                }
                if (populated.size() == SEARCH_LIMIT) {
                    break;
                }
            }
            return populated;
        } catch (IOException | InterruptedException exception) {
            Thread.currentThread().interrupt();
            return List.of();
        }
    }

    public Instrument getInstrumentById(Long id) {
        if(id == null || id <= 0) {
            throw new ApiException(ErrorCode.INVALID_INPUT);
        }
        Instrument instrument = this.instrumentMapper.findById(id);
        if(instrument == null) {
            throw new ApiException(ErrorCode.INSTRUMENT_NOT_FOUND, "Instrument not found with id: " + id);
        }
        return instrument;
    }

    public Instrument getInstrumentByTicker(String ticker) {
        if(ticker == null || ticker.trim().isEmpty()) {
            throw new ApiException(ErrorCode.MISSING_REQUIRED_FIELD);
        }
        Instrument instrument = this.instrumentMapper.findByTicker(ticker);
        if(instrument == null) {
            throw new ApiException(ErrorCode.INSTRUMENT_NOT_FOUND, "Instrument not found with ticker: " + ticker);
        }
        return instrument;
    }

    public Instrument createInstrument(Instrument instrument) {
        if(instrument == null || instrument.getTicker() == null || instrument.getTicker().trim().isEmpty()) {
            throw new ApiException(ErrorCode.MISSING_REQUIRED_FIELD);
        }
        Instrument duplicate = this.instrumentMapper.findByTicker(instrument.getTicker());
        if(duplicate != null) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Instrument already defined with ticker: " + instrument.getTicker());
        }
        int result = this.instrumentMapper.createInstrument(instrument);
        if(result <= 0) {
            throw new ApiException(ErrorCode.CREATION_FAILED);
        }
        return instrument;
    }

    public Instrument updateInstrument(Instrument instrument, Long id) {
        if(instrument == null) {
            throw new ApiException(ErrorCode.INVALID_INPUT);
        }
        if(id == null || id <= 0) {
            throw new ApiException(ErrorCode.INVALID_INPUT);
        }
        Instrument found = this.instrumentMapper.findById(id);
        if(found == null) {
            throw new ApiException(ErrorCode.INSTRUMENT_NOT_FOUND, "Instrument not found with id: " + id);
        }
        instrument.setInstrumentId(id);
        int result = this.instrumentMapper.saveInstrument(instrument);
        if(result <= 0) {
            throw new ApiException(ErrorCode.UPDATE_FAILED);
        }
        return instrument;
    }

    public void deleteInstrumentById(Long id) {
        if(id == null || id <= 0) {
            throw new ApiException(ErrorCode.INVALID_INPUT);
        }
        Instrument found = this.instrumentMapper.findById(id);
        if(found == null) {
            throw new ApiException(ErrorCode.INSTRUMENT_NOT_FOUND, "Instrument not found with id: " + id);
        }
        int result = this.instrumentMapper.deleteInstrumentById(id);
        if(result <= 0) {
            throw new ApiException(ErrorCode.DELETE_FAILED);
        }
    }
}
