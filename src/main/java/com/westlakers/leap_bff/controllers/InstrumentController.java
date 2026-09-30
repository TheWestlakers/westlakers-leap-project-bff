package com.westlakers.leap_bff.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.westlakers.leap_bff.services.InstrumentService;

import jakarta.validation.Valid;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.westlakers.leap_bff.entities.Instrument;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;




@RestController 
@RequestMapping("/api")
public class InstrumentController {
    private final InstrumentService instrumentService;

    public InstrumentController(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @PostMapping("/instruments")
    public ResponseEntity<Instrument> CreateInstrument(@Valid @RequestBody Instrument instrument) {
        return new ResponseEntity<>(this.instrumentService.createInstrument(instrument), HttpStatus.CREATED);
    }
    

    @GetMapping("/instruments")
    public ResponseEntity<List<Instrument>> getAllInstruments() {
        return new ResponseEntity<>(this.instrumentService.getAllInstruments(), HttpStatus.OK);
    }

    @GetMapping("/instruments/{id}")
    public ResponseEntity<Instrument> getInstrumentById(@PathVariable Long id) {
        return new ResponseEntity<>(this.instrumentService.getInstrumentById(id), HttpStatus.OK);
    }

    @PutMapping("instruments/{id}")
    public ResponseEntity<Instrument> putMethodName(@PathVariable Long id, @Valid @RequestBody Instrument instrument) {
        return new ResponseEntity<>(this.instrumentService.updateInstrument(instrument, id), HttpStatus.OK);
    }
    
    @DeleteMapping("instruments/{id}") 
    public ResponseEntity<Void> deleteInstrumentById(@PathVariable Long id) {
        this.instrumentService.deleteInstrumentById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
