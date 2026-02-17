package com.example.RecordStore.service;

import com.example.RecordStore.dtos.RecordDto;

import java.util.List;
import java.util.Optional;

public interface RecordService {

    List<RecordDto.Response> getAllRecords();
    Optional<RecordDto.Response> getRecordById(Long id);
    RecordDto.Response createRecord(RecordDto.Request record);
    RecordDto.Response updateRecord(Long id, RecordDto.Request record);
    void deleteRecord(Long id);

}
