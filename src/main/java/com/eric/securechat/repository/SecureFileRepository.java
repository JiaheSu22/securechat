package com.eric.securechat.repository;

import com.eric.securechat.model.SecureFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SecureFileRepository extends JpaRepository<SecureFile, UUID> {
    Optional<SecureFile> findByStoragePath(String storagePath);
}
