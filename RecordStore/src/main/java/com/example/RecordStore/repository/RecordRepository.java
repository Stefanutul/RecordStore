package com.example.RecordStore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.RecordStore.model.Record;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecordRepository extends JpaRepository<Record , Long> {

    List<Record> findAllByOwnerUsernameOrderByIdDesc(String username);

    Optional<Record> findByIdAndOwnerUsername(Long id, String username);

    boolean existsByIdAndOwnerUsername(Long id, String username);

    List<Record> findAllByOwnerUsername(String username);


}
