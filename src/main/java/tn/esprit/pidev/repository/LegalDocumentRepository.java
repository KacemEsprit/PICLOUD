package tn.esprit.pidev.repository;

import tn.esprit.pidev.entity.LegalDocument;
import tn.esprit.pidev.entity.DocumentStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for LegalDocument entity
 */
@Repository
public interface LegalDocumentRepository extends JpaRepository<LegalDocument, Long> {

    /**
     * Find documents by user ID
     */
    Page<LegalDocument> findByUserId(Long userId, Pageable pageable);

    /**
     * Find a specific document by ID and user ID (security check)
     */
    Optional<LegalDocument> findByIdAndUserId(Long id, Long userId);

    /**
     * Find all documents of a user by status
     */
    Page<LegalDocument> findByUserIdAndStatus(Long userId, DocumentStatusEnum status, Pageable pageable);

    /**
     * Find all documents by document type
     */
    Page<LegalDocument> findByDocumentTypeId(Long documentTypeId, Pageable pageable);

    /**
     * Search documents with advanced criteria for admin
     */
    @Query("SELECT ld FROM LegalDocument ld WHERE " +
           "(:userId IS NULL OR ld.userId = :userId) AND " +
           "(:documentTypeId IS NULL OR ld.documentType.id = :documentTypeId) AND " +
           "(:status IS NULL OR ld.status = :status)")
    Page<LegalDocument> searchDocuments(
            @Param("userId") Long userId,
            @Param("documentTypeId") Long documentTypeId,
            @Param("status") DocumentStatusEnum status,
            Pageable pageable);

    /**
     * Find all pending documents (for admin review)
     */
    @Query("SELECT ld FROM LegalDocument ld WHERE ld.status = 'PENDING' ORDER BY ld.uploadDate ASC")
    Page<LegalDocument> findAllPending(Pageable pageable);

    /**
     * Find documents with expired status based on expiry date
     */
    @Query("SELECT ld FROM LegalDocument ld WHERE ld.expiryDate IS NOT NULL AND ld.expiryDate < :now AND ld.status != 'EXPIRED'")
    List<LegalDocument> findExpiredDocuments(@Param("now") LocalDateTime now);

    /**
     * Find documents expiring within X days (for notifications)
     */
    @Query("SELECT ld FROM LegalDocument ld WHERE ld.expiryDate IS NOT NULL AND " +
           "ld.expiryDate BETWEEN :from AND :to AND ld.status IN ('PENDING', 'VALID')")
    List<LegalDocument> findDocumentsExpiringWithin(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /**
     * Find documents by file hash (duplicate check)
     */
    Optional<LegalDocument> findByFileHashAndUserId(String fileHash, Long userId);

    /**
     * Count pending documents for a user
     */
    long countByUserIdAndStatus(Long userId, DocumentStatusEnum status);
}
