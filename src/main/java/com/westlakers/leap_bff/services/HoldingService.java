package com.westlakers.leap_bff.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.westlakers.leap_bff.mappers.HoldingMapper;
import com.westlakers.leap_bff.entities.Holding;
import com.westlakers.leap_bff.dtos.HoldingDTO;

@Service
public class HoldingService {

    private final HoldingMapper holdingMapper;

    public HoldingService(HoldingMapper holdingMapper) {
        this.holdingMapper = holdingMapper;
    }

    @Transactional(readOnly = true)
    public List<HoldingDTO> getAllHoldings() {
        List<Holding> holdings = this.holdingMapper.findAll();

        if(holdings.size() == 0) {
            throw new RuntimeException("List was zero");
        }
        // Convert Holding entities to DTOs
        return holdings.stream()
                .map(HoldingDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HoldingDTO getHoldingById(Long id) {
        Holding holding = this.holdingMapper.findById(id);

        if(holding == null) {
            throw new RuntimeException("Holding not found with id: " + id);
        }

        return HoldingDTO.fromEntity(holding);
    }

    @Transactional(readOnly = true)
    public List<HoldingDTO> getHoldingsByAccountId(Long accountId) {
        List<Holding> holdings = this.holdingMapper.findByAccountId(accountId);

        if(holdings.size() == 0) {
            throw new RuntimeException("No holdings found for account id: " + accountId);
        }
        // Convert Holding entities to DTOs
        return holdings.stream()
                .map(HoldingDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public HoldingDTO createHolding(Holding holding) {
        // Validate required fields
        if(holding.getAccountId() == null) {
            throw new RuntimeException("Account ID is required");
        }
        if(holding.getInstrumentId() == null) {
            throw new RuntimeException("Instrument ID is required");
        }
        if(holding.getQuantity() == null) {
            throw new RuntimeException("Quantity is required");
        }
        if(holding.getAveragePrice() == null) {
            throw new RuntimeException("Average Price is required");
        }

        // Insert the holding
        int result = this.holdingMapper.insert(holding);
        if(result == 0) {
            throw new RuntimeException("Failed to create holding");
        }

        // Return the created holding
        return HoldingDTO.fromEntity(holding);
    }

    @Transactional
    public HoldingDTO updateHolding(Long holdingId, Holding updatedHolding) {
        // Verify holding exists
        Holding existingHolding = this.holdingMapper.findById(holdingId);
        if(existingHolding == null) {
            throw new RuntimeException("Holding not found with id: " + holdingId);
        }

        // Set the holding ID to ensure we're updating the correct record
        updatedHolding.setHoldingId(holdingId);

        // Update the holding
        int result = this.holdingMapper.update(updatedHolding);
        if(result == 0) {
            throw new RuntimeException("Failed to update holding with id: " + holdingId);
        }

        // Fetch and return the updated holding
        Holding updated = this.holdingMapper.findById(holdingId);
        return HoldingDTO.fromEntity(updated);
    }

    @Transactional
    public void deleteHolding(Long holdingId) {
        // Verify holding exists
        Holding holding = this.holdingMapper.findById(holdingId);
        if(holding == null) {
            throw new RuntimeException("Holding not found with id: " + holdingId);
        }

        // Delete the holding
        int result = this.holdingMapper.delete(holdingId);
        if(result == 0) {
            throw new RuntimeException("Failed to delete holding with id: " + holdingId);
        }
    }

    @Transactional
    public HoldingDTO upsertHoldingOnTrade(Long accountId, Long instrumentId, 
                                           BigDecimal quantity, BigDecimal executionPrice, String side) {
        if (accountId == null || accountId <= 0) {
            throw new RuntimeException("Valid Account ID is required");
        }
        if (instrumentId == null || instrumentId <= 0) {
            throw new RuntimeException("Valid Instrument ID is required");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }
        if (executionPrice == null || executionPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Execution price must be greater than 0");
        }
        if (side == null || !side.toUpperCase().matches("^(BUY|SELL)$")) {
            throw new RuntimeException("Side must be either BUY or SELL");
        }

        String normalizedSide = side.toUpperCase();
        
        // Find existing holding
        Holding existingHolding = this.holdingMapper.findByAccountAndInstrument(accountId, instrumentId);
        
        if (existingHolding == null) {
            // No existing holding, should only be valid for BUY
            if (!normalizedSide.equals("BUY")) {
                throw new RuntimeException("Cannot sell shares that are not owned. Account has no position in instrument.");
            }
            
            // Create new holding
            Holding newHolding = new Holding();
            newHolding.setAccountId(accountId);
            newHolding.setInstrumentId(instrumentId);
            newHolding.setQuantity(quantity);
            newHolding.setAveragePrice(executionPrice);
            
            int result = this.holdingMapper.insert(newHolding);
            if (result == 0) {
                throw new RuntimeException("Failed to create holding during BUY trade");
            }
            
            return HoldingDTO.fromEntity(newHolding);
        }
        
        // Existing holding found and update it
        if (normalizedSide.equals("BUY")) {
            // BUY logic: Add to position and recalculate average price
            BigDecimal oldQuantity = existingHolding.getQuantity();
            BigDecimal oldAvgPrice = existingHolding.getAveragePrice();
            
            // newAvgPrice = (oldQty * oldAvgPrice + newQty * newPrice) / (oldQty + newQty)
            BigDecimal totalCost = oldQuantity.multiply(oldAvgPrice)
                    .add(quantity.multiply(executionPrice));
            BigDecimal newQuantity = oldQuantity.add(quantity);
            BigDecimal newAvgPrice = totalCost.divide(newQuantity, 4, RoundingMode.HALF_UP);
            
            existingHolding.setQuantity(newQuantity);
            existingHolding.setAveragePrice(newAvgPrice);
            
            int result = this.holdingMapper.update(existingHolding);
            if (result == 0) {
                throw new RuntimeException("Failed to update holding during BUY trade");
            }
            
            return HoldingDTO.fromEntity(existingHolding);
        } else {
            // SELL logic: Reduce position
            BigDecimal oldQuantity = existingHolding.getQuantity();
            
            if (quantity.compareTo(oldQuantity) > 0) {
                throw new RuntimeException(
                    String.format("Cannot sell %.4f shares - only %.4f available", quantity, oldQuantity)
                );
            }
            
            BigDecimal newQuantity = oldQuantity.subtract(quantity);
            
            if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
                // Quantity is 0 then delete the holding
                int result = this.holdingMapper.deleteByAccountAndInstrument(accountId, instrumentId);
                if (result == 0) {
                    throw new RuntimeException("Failed to delete holding after selling all shares");
                }
                
                // Create a response DTO showing the liquidated position
                Holding liquidatedHolding = new Holding();
                liquidatedHolding.setAccountId(accountId);
                liquidatedHolding.setInstrumentId(instrumentId);
                liquidatedHolding.setQuantity(BigDecimal.ZERO);
                liquidatedHolding.setAveragePrice(existingHolding.getAveragePrice());
                
                return HoldingDTO.fromEntity(liquidatedHolding);
            } else {
                // Quantity remains so update it (average price stays the same for SELL)
                existingHolding.setQuantity(newQuantity);
                
                int result = this.holdingMapper.update(existingHolding);
                if (result == 0) {
                    throw new RuntimeException("Failed to update holding during SELL trade");
                }
                
                return HoldingDTO.fromEntity(existingHolding);
            }
        }
    }
}
