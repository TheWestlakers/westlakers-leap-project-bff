package com.westlakers.leap_bff.services;

import com.westlakers.leap_bff.entities.Instrument;
import com.westlakers.leap_bff.mappers.InstrumentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class InstrumentServiceTest {

    @Mock
    private InstrumentMapper instrumentMapper;

    @InjectMocks
    private InstrumentService instrumentService;

    private Instrument testInstrument;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test data
        testInstrument = new Instrument();
        testInstrument.setInstrumentId(1L);
        testInstrument.setMarketId(1L);
        testInstrument.setAssetClassId(1L);
        testInstrument.setTicker("AAPL");
        testInstrument.setName("Apple Inc.");
    }

    @Test
    void testGetAllInstruments_Success() {
        // Arrange
        List<Instrument> instruments = Arrays.asList(testInstrument);
        when(instrumentMapper.findAll()).thenReturn(instruments);

        // Act
        List<Instrument> result = instrumentService.getAllInstruments();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).getTicker());
        verify(instrumentMapper, times(1)).findAll();
    }

    @Test
    void testGetAllInstruments_EmptyList_ThrowsNotFound() {
        // Arrange
        when(instrumentMapper.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> instrumentService.getAllInstruments());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testGetInstrumentById_ValidId() {
        // Arrange
        when(instrumentMapper.findById(1L)).thenReturn(testInstrument);

        // Act
        Instrument result = instrumentService.getInstrumentById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getInstrumentId());
        assertEquals("AAPL", result.getTicker());
        verify(instrumentMapper, times(1)).findById(1L);
    }

    @Test
    void testGetInstrumentById_InvalidId_ThrowsBadRequest() {
        // Arrange - id <= 0 is invalid

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> instrumentService.getInstrumentById(0L));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void testGetInstrumentById_NegativeId_ThrowsBadRequest() {
        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> instrumentService.getInstrumentById(-1L));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void testGetInstrumentById_NotFound_ThrowsNotFound() {
        // Arrange
        when(instrumentMapper.findById(999L)).thenReturn(null);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> instrumentService.getInstrumentById(999L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testGetInstrumentByTicker_Success() {
        // Arrange
        when(instrumentMapper.findByTicker("AAPL")).thenReturn(testInstrument);

        // Act
        Instrument result = instrumentService.getInstrumentByTicker("AAPL");

        // Assert
        assertNotNull(result);
        assertEquals("AAPL", result.getTicker());
        verify(instrumentMapper, times(1)).findByTicker("AAPL");
    }

    @Test
    void testGetInstrumentByTicker_EmptyTicker_ThrowsBadRequest() {
        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> instrumentService.getInstrumentByTicker(""));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void testGetInstrumentByTicker_NotFound_ThrowsNotFound() {
        // Arrange
        when(instrumentMapper.findByTicker("UNKNOWN")).thenReturn(null);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> instrumentService.getInstrumentByTicker("UNKNOWN"));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testCreateInstrument_ValidData() {
        // Arrange
        when(instrumentMapper.findByTicker("AAPL")).thenReturn(null); // Not duplicate
        when(instrumentMapper.createInstrument(testInstrument)).thenReturn(1);

        // Act
        Instrument result = instrumentService.createInstrument(testInstrument);

        // Assert
        assertNotNull(result);
        assertEquals("AAPL", result.getTicker());
        verify(instrumentMapper, times(1)).createInstrument(testInstrument);
    }

    @Test
    void testCreateInstrument_DuplicateTicker_ThrowsConflict() {
        // Arrange
        when(instrumentMapper.findByTicker("AAPL")).thenReturn(testInstrument);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> instrumentService.createInstrument(testInstrument));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void testCreateInstrument_InvalidTicker_ThrowsBadRequest() {
        // Arrange
        testInstrument.setTicker("");

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> instrumentService.createInstrument(testInstrument));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void testUpdateInstrument_Success() {
        // Arrange
        Instrument updatedInstrument = new Instrument();
        updatedInstrument.setTicker("AAPL");
        updatedInstrument.setName("Apple Inc. Updated");

        when(instrumentMapper.findById(1L)).thenReturn(testInstrument);
        when(instrumentMapper.saveInstrument(any())).thenReturn(1);

        // Act
        Instrument result = instrumentService.updateInstrument(updatedInstrument, 1L);

        // Assert
        assertNotNull(result);
        verify(instrumentMapper, times(1)).saveInstrument(any());
    }

    @Test
    void testUpdateInstrument_InvalidId_ThrowsBadRequest() {
        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> instrumentService.updateInstrument(testInstrument, 0L));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void testUpdateInstrument_NotFound_ThrowsNotFound() {
        // Arrange
        when(instrumentMapper.findById(999L)).thenReturn(null);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> instrumentService.updateInstrument(testInstrument, 999L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testDeleteInstrumentById_Success() {
        // Arrange
        when(instrumentMapper.findById(1L)).thenReturn(testInstrument);
        when(instrumentMapper.deleteInstrumentById(1L)).thenReturn(1);

        // Act
        assertDoesNotThrow(() -> instrumentService.deleteInstrumentById(1L));

        // Assert
        verify(instrumentMapper, times(1)).deleteInstrumentById(1L);
    }

    @Test
    void testDeleteInstrumentById_InvalidId_ThrowsBadRequest() {
        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> instrumentService.deleteInstrumentById(0L));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void testDeleteInstrumentById_NotFound_ThrowsNotFound() {
        // Arrange
        when(instrumentMapper.findById(999L)).thenReturn(null);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> instrumentService.deleteInstrumentById(999L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
