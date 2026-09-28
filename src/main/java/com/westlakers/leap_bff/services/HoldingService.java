package com.westlakers.leap_bff.services;

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
}
