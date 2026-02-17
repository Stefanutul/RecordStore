package com.example.RecordStore.repository;

import com.example.RecordStore.model.AppUser;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);

    @Modifying
    @Transactional
    @Query("update AppUser u set u.passwordHash = :hash where u.username = :username")
    int updatePasswordHashByUsername(@Param("username") String username,
                                     @Param("hash") String hash);
}
