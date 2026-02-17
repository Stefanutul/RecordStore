package com.example.RecordStore.service;

import com.example.RecordStore.dtos.TrackDto;
import com.example.RecordStore.exceptions.RecordNotFoundException;
import com.example.RecordStore.exceptions.TrackNotFoundException;
import com.example.RecordStore.mapper.TrackMapper;
import com.example.RecordStore.model.Record;
import com.example.RecordStore.model.Track;
import com.example.RecordStore.repository.RecordRepository;
import com.example.RecordStore.repository.TrackRepository;
import com.example.RecordStore.service.impl.TrackServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackServiceImplTest {

    @Mock private TrackRepository trackRepository;
    @Mock private RecordRepository recordRepository;
    @Mock private TrackMapper trackMapper;
    @InjectMocks private TrackServiceImpl service;

    private Record record;
    private Track track;
    private TrackDto.Response responseDto;
    private TrackDto.Request requestDto;

    @BeforeEach
    void setUp() {
        record = new Record();
        record.setId(1L);

        track = new Track();
        track.setId(1L);
        track.setTitle("Come Together");
        track.setDurationSeconds(259);
        track.setTrackKey("A");
        track.setRecord(record);

        responseDto = new TrackDto.Response(1L, "Come Together", 259, "A");
        requestDto = new TrackDto.Request("Come Together", 259, "A");
    }

    @Test
    void getAllTracksForRecord_success() {
        when(recordRepository.existsById(1L)).thenReturn(true);
        when(trackRepository.findByRecordId(1L)).thenReturn(List.of(track));
        when(trackMapper.toResponse(track)).thenReturn(responseDto);

        List<TrackDto.Response> result = service.getAllTracksForRecord(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Come Together");
    }

    @Test
    void getAllTracksForRecord_recordNotFound_throwsRecordNotFoundException() {
        when(recordRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.getAllTracksForRecord(99L))
                .isInstanceOf(RecordNotFoundException.class);
    }

    @Test
    void getTrackById_success() {
        when(trackRepository.findById(1L)).thenReturn(Optional.of(track));
        when(trackMapper.toResponse(track)).thenReturn(responseDto);

        TrackDto.Response result = service.getTrackById(1L, 1L);

        assertThat(result.title()).isEqualTo("Come Together");
    }

    @Test
    void getTrackById_wrongRecord_throwsTrackNotFoundException() {
        when(trackRepository.findById(1L)).thenReturn(Optional.of(track));

        assertThatThrownBy(() -> service.getTrackById(99L, 1L))
                .isInstanceOf(TrackNotFoundException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    void createTrack_success() {
        when(recordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(trackMapper.toEntity(requestDto)).thenReturn(track);
        when(trackRepository.save(any(Track.class))).thenReturn(track);
        when(trackMapper.toResponse(track)).thenReturn(responseDto);

        TrackDto.Response result = service.createTrack(1L, requestDto);

        assertThat(result.title()).isEqualTo("Come Together");
        verify(trackRepository).save(argThat(t -> t.getRecord().equals(record)));
    }

    @Test
    void createTrack_recordNotFound_throwsRecordNotFoundException() {
        when(recordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createTrack(99L, requestDto))
                .isInstanceOf(RecordNotFoundException.class);
    }

    @Test
    void deleteTrack_success() {
        when(trackRepository.findById(1L)).thenReturn(Optional.of(track));

        service.deleteTrack(1L, 1L);

        verify(trackRepository).delete(track);
    }

    @Test
    void deleteTrack_wrongRecord_throwsTrackNotFoundException() {
        when(trackRepository.findById(1L)).thenReturn(Optional.of(track));

        assertThatThrownBy(() -> service.deleteTrack(99L, 1L))
                .isInstanceOf(TrackNotFoundException.class);
    }
}
