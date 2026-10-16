package com.westlakers.leap_bff.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.westlakers.leap_bff.dtos.TradingVolumeDTO;
import com.westlakers.leap_bff.dtos.TradingVolumePageDTO;
import com.westlakers.leap_bff.entities.TradingVolume;
import com.westlakers.leap_bff.repositories.TradingVolumeRepository;

@Service
public class TradingVolumeService {

    public static final int PAGE_SIZE = 20;

    private final TradingVolumeRepository tradingVolumeRepository;

    public TradingVolumeService(TradingVolumeRepository tradingVolumeRepository) {
        this.tradingVolumeRepository = tradingVolumeRepository;
    }

    public TradingVolumePageDTO getTradingVolumes(int page) {
        long totalElements = tradingVolumeRepository.countAll();
        int totalPages = (int) ((totalElements + PAGE_SIZE - 1) / PAGE_SIZE);
        List<TradingVolumeDTO> content = tradingVolumeRepository
                .findPageOrderedByExecutedAt(PAGE_SIZE, page * PAGE_SIZE).stream()
                .map(this::toDto)
                .toList();

        return new TradingVolumePageDTO(content, page, PAGE_SIZE, totalElements, totalPages);
    }

    private TradingVolumeDTO toDto(TradingVolume volume) {
        return new TradingVolumeDTO(
                volume.getStagingId(), volume.getOrderId(), volume.getAccountId(),
                volume.getInstrumentId(), volume.getUserId(), volume.getSide(),
                volume.getQuantity(), volume.getLimitPrice(), volume.getExecutedAt(),
                volume.getAccountType(), volume.getFirstName(), volume.getLastName(),
                volume.getInstrumentCode(), volume.getInstrumentName(), volume.getInstrumentType(),
                volume.getClientSegment(), volume.getCreatedAt());
    }
}