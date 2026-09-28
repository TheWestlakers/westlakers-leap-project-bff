package com.westlakers.leap_bff.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.westlakers.leap_bff.services.InstrumentService;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import com.westlakers.leap_bff.entities.Instrument;


@RestController 
@RequestMapping("/api")
public class InstrumentController {
    private final InstrumentService instrumentService;

    public InstrumentController(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @GetMapping("/instruments")
    public List<Instrument> getAllInstruments() {
        return this.instrumentService.getAllInstruments();
    }

    @GetMapping("/instruments/{id}")
    public Instrument getInstrumentById(@PathVariable Long id) {
        return this.instrumentService.getInstrumentById(id);
    }
}
