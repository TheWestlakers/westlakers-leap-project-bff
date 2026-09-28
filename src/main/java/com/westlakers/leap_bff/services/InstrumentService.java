package com.westlakers.leap_bff.services;

import java.util.List;

import org.springframework.stereotype.Service;

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
        if(instruments.size() == 0) {
            throw new RuntimeException("Couldn't find any instruments");
        }

        return instruments;
    }

    public Instrument getInstrumentById(Long id) {
        Instrument instrument = this.instrumentMapper.findById(id);
        
        if(instrument == null) {
            throw new RuntimeException("Instrument not found with id: "  + id);
        }

        return instrument;
    }

    public Instrument getInstrumentByTicker(String ticker) {
        Instrument instrument = this.instrumentMapper.findByTicker(ticker);
        
        if(instrument == null) {
            throw new RuntimeException("Instrument not found with ticker: " + ticker);
        }

        return instrument;
    }
}
