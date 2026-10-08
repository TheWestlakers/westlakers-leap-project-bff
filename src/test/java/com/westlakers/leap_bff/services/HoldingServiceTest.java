package com.westlakers.leap_bff.services;

import com.westlakers.leap_bff.dtos.HoldingDTO;
import com.westlakers.leap_bff.entities.Holding;
import com.westlakers.leap_bff.mappers.HoldingMapper;
import com.westlakers.leap_bff.exceptions.ApiException;
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
        assertThrows(ApiException.class, () -> holdingService.getAllHoldings());
        
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
        assertThrows(ApiException.class, () -> holdingService.getHoldingById(999L));
        
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
        assertThrows(ApiException.class, () -> holdingService.getHoldingsByAccountId(999L));
        
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
        assertThrows(ApiException.class, () -> holdingService.createHolding(testHolding));
        
        // Verify validation occurred before mapper call
        verify(holdingMapper, never()).insert(any());
    }

    @Test
    void testCreateHolding_NegativeQuantity_ThrowsException() {
        // Arrange
        testHolding.setQuantity(new BigDecimal("-100.00"));

        // Act & Assert
        assertThrows(ApiException.class, () -> holdingService.createHolding(testHolding));
        
        // Verify validation occurred before mapper call
        verify(holdingMapper, never()).insert(any());
    }

    @Test
    void testCreateHolding_InvalidPrice_ThrowsException() {
        // Arrange
        testHolding.setAveragePrice(BigDecimal.ZERO);

        // Act & Assert
        assertThrows(ApiException.class, () -> holdingService.createHolding(testHolding));
        
        // Verify validation occurred before mapper call
        verify(holdingMapper, never()).insert(any());
    }

    @Test
    void testCreateHolding_NegativePrice_ThrowsException() {
        // Arrange
        testHolding.setAveragePrice(new BigDecimal("-50.00"));

        // Act & Assert
        assertThrows(ApiException.class, () -> holdingService.createHolding(testHolding));
        
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
        assertThrows(ApiException.class, () -> holdingService.updateHolding(999L, testHolding));
        
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
        assertThrows(ApiException.class, () -> holdingService.deleteHolding(999L));
        
        // Verify lookup was attempted
        verify(holdingMapper, times(1)).findById(999L);
        // Verify delete was never called
        verify(holdingMapper, never()).delete(any());
    }

    /*
     * ==================== NEW TESTS FOR upsertHoldingOnTrade() ====================
     */

    /*
    @Test
    void testUpsertHoldingOnTrade_CreateNewHoldingOnBuy_Success() {
        // Arrange
        Long accountId = 1L;
        Long instrumentId = 1L;
        BigDecimal quantity = new BigDecimal("100.00");
        BigDecimal executionPrice = new BigDecimal("50.00");
        String side = "BUY";

        Holding newHolding = new Holding();
        newHolding.setAccountId(accountId);
        newHolding.setInstrumentId(instrumentId);
        newHolding.setQuantity(quantity);
        newHolding.setAveragePrice(executionPrice);

        when(holdingMapper.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(null);
        when(holdingMapper.insert(any())).thenReturn(1);

        // Act
        HoldingDTO result = holdingService.upsertHoldingOnTrade(accountId, instrumentId, quantity, executionPrice, side);

        // Assert
        assertNotNull(result);
        assertEquals(accountId, result.getAccountId());
        assertEquals(instrumentId, result.getInstrumentId());
        assertEquals(quantity, result.getQuantity());
        assertEquals(executionPrice, result.getAveragePrice());
        verify(holdingMapper, times(1)).findByAccountAndInstrument(accountId, instrumentId);
        verify(holdingMapper, times(1)).insert(any());
    }

    @Test
    void testUpsertHoldingOnTrade_UpdateExistingHoldingOnBuy_RecalculatesAveragePrice() {
        // Arrange
        Long accountId = 1L;
        Long instrumentId = 1L;
        BigDecimal quantity = new BigDecimal("50.00"); // Adding 50 more shares
        BigDecimal executionPrice = new BigDecimal("60.00"); // New price higher than old
        String side = "BUY";

        Holding existingHolding = new Holding();
        existingHolding.setAccountId(accountId);
        existingHolding.setInstrumentId(instrumentId);
        existingHolding.setQuantity(new BigDecimal("100.00")); // Already own 100 shares
        existingHolding.setAveragePrice(new BigDecimal("50.00")); // At average price of 50

        when(holdingMapper.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(existingHolding);
        when(holdingMapper.update(any())).thenReturn(1);

        // Act
        HoldingDTO result = holdingService.upsertHoldingOnTrade(accountId, instrumentId, quantity, executionPrice, side);

        // Assert
        assertNotNull(result);
        // Expected: (100 * 50 + 50 * 60) / (100 + 50) = (5000 + 3000) / 150 = 53.3333
        assertEquals(new BigDecimal("150.00"), result.getQuantity()); // 100 + 50
        assertEquals(new BigDecimal("53.3333"), result.getAveragePrice()); // Recalculated average
        verify(holdingMapper, times(1)).findByAccountAndInstrument(accountId, instrumentId);
        verify(holdingMapper, times(1)).update(any());
    }

    @Test
    void testUpsertHoldingOnTrade_SellReducesPosition() {
        // Arrange
        Long accountId = 1L;
        Long instrumentId = 1L;
        BigDecimal quantity = new BigDecimal("30.00"); // Selling 30 shares
        BigDecimal executionPrice = new BigDecimal("55.00");
        String side = "SELL";

        Holding existingHolding = new Holding();
        existingHolding.setAccountId(accountId);
        existingHolding.setInstrumentId(instrumentId);
        existingHolding.setQuantity(new BigDecimal("100.00")); // Own 100 shares
        existingHolding.setAveragePrice(new BigDecimal("50.00"));

        when(holdingMapper.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(existingHolding);
        when(holdingMapper.update(any())).thenReturn(1);

        // Act
        HoldingDTO result = holdingService.upsertHoldingOnTrade(accountId, instrumentId, quantity, executionPrice, side);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("70.00"), result.getQuantity()); // 100 - 30
        assertEquals(new BigDecimal("50.00"), result.getAveragePrice()); // Price stays same for SELL
        verify(holdingMapper, times(1)).findByAccountAndInstrument(accountId, instrumentId);
        verify(holdingMapper, times(1)).update(any());
    }

    @Test
    void testUpsertHoldingOnTrade_SellAllShares_DeletesHolding() {
        // Arrange
        Long accountId = 1L;
        Long instrumentId = 1L;
        BigDecimal quantity = new BigDecimal("100.00"); // Selling all shares
        BigDecimal executionPrice = new BigDecimal("55.00");
        String side = "SELL";

        Holding existingHolding = new Holding();
        existingHolding.setAccountId(accountId);
        existingHolding.setInstrumentId(instrumentId);
        existingHolding.setQuantity(new BigDecimal("100.00")); // Own 100 shares
        existingHolding.setAveragePrice(new BigDecimal("50.00"));

        when(holdingMapper.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(existingHolding);
        when(holdingMapper.deleteByAccountAndInstrument(accountId, instrumentId)).thenReturn(1);

        // Act
        HoldingDTO result = holdingService.upsertHoldingOnTrade(accountId, instrumentId, quantity, executionPrice, side);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getQuantity()); // Position liquidated
        verify(holdingMapper, times(1)).findByAccountAndInstrument(accountId, instrumentId);
        verify(holdingMapper, times(1)).deleteByAccountAndInstrument(accountId, instrumentId);
        verify(holdingMapper, never()).update(any());
    }

    @Test
    void testUpsertHoldingOnTrade_SellMoreThanOwned_ThrowsException() {
        // Arrange
        Long accountId = 1L;
        Long instrumentId = 1L;
        BigDecimal quantity = new BigDecimal("150.00"); // Trying to sell 150 but only own 100
        BigDecimal executionPrice = new BigDecimal("55.00");
        String side = "SELL";

        Holding existingHolding = new Holding();
        existingHolding.setAccountId(accountId);
        existingHolding.setInstrumentId(instrumentId);
        existingHolding.setQuantity(new BigDecimal("100.00")); // Own 100 shares
        existingHolding.setAveragePrice(new BigDecimal("50.00"));

        when(holdingMapper.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(existingHolding);

        // Act & Assert
        assertThrows(ApiException.class, () ->
            holdingService.upsertHoldingOnTrade(accountId, instrumentId, quantity, executionPrice, side)
        );
        verify(holdingMapper, times(1)).findByAccountAndInstrument(accountId, instrumentId);
        verify(holdingMapper, never()).update(any());
        verify(holdingMapper, never()).deleteByAccountAndInstrument(any(), any());
    }

    @Test
    void testUpsertHoldingOnTrade_SellWithoutExistingHolding_ThrowsException() {
        // Arrange
        Long accountId = 1L;
        Long instrumentId = 1L;
        BigDecimal quantity = new BigDecimal("50.00");
        BigDecimal executionPrice = new BigDecimal("55.00");
        String side = "SELL"; // Trying to sell but have no position

        when(holdingMapper.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () ->
            holdingService.upsertHoldingOnTrade(accountId, instrumentId, quantity, executionPrice, side)
        );
        verify(holdingMapper, times(1)).findByAccountAndInstrument(accountId, instrumentId);
        verify(holdingMapper, never()).insert(any());
        verify(holdingMapper, never()).update(any());
    }

    @Test
    void testUpsertHoldingOnTrade_InvalidAccountId_ThrowsException() {
        // Arrange
        Long invalidAccountId = 0L;
        BigDecimal quantity = new BigDecimal("50.00");
        BigDecimal executionPrice = new BigDecimal("55.00");

        // Act & Assert
        assertThrows(ApiException.class, () ->
            holdingService.upsertHoldingOnTrade(invalidAccountId, 1L, quantity, executionPrice, "BUY")
        );
    }

    @Test
    void testUpsertHoldingOnTrade_InvalidInstrumentId_ThrowsException() {
        // Arrange
        Long invalidInstrumentId = -1L;
        BigDecimal quantity = new BigDecimal("50.00");
        BigDecimal executionPrice = new BigDecimal("55.00");

        // Act & Assert
        assertThrows(ApiException.class, () ->
            holdingService.upsertHoldingOnTrade(1L, invalidInstrumentId, quantity, executionPrice, "BUY")
        );
    }

    @Test
    void testUpsertHoldingOnTrade_InvalidQuantity_ThrowsException() {
        // Arrange
        BigDecimal invalidQuantity = BigDecimal.ZERO;
        BigDecimal executionPrice = new BigDecimal("55.00");

        // Act & Assert
        assertThrows(ApiException.class, () ->
            holdingService.upsertHoldingOnTrade(1L, 1L, invalidQuantity, executionPrice, "BUY")
        );
    }

    @Test
    void testUpsertHoldingOnTrade_NegativeQuantity_ThrowsException() {
        // Arrange
        BigDecimal negativeQuantity = new BigDecimal("-50.00");
        BigDecimal executionPrice = new BigDecimal("55.00");

        // Act & Assert
        assertThrows(ApiException.class, () ->
            holdingService.upsertHoldingOnTrade(1L, 1L, negativeQuantity, executionPrice, "BUY")
        );
    }

    @Test
    void testUpsertHoldingOnTrade_InvalidExecutionPrice_ThrowsException() {
        // Arrange
        BigDecimal quantity = new BigDecimal("50.00");
        BigDecimal invalidPrice = BigDecimal.ZERO;

        // Act & Assert
        assertThrows(ApiException.class, () ->
            holdingService.upsertHoldingOnTrade(1L, 1L, quantity, invalidPrice, "BUY")
        );
    }

    @Test
    void testUpsertHoldingOnTrade_NegativeExecutionPrice_ThrowsException() {
        // Arrange
        BigDecimal quantity = new BigDecimal("50.00");
        BigDecimal negativePrice = new BigDecimal("-55.00");

        // Act & Assert
        assertThrows(ApiException.class, () ->
            holdingService.upsertHoldingOnTrade(1L, 1L, quantity, negativePrice, "BUY")
        );
    }

    @Test
    void testUpsertHoldingOnTrade_InvalidSide_ThrowsException() {
        // Arrange
        BigDecimal quantity = new BigDecimal("50.00");
        BigDecimal executionPrice = new BigDecimal("55.00");
        String invalidSide = "INVALID";

        // Act & Assert
        assertThrows(ApiException.class, () ->
            holdingService.upsertHoldingOnTrade(1L, 1L, quantity, executionPrice, invalidSide)
        );
    }

    @Test
    void testUpsertHoldingOnTrade_NullSide_ThrowsException() {
        // Arrange
        BigDecimal quantity = new BigDecimal("50.00");
        BigDecimal executionPrice = new BigDecimal("55.00");

        // Act & Assert
        assertThrows(ApiException.class, () ->
            holdingService.upsertHoldingOnTrade(1L, 1L, quantity, executionPrice, null)
        );
    }

    @Test
    void testUpsertHoldingOnTrade_InsertionFailure_ThrowsException() {
        // Arrange
        Long accountId = 1L;
        Long instrumentId = 1L;
        BigDecimal quantity = new BigDecimal("100.00");
        BigDecimal executionPrice = new BigDecimal("50.00");
        String side = "BUY";

        when(holdingMapper.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(null);
        when(holdingMapper.insert(any())).thenReturn(0); // Insertion fails

        // Act & Assert
        assertThrows(ApiException.class, () ->
            holdingService.upsertHoldingOnTrade(accountId, instrumentId, quantity, executionPrice, side)
        );
        verify(holdingMapper, times(1)).insert(any());
    }

    @Test
    void testUpsertHoldingOnTrade_UpdateFailureOnBuy_ThrowsException() {
        // Arrange
        Long accountId = 1L;
        Long instrumentId = 1L;
        BigDecimal quantity = new BigDecimal("50.00");
        BigDecimal executionPrice = new BigDecimal("60.00");
        String side = "BUY";

        Holding existingHolding = new Holding();
        existingHolding.setAccountId(accountId);
        existingHolding.setInstrumentId(instrumentId);
        existingHolding.setQuantity(new BigDecimal("100.00"));
        existingHolding.setAveragePrice(new BigDecimal("50.00"));

        when(holdingMapper.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(existingHolding);
        when(holdingMapper.update(any())).thenReturn(0); // Update fails

        // Act & Assert
        assertThrows(ApiException.class, () ->
            holdingService.upsertHoldingOnTrade(accountId, instrumentId, quantity, executionPrice, side)
        );
        verify(holdingMapper, times(1)).update(any());
    }

    @Test
    void testUpsertHoldingOnTrade_UpdateFailureOnSell_ThrowsException() {
        // Arrange
        Long accountId = 1L;
        Long instrumentId = 1L;
        BigDecimal quantity = new BigDecimal("30.00");
        BigDecimal executionPrice = new BigDecimal("55.00");
        String side = "SELL";

        Holding existingHolding = new Holding();
        existingHolding.setAccountId(accountId);
        existingHolding.setInstrumentId(instrumentId);
        existingHolding.setQuantity(new BigDecimal("100.00"));
        existingHolding.setAveragePrice(new BigDecimal("50.00"));

        when(holdingMapper.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(existingHolding);
        when(holdingMapper.update(any())).thenReturn(0); // Update fails

        // Act & Assert
        assertThrows(ApiException.class, () ->
            holdingService.upsertHoldingOnTrade(accountId, instrumentId, quantity, executionPrice, side)
        );
        verify(holdingMapper, times(1)).update(any());
    }

    @Test
    void testUpsertHoldingOnTrade_DeleteFailureOnSellAll_ThrowsException() {
        // Arrange
        Long accountId = 1L;
        Long instrumentId = 1L;
        BigDecimal quantity = new BigDecimal("100.00");
        BigDecimal executionPrice = new BigDecimal("55.00");
        String side = "SELL";

        Holding existingHolding = new Holding();
        existingHolding.setAccountId(accountId);
        existingHolding.setInstrumentId(instrumentId);
        existingHolding.setQuantity(new BigDecimal("100.00"));
        existingHolding.setAveragePrice(new BigDecimal("50.00"));

        when(holdingMapper.findByAccountAndInstrument(accountId, instrumentId)).thenReturn(existingHolding);
        when(holdingMapper.deleteByAccountAndInstrument(accountId, instrumentId)).thenReturn(0); // Delete fails

        // Act & Assert
        assertThrows(ApiException.class, () ->
            holdingService.upsertHoldingOnTrade(accountId, instrumentId, quantity, executionPrice, side)
        );
        verify(holdingMapper, times(1)).deleteByAccountAndInstrument(accountId, instrumentId);
    }
    */
}
