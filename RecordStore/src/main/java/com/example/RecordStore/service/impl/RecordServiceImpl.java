package com.example.RecordStore.service.impl;

import com.example.RecordStore.dtos.RecordDto;
import com.example.RecordStore.exceptions.RecordNotFoundException;
import com.example.RecordStore.mapper.RecordMapper;
import com.example.RecordStore.model.AppUser;
import com.example.RecordStore.model.Record;
import com.example.RecordStore.repository.RecordRepository;
import com.example.RecordStore.service.AppUserService;
import com.example.RecordStore.service.RecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RecordServiceImpl implements RecordService {

    private final RecordRepository repository;
    private final RecordMapper mapper;
    private final AppUserService appUserService;



    @Override
    @Transactional(readOnly = true)
    public List<RecordDto.Response> getAllRecords() {
        String username = currentUsername();

        return repository.findAllByOwnerUsername(username).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RecordDto.Response> getRecordById(Long id) {
        String username = currentUsername();

        return repository.findByIdAndOwnerUsername(id, username)
                .map(mapper::toResponse);
    }



    @Override
    public RecordDto.Response createRecord(RecordDto.Request record) {
        String username = currentUsername();
        AppUser owner = appUserService.getByUsername(username);

        Record rec = mapper.toEntity(record);
        rec.setOwner(owner); //  owner always comes from auth

        Record saved = repository.save(rec);
        return mapper.toResponse(saved);
    }



    @Override
    public RecordDto.Response updateRecord(Long id, RecordDto.Request record) {
        String username = currentUsername();

        Record rec = repository.findByIdAndOwnerUsername(id, username)
                .orElseThrow(() ->
                        new RecordNotFoundException("Record not found or not owned by user")
                );

        rec.setTitle(record.title());
        rec.setArtist(record.artist());
        rec.setLabel(record.label());
        rec.setGenre(record.genre());
        rec.setPublishingYear(record.publishingYear());
        rec.setPrice(record.price());

        Record updated = repository.save(rec);
        return mapper.toResponse(updated);
    }



    @Override
    public void deleteRecord(Long id) {
        String username = currentUsername();

        Record rec = repository.findByIdAndOwnerUsername(id, username)
                .orElseThrow(() ->
                        new RecordNotFoundException("Record not found or not owned by user")
                );

        repository.delete(rec);
    }


    private String currentUsername() {
        return SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
    }
}
