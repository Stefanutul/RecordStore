package com.example.RecordStore.service.impl;

import com.example.RecordStore.dtos.TrackDto;
import com.example.RecordStore.exceptions.RecordNotFoundException;
import com.example.RecordStore.model.Record;
import com.example.RecordStore.exceptions.TrackNotFoundException;
import com.example.RecordStore.mapper.TrackMapper;
import com.example.RecordStore.model.Track;
import com.example.RecordStore.repository.RecordRepository;
import com.example.RecordStore.repository.TrackRepository;
import com.example.RecordStore.service.TrackService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TrackServiceImpl implements TrackService {

    private final TrackRepository trackRepository;
    private final RecordRepository recordRepository;
    private final TrackMapper trackMapper;

    @Override
    public List<TrackDto.Response> getAllTracksForRecord(Long recordId) {
        // optional: validate record exists (gives nicer error)
        if (!recordRepository.existsById(recordId)) {
            throw new RecordNotFoundException("Record not found with id " + recordId);
        }

        return trackRepository.findByRecordId(recordId).stream()
                .map(trackMapper::toResponse)
                .toList();
    }

    @Override
    public TrackDto.Response getTrackById(Long recordId, Long trackId) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new TrackNotFoundException("Track not found with id " + trackId));

        // ensure the track belongs to that record (important!)
        if (!track.getRecord().getId().equals(recordId)) {
            throw new TrackNotFoundException("Track " + trackId + " does not belong to record " + recordId);
        }

        return trackMapper.toResponse(track);
    }

    @Override
    public TrackDto.Response createTrack(Long recordId, TrackDto.Request request) {
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RecordNotFoundException("Record not found with id " + recordId));

        Track track = trackMapper.toEntity(request);
        track.setRecord(record);

        Track saved = trackRepository.save(track);
        return trackMapper.toResponse(saved);
    }

    @Override
    public TrackDto.Response updateTrack(Long recordId, Long trackId, TrackDto.Request request) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new TrackNotFoundException("Track not found with id " + trackId));

        if (!track.getRecord().getId().equals(recordId)) {
            throw new TrackNotFoundException("Track " + trackId + " does not belong to record " + recordId);
        }

        track.setTitle(request.title());
        track.setDurationSeconds(request.durationSeconds());
        track.setTrackKey(request.trackKey());

        Track updated = trackRepository.save(track);
        return trackMapper.toResponse(updated);
    }

    @Override
    public void deleteTrack(Long recordId, Long trackId) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new TrackNotFoundException("Track not found with id " + trackId));

        if (!track.getRecord().getId().equals(recordId)) {
            throw new TrackNotFoundException("Track " + trackId + " does not belong to record " + recordId);
        }

        trackRepository.delete(track);
    }
}

