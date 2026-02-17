package com.example.RecordStore.mapper;


import com.example.RecordStore.dtos.RecordDto;
import com.example.RecordStore.model.Record;
import org.springframework.stereotype.Component;

@Component
public class RecordMapper {

    public Record toEntity(RecordDto.Request dto) {
        if (dto == null) return null;

        Record record = new Record();
        record.setTitle(dto.title());
        record.setArtist(dto.artist());
        record.setLabel(dto.label());
        record.setGenre(dto.genre());
        record.setPublishingYear(dto.publishingYear());
        record.setPrice(dto.price());
        return record;
    }

    public RecordDto.Response toResponse(Record entity) {
        if (entity == null) return null;

        return new RecordDto.Response(
                entity.getId(),
                entity.getTitle(),
                entity.getArtist(),
                entity.getLabel(),
                entity.getGenre(),
                entity.getPublishingYear(),
                entity.getPrice()
        );
    }
}

