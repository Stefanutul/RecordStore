package com.example.RecordStore.mapper;



import com.example.RecordStore.dtos.TrackDto;
import com.example.RecordStore.model.Track;
import com.example.RecordStore.model.Record;
import org.springframework.stereotype.Component;

@Component
public class TrackMapper {

    public Track toEntity(TrackDto.Request dto) {
        if (dto == null) return null;

        Track track = new Track();
        track.setTitle(dto.title());
        track.setDurationSeconds(dto.durationSeconds());
        track.setTrackKey(dto.trackKey());
        return track;
    }

    public TrackDto.Response toResponse(Track entity) {
        if (entity == null) return null;

        return new TrackDto.Response(
                entity.getId(),
                entity.getTitle(),
                entity.getDurationSeconds(),
                entity.getTrackKey()
        );
    }
}


