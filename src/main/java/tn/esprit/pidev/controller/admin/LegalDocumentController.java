package tn.esprit.pidev.controller.admin;

import tn.esprit.pidev.dto.Documents.LegalDocumentResponse;
import tn.esprit.pidev.entity.LegalDocument;
import tn.esprit.pidev.exception.FileUploadException;
import tn.esprit.pidev.service.admin.LegalDocumentService;
import tn.esprit.pidev.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * REST Controller for Legal Document Operations (USER side)
 */
@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("isAuthenticated()")
public class LegalDocumentController {

    private static final Logger logger = LoggerFactory.getLogger(LegalDocumentController.class);

    @Autowired
    private LegalDocumentService legalDocumentService;

    @Autowired
    private FileUploadService fileUploadService;

    /**
     * GET /api/documents - Get all documents for current user
     */
    @GetMapping
    public ResponseEntity<Page<LegalDocumentResponse>> getUserDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = getCurrentUserId();
        logger.info("GET /api/documents - User {} fetching their documents, page: {}", userId, page);

        Page<LegalDocument> documents = legalDocumentService.getUserDocuments(userId, page, size);
        Page<LegalDocumentResponse> responseData = documents.map(legalDocumentService::toDto);

        return ResponseEntity.ok(responseData);
    }

    /**
     * GET /api/documents/{id} - Get specific document (own only)
     */
    @GetMapping("/{id}")
    public ResponseEntity<LegalDocumentResponse> getDocument(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        logger.info("GET /api/documents/{} - User {} fetching document", id, userId);

        LegalDocument document = legalDocumentService.getDocumentById(id, userId);
        LegalDocumentResponse response = legalDocumentService.toDto(document);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/documents - Upload new document
     * Multipart form data:
     *   - file: Document file
     *   - documentTypeId: ID of document type
     *   - expiryDate: ISO format date (optional, depends on document type)
     *   - customFields: JSON string with custom metadata (optional)
     */
    @PostMapping
    public ResponseEntity<LegalDocumentResponse> uploadDocument(
            @RequestParam Long documentTypeId,
            @RequestParam(required = false) String expiryDate,
            @RequestParam(required = false) String customFields,
            @RequestParam("file") MultipartFile file) {

        Long userId = getCurrentUserId();
        logger.info("POST /api/documents - User {} uploading document type {}", userId, documentTypeId);

        try {
            LocalDateTime expiryDateTime = null;
            if (expiryDate != null && !expiryDate.isEmpty()) {
                expiryDateTime = LocalDateTime.parse(expiryDate, DateTimeFormatter.ISO_DATE_TIME);
            }

            LegalDocument document = legalDocumentService.uploadDocument(
                userId,
                documentTypeId,
                file,
                expiryDateTime,
                customFields
            );

            LegalDocumentResponse response = legalDocumentService.toDto(document);
            return ResponseEntity.status(201).body(response);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid date format: {}", e.getMessage());
            throw new FileUploadException("Invalid expiry date format. Use ISO format: yyyy-MM-ddTHH:mm:ss");
        }
    }

    /**
     * GET /api/documents/{id}/download - Download document file
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        logger.info("GET /api/documents/{}/download - User {} downloading document", id, userId);

        LegalDocument document = legalDocumentService.getDocumentById(id, userId);
        byte[] fileContent = fileUploadService.downloadFile(document.getDocumentUrl());

        // Determine content type from file extension
        String contentType = determineContentType(document.getDocumentUrl());

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"document_" + id + "\"")
            .body(fileContent);
    }

    /**
     * DELETE /api/documents/{id} - Delete document (only PENDING or REJECTED)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        logger.info("DELETE /api/documents/{} - User {} deleting document", id, userId);

        legalDocumentService.deleteDocument(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/documents/{id}/reupload - Re-upload document (for REQUEST_UPDATE status)
     */
    @PostMapping("/{id}/reupload")
    public ResponseEntity<LegalDocumentResponse> reuploadDocument(
            @PathVariable Long id,
            @RequestParam(required = false) String expiryDate,
            @RequestParam(required = false) String customFields,
            @RequestParam("file") MultipartFile file) {

        Long userId = getCurrentUserId();
        logger.info("POST /api/documents/{}/reupload - User {} re-uploading document", id, userId);

        try {
            // Get original document
            LegalDocument originalDoc = legalDocumentService.getDocumentById(id, userId);

            // Delete original document (soft delete + file removal)
            legalDocumentService.deleteDocument(id, userId);

            // Upload as new document
            LocalDateTime expiryDateTime = null;
            if (expiryDate != null && !expiryDate.isEmpty()) {
                expiryDateTime = LocalDateTime.parse(expiryDate, DateTimeFormatter.ISO_DATE_TIME);
            } else {
                expiryDateTime = originalDoc.getExpiryDate();
            }

            LegalDocument newDocument = legalDocumentService.uploadDocument(
                userId,
                originalDoc.getDocumentTypeId(),
                file,
                expiryDateTime,
                customFields
            );

            LegalDocumentResponse response = legalDocumentService.toDto(newDocument);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid date format: {}", e.getMessage());
            throw new FileUploadException("Invalid expiry date format. Use ISO format: yyyy-MM-ddTHH:mm:ss");
        }
    }

    /**
     * Helper: Get current user ID from security context
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // In a real implementation, extract user ID from JWT token or UserDetails
        // For now, return a placeholder
        return 1L;
    }

    /**
     * Helper: Determine content type based on file extension
     */
    private String determineContentType(String filePath) {
        if (filePath.endsWith(".pdf")) return "application/pdf";
        if (filePath.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (filePath.endsWith(".doc")) return "application/msword";
        if (filePath.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (filePath.endsWith(".xls")) return "application/vnd.ms-excel";
        if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) return "image/jpeg";
        if (filePath.endsWith(".png")) return "image/png";
        if (filePath.endsWith(".gif")) return "image/gif";
        if (filePath.endsWith(".webp")) return "image/webp";
        return "application/octet-stream";
    }
}

