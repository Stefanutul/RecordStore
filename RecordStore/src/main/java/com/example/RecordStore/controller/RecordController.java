package com.example.RecordStore.controller;

import com.example.RecordStore.dtos.RecordDto;
import com.example.RecordStore.service.RecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my/records")
public class RecordController {

    private final RecordService recordService;


    @GetMapping
    public List<RecordDto.Response> getMyRecords() {
        return recordService.getAllRecords(); // owner-scoped in service
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecordDto.Response> getMyRecordById(@PathVariable Long id) {
        return recordService.getRecordById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecordDto.Response createRecord(@Valid @RequestBody RecordDto.Request request) {
        return recordService.createRecord(request); // owner set from auth in service
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecordDto.Response> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody RecordDto.Request req
    ) {
        RecordDto.Response updated = recordService.updateRecord(id, req);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecord(@PathVariable Long id) {
        recordService.deleteRecord(id);
    }
}
