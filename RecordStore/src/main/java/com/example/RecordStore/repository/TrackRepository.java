package com.example.RecordStore.repository;

import com.example.RecordStore.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrackRepository extends JpaRepository<Track , Long> {
    List<Track> findByRecordId(Long recordId);
}
