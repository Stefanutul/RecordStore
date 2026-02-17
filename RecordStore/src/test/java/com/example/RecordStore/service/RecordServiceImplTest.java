package com.example.RecordStore.service;

import com.example.RecordStore.dtos.RecordDto;
import com.example.RecordStore.exceptions.RecordNotFoundException;
import com.example.RecordStore.mapper.RecordMapper;
import com.example.RecordStore.model.AppUser;
import com.example.RecordStore.model.Record;
import com.example.RecordStore.model.Role;
import com.example.RecordStore.repository.RecordRepository;
import com.example.RecordStore.service.impl.RecordServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordServiceImplTest {

    @Mock private RecordRepository repository;
    @Mock private RecordMapper mapper;
    @Mock private AppUserService appUserService;
    @InjectMocks private RecordServiceImpl service;

    private final String USERNAME = "testuser";
    private AppUser owner;
    private Record record;
    private RecordDto.Response responseDto;
    private RecordDto.Request requestDto;

    private void setId(AppUser user, Long id) throws Exception {
        Field idField = AppUser.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);
    }

    @BeforeEach
    void setUp() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken(USERNAME, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        owner = new AppUser(USERNAME, "encoded", Set.of(Role.USER));
        setId(owner, 1L);

        record = new Record();
        record.setId(1L);
        record.setTitle("Abbey Road");
        record.setArtist("The Beatles");
        record.setGenre("Rock");
        record.setPublishingYear(1969);
        record.setPrice(new BigDecimal("29.99"));
        record.setOwner(owner);

        responseDto = new RecordDto.Response(1L, "Abbey Road", "The Beatles", null, "Rock", 1969, new BigDecimal("29.99"));
        requestDto = new RecordDto.Request("Abbey Road", "The Beatles", null, "Rock", 1969, new BigDecimal("29.99"));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllRecords_returnsListForCurrentUser() {
        when(repository.findAllByOwnerUsername(USERNAME)).thenReturn(List.of(record));
        when(mapper.toResponse(record)).thenReturn(responseDto);

        List<RecordDto.Response> result = service.getAllRecords();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Abbey Road");
        verify(repository).findAllByOwnerUsername(USERNAME);
    }

    @Test
    void getRecordById_found_returnsResponse() {
        when(repository.findByIdAndOwnerUsername(1L, USERNAME)).thenReturn(Optional.of(record));
        when(mapper.toResponse(record)).thenReturn(responseDto);

        Optional<RecordDto.Response> result = service.getRecordById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().title()).isEqualTo("Abbey Road");
    }

    @Test
    void getRecordById_notFound_returnsEmpty() {
        when(repository.findByIdAndOwnerUsername(99L, USERNAME)).thenReturn(Optional.empty());

        Optional<RecordDto.Response> result = service.getRecordById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void createRecord_setsOwnerFromAuth() {
        when(appUserService.getByUsername(USERNAME)).thenReturn(owner);
        when(mapper.toEntity(requestDto)).thenReturn(record);
        when(repository.save(any(Record.class))).thenReturn(record);
        when(mapper.toResponse(record)).thenReturn(responseDto);

        RecordDto.Response result = service.createRecord(requestDto);

        assertThat(result.title()).isEqualTo("Abbey Road");
        verify(repository).save(argThat(r -> r.getOwner().equals(owner)));
    }

    @Test
    void updateRecord_success() {
        when(repository.findByIdAndOwnerUsername(1L, USERNAME)).thenReturn(Optional.of(record));
        when(repository.save(any(Record.class))).thenReturn(record);
        when(mapper.toResponse(record)).thenReturn(responseDto);

        RecordDto.Response result = service.updateRecord(1L, requestDto);

        assertThat(result).isNotNull();
        verify(repository).save(record);
    }

    @Test
    void updateRecord_notOwned_throwsRecordNotFoundException() {
        when(repository.findByIdAndOwnerUsername(1L, USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateRecord(1L, requestDto))
                .isInstanceOf(RecordNotFoundException.class);
    }

    @Test
    void deleteRecord_success() {
        when(repository.findByIdAndOwnerUsername(1L, USERNAME)).thenReturn(Optional.of(record));

        service.deleteRecord(1L);

        verify(repository).delete(record);
    }

    @Test
    void deleteRecord_notOwned_throwsRecordNotFoundException() {
        when(repository.findByIdAndOwnerUsername(1L, USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteRecord(1L))
                .isInstanceOf(RecordNotFoundException.class);
    }
}
