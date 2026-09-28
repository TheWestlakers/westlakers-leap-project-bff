package com.westlakers.leap_bff.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.westlakers.leap_bff.services.InstrumentService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.westlakers.leap_bff.entities.Instrument;


@RestController 
@RequestMapping("/api")
public class InstrumentController {
    private final InstrumentService instrumentService;

    public InstrumentController(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @GetMapping("/instruments")
    public ResponseEntity<List<Instrument>> getAllInstruments() {
        ResponseEntity<List<Instrument>> response = new ResponseEntity<>(this.instrumentService.getAllInstruments(), HttpStatus.OK);
        
        return response;
    }

    @GetMapping("/instruments/{id}")
    public ResponseEntity<Instrument> getInstrumentById(@PathVariable Long id) {
        ResponseEntity<Instrument> response = new ResponseEntity<>(this.instrumentService.getInstrumentById(id), HttpStatus.OK);

        return response;
    }
}
