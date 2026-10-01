package com.westlakers.leap_bff.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.westlakers.leap_bff.entities.Instrument;
import com.westlakers.leap_bff.mappers.InstrumentMapper;

@Service 
public class InstrumentService {
    private final InstrumentMapper instrumentMapper;

    public InstrumentService(InstrumentMapper instrumentMapper) {
        this.instrumentMapper = instrumentMapper;
    }

    public List<Instrument> getAllInstruments() {
        List<Instrument> instruments = this.instrumentMapper.findAll();
        if(instruments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No instruments found");
        }
        return instruments;
    }

    public Instrument getInstrumentById(Long id) {
        if(id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid instrument ID");
        }
        Instrument instrument = this.instrumentMapper.findById(id);
        if(instrument == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Instrument not found with id: " + id);
        }
        return instrument;
    }

    public Instrument getInstrumentByTicker(String ticker) {
        if(ticker == null || ticker.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticker cannot be empty");
        }
        Instrument instrument = this.instrumentMapper.findByTicker(ticker);
        if(instrument == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Instrument not found with ticker: " + ticker);
        }
        return instrument;
    }

    public Instrument createInstrument(Instrument instrument) {
        if(instrument == null || instrument.getTicker() == null || instrument.getTicker().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Instrument and ticker cannot be null or empty");
        }
        Instrument duplicate = this.instrumentMapper.findByTicker(instrument.getTicker());
        if(duplicate != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Instrument already defined with ticker: " + instrument.getTicker());
        }
        int result = this.instrumentMapper.createInstrument(instrument);
        if(result <= 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create instrument");
        }
        return instrument;
    }

    public Instrument updateInstrument(Instrument instrument, Long id) {
        if(instrument == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Instrument cannot be null");
        }
        if(id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid instrument ID");
        }
        Instrument found = this.instrumentMapper.findById(id);
        if(found == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Instrument not found with id: " + id);
        }
        instrument.setInstrumentId(id);
        int result = this.instrumentMapper.saveInstrument(instrument);
        if(result <= 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update instrument");
        }
        return instrument;
    }

    public void deleteInstrumentById(Long id) {
        if(id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid instrument ID");
        }
        Instrument found = this.instrumentMapper.findById(id);
        if(found == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Instrument not found with id: " + id);
        }
        int result = this.instrumentMapper.deleteInstrumentById(id);
        if(result <= 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete instrument");
        }
    }
}
