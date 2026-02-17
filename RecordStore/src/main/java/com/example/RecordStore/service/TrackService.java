package com.example.RecordStore.service;

import com.example.RecordStore.dtos.TrackDto;

import java.util.List;

public interface TrackService {

    List<TrackDto.Response> getAllTracksForRecord(Long recordId);

    TrackDto.Response getTrackById(Long recordId, Long trackId);

    TrackDto.Response createTrack(Long recordId, TrackDto.Request request);

    TrackDto.Response updateTrack(Long recordId, Long trackId, TrackDto.Request request);

    void deleteTrack(Long recordId, Long trackId);
}

