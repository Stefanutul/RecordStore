package com.example.RecordStore.controller;

import com.example.RecordStore.dtos.TrackDto;
import com.example.RecordStore.service.TrackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/records/{recordId}/tracks")
public class TrackController {

    private final TrackService trackService;

    @GetMapping
    public List<TrackDto.Response> getTracks(@PathVariable Long recordId) {
        return trackService.getAllTracksForRecord(recordId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrackDto.Response create(
            @PathVariable Long recordId,
            @RequestBody @Valid TrackDto.Request req
    ) {
        return trackService.createTrack(recordId, req);
    }

    @DeleteMapping("/{trackId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long recordId, @PathVariable Long trackId) {
        trackService.deleteTrack(recordId, trackId);
    }
}

