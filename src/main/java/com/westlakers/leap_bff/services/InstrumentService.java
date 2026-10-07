package com.westlakers.leap_bff.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.westlakers.leap_bff.entities.Instrument;
import com.westlakers.leap_bff.mappers.InstrumentMapper;
import com.westlakers.leap_bff.exceptions.ApiException;
import com.westlakers.leap_bff.exceptions.ErrorCode;

@Service 
public class InstrumentService {
    private final InstrumentMapper instrumentMapper;

    public InstrumentService(InstrumentMapper instrumentMapper) {
        this.instrumentMapper = instrumentMapper;
    }

    public List<Instrument> getAllInstruments() {
        List<Instrument> instruments = this.instrumentMapper.findAll();
        if(instruments.isEmpty()) {
            throw new ApiException(ErrorCode.EMPTY_RESULTS);
        }
        return instruments;
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
