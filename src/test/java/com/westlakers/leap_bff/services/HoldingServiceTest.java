package com.westlakers.leap_bff.services;

import com.westlakers.leap_bff.dtos.HoldingDTO;
import com.westlakers.leap_bff.entities.Holding;
import com.westlakers.leap_bff.mappers.HoldingMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HoldingServiceTest {

    @Mock
    private HoldingMapper holdingMapper;

    @InjectMocks
    private HoldingService holdingService;

    private Holding testHolding;
    private HoldingDTO testHoldingDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test data
        testHolding = new Holding();
        testHolding.setHoldingId(1L);
        testHolding.setAccountId(1L);
        testHolding.setInstrumentId(1L);
        testHolding.setQuantity(new BigDecimal("100.00"));
        testHolding.setAveragePrice(new BigDecimal("50.00"));

        testHoldingDTO = new HoldingDTO();
        testHoldingDTO.setHoldingId(1L);
        testHoldingDTO.setAccountId(1L);
        testHoldingDTO.setInstrumentId(1L);
        testHoldingDTO.setQuantity(new BigDecimal("100.00"));
        testHoldingDTO.setAveragePrice(new BigDecimal("50.00"));
    }

    @Test
    void testGetAllHoldings_Success() {
        // Arrange
        List<Holding> holdings = Arrays.asList(testHolding);
        when(holdingMapper.findAll()).thenReturn(holdings);

        // Act
        List<HoldingDTO> result = holdingService.getAllHoldings();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(holdingMapper, times(1)).findAll();
    }

    @Test
    void testGetAllHoldings_EmptyList_ThrowsException() {
        // Arrange
        when(holdingMapper.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> holdingService.getAllHoldings());
        
        // Verify the only call was findAll
        verify(holdingMapper, times(1)).findAll();
    }

    @Test
    void testGetHoldingById_Success() {
        // Arrange
        when(holdingMapper.findById(1L)).thenReturn(testHolding);

        // Act
        HoldingDTO result = holdingService.getHoldingById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getHoldingId());
        assertEquals(new BigDecimal("100.00"), result.getQuantity());
        verify(holdingMapper, times(1)).findById(1L);
    }

    @Test
    void testGetHoldingById_NotFound_ThrowsException() {
        // Arrange
        when(holdingMapper.findById(999L)).thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> holdingService.getHoldingById(999L));
        
        // Verify the lookup was attempted
        verify(holdingMapper, times(1)).findById(999L);
    }

    @Test
    void testGetHoldingsByAccountId_Success() {
        // Arrange
        List<Holding> holdings = Arrays.asList(testHolding);
        when(holdingMapper.findByAccountId(1L)).thenReturn(holdings);

        // Act
        List<HoldingDTO> result = holdingService.getHoldingsByAccountId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(new BigDecimal("100.00"), result.get(0).getQuantity());
        verify(holdingMapper, times(1)).findByAccountId(1L);
    }

    @Test
    void testGetHoldingsByAccountId_NoHoldings_ThrowsException() {
        // Arrange
        when(holdingMapper.findByAccountId(999L)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> holdingService.getHoldingsByAccountId(999L));
        
        // Verify the lookup was attempted
        verify(holdingMapper, times(1)).findByAccountId(999L);
    }

    @Test
    void testCreateHolding_ValidData() {
        // Arrange
        when(holdingMapper.insert(testHolding)).thenReturn(1);

        // Act
        HoldingDTO result = holdingService.createHolding(testHolding);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.getQuantity());
        verify(holdingMapper, times(1)).insert(testHolding);
    }

    @Test
    void testCreateHolding_InvalidQuantity_ThrowsException() {
        // Arrange
        testHolding.setQuantity(BigDecimal.ZERO);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> holdingService.createHolding(testHolding));
        
        // Verify validation occurred before mapper call
        verify(holdingMapper, never()).insert(any());
    }

    @Test
    void testCreateHolding_NegativeQuantity_ThrowsException() {
        // Arrange
        testHolding.setQuantity(new BigDecimal("-100.00"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> holdingService.createHolding(testHolding));
        
        // Verify validation occurred before mapper call
        verify(holdingMapper, never()).insert(any());
    }

    @Test
    void testCreateHolding_InvalidPrice_ThrowsException() {
        // Arrange
        testHolding.setAveragePrice(BigDecimal.ZERO);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> holdingService.createHolding(testHolding));
        
        // Verify validation occurred before mapper call
        verify(holdingMapper, never()).insert(any());
    }

    @Test
    void testCreateHolding_NegativePrice_ThrowsException() {
        // Arrange
        testHolding.setAveragePrice(new BigDecimal("-50.00"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> holdingService.createHolding(testHolding));
        
        // Verify validation occurred before mapper call
        verify(holdingMapper, never()).insert(any());
    }

    @Test
    void testUpdateHolding_Success() {
        // Arrange
        Holding updatedHolding = new Holding();
        updatedHolding.setQuantity(new BigDecimal("150.00"));
        updatedHolding.setAveragePrice(new BigDecimal("55.00"));

        when(holdingMapper.findById(1L)).thenReturn(testHolding);
        when(holdingMapper.update(any())).thenReturn(1);

        // Act
        HoldingDTO result = holdingService.updateHolding(1L, updatedHolding);

        // Assert
        assertNotNull(result);
        verify(holdingMapper, times(2)).findById(1L);
        verify(holdingMapper, times(1)).update(any());
    }

    @Test
    void testUpdateHolding_NotFound_ThrowsException() {
        // Arrange
        when(holdingMapper.findById(999L)).thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> holdingService.updateHolding(999L, testHolding));
        
        // Verify lookup was attempted
        verify(holdingMapper, times(1)).findById(999L);
        // Verify update was never called
        verify(holdingMapper, never()).update(any());
    }

    @Test
    void testDeleteHolding_Success() {
        // Arrange
        when(holdingMapper.findById(1L)).thenReturn(testHolding);
        when(holdingMapper.delete(1L)).thenReturn(1);

        // Act
        assertDoesNotThrow(() -> holdingService.deleteHolding(1L));

        // Assert
        verify(holdingMapper, times(1)).findById(1L);
        verify(holdingMapper, times(1)).delete(1L);
    }

    @Test
    void testDeleteHolding_NotFound_ThrowsException() {
        // Arrange
        when(holdingMapper.findById(999L)).thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> holdingService.deleteHolding(999L));
        
        // Verify lookup was attempted
        verify(holdingMapper, times(1)).findById(999L);
        // Verify delete was never called
        verify(holdingMapper, never()).delete(any());
    }
}
