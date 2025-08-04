package com.eric.securechat.repository;

import com.eric.securechat.model.SecureFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for SecureFile entity operations.
 * Provides data access methods for file management.
 */
@Repository
public interface SecureFileRepository extends JpaRepository<SecureFile, UUID> {
    
    /**
     * Finds a file by its storage path.
     * 
     * @param storagePath The storage path to search for
     * @return Optional containing the file if found, empty otherwise
     */
    Optional<SecureFile> findByStoragePath(String storagePath);
}
