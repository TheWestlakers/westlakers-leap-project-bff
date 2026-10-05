package com.westlakers.leap_bff.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.westlakers.leap_bff.mappers.HoldingMapper;
import com.westlakers.leap_bff.entities.Holding;
import com.westlakers.leap_bff.dtos.HoldingDTO;
import java.math.BigDecimal;


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
        if(holding.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Quantity must be greater than zero");
        }
        if(holding.getAveragePrice() == null) {
            throw new RuntimeException("Average Price is required");
        }
        if(holding.getAveragePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Average Price must be greater than zero");
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
}
