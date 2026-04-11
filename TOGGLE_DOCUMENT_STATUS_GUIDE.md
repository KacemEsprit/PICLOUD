# Toggle Document Status - Implementation Guide

## ✅ Feature Implemented

A new method to toggle document status between **REJECTED** and **VALID** has been successfully added to your backend.

---

## 📋 What Was Added

### 1. Service Method: `toggleDocumentStatus()` ✅
**File:** `src/main/java/tn/esprit/pidev/service/admin/LegalDocumentService.java`

```java
/**
 * Toggle document status between REJECTED and VALID (ADMIN operation)
 * Allows admin to change a rejected document to approved and vice versa
 */
@Transactional
public LegalDocument toggleDocumentStatus(Long documentId, Long adminUserId)
```

**Features:**
- ✅ Toggles between VALID → REJECTED
- ✅ Toggles between REJECTED → VALID
- ✅ Only these two statuses can be toggled
- ✅ Logs all status changes
- ✅ Returns the updated document
- ✅ Throws exception if document has other status (PENDING, EXPIRED, REQUEST_UPDATE)

**Validation:**
- Document must exist
- Document status must be VALID or REJECTED
- Cannot toggle PENDING, EXPIRED, or REQUEST_UPDATE documents

---

### 2. REST API Endpoint: `/api/admin/documents/{id}/toggle-status` ✅
**File:** `src/main/java/tn/esprit/pidev/controller/admin/AdminDocumentController.java`

```java
@PostMapping("/{id}/toggle-status")
public ResponseEntity<LegalDocumentResponse> toggleDocumentStatus(
    @PathVariable @Positive Long id)
```

---

## 🚀 How to Use

### API Endpoint

**Request:**
```http
POST /api/admin/documents/{documentId}/toggle-status
Authorization: Bearer YOUR_JWT_TOKEN
```

**Path Parameters:**
- `documentId` (Long) - ID of the document to toggle

**Response (200 OK):**
```json
{
  "id": 1,
  "userId": 1,
  "documentTypeId": 1,
  "documentUrl": "1/1775606291_6f83f7b9.pdf",
  "status": "REJECTED",
  "uploadDate": "2026-04-11T10:30:00",
  "expiryDate": "2027-04-11T10:30:00"
}
```

---

## 📊 Status Transitions

```
Allowed Transitions:

VALID    ←→    REJECTED
  ↓           ↓
Click Toggle  Click Toggle
  ↓           ↓
REJECTED  ←→  VALID


Not Allowed (will throw exception):

PENDING        ❌ Cannot toggle
EXPIRED        ❌ Cannot toggle
REQUEST_UPDATE ❌ Cannot toggle
```

---

## 🔄 Examples

### Example 1: Toggle from VALID to REJECTED
```bash
# Document currently has status: VALID
curl -X POST http://localhost:8081/api/admin/documents/1/toggle-status \
  -H "Authorization: Bearer YOUR_TOKEN"

# Response: Status changes to REJECTED
{
  "id": 1,
  "status": "REJECTED"
}
```

### Example 2: Toggle from REJECTED back to VALID
```bash
# Document currently has status: REJECTED
curl -X POST http://localhost:8081/api/admin/documents/1/toggle-status \
  -H "Authorization: Bearer YOUR_TOKEN"

# Response: Status changes back to VALID
{
  "id": 1,
  "status": "VALID"
}
```

### Example 3: Error - Try to toggle PENDING document
```bash
# Document has status: PENDING
curl -X POST http://localhost:8081/api/admin/documents/1/toggle-status \
  -H "Authorization: Bearer YOUR_TOKEN"

# Response: 400 Bad Request
{
  "error": "Cannot toggle status for document with status: PENDING. Only VALID and REJECTED documents can be toggled."
}
```

---

## 📝 Code Details

### Service Layer Implementation

```java
@Transactional
public LegalDocument toggleDocumentStatus(Long documentId, Long adminUserId) {
    // 1. Find document
    LegalDocument document = legalDocumentRepository.findById(documentId)
        .orElseThrow(() -> new DocumentNotFoundException(documentId));

    DocumentStatusEnum currentStatus = document.getStatus();

    // 2. Toggle based on current status
    if (currentStatus == DocumentStatusEnum.VALID) {
        document.setStatus(DocumentStatusEnum.REJECTED);
    } else if (currentStatus == DocumentStatusEnum.REJECTED) {
        document.setStatus(DocumentStatusEnum.VALID);
    } else {
        // Only VALID and REJECTED can be toggled
        throw new InvalidDocumentException(
            "Cannot toggle status for document with status: " + currentStatus + 
            ". Only VALID and REJECTED documents can be toggled."
        );
    }

    // 3. Save and return
    LegalDocument saved = legalDocumentRepository.save(document);
    logger.info("✓ Document status toggled: ID {} - New Status: {}", 
                documentId, saved.getStatus());
    return saved;
}
```

### Controller Implementation

```java
@PostMapping("/{id}/toggle-status")
public ResponseEntity<LegalDocumentResponse> toggleDocumentStatus(
        @PathVariable @Positive Long id) {

    Long adminUserId = getCurrentUserId();
    logger.info("POST /api/admin/documents/{}/toggle-status - Admin {}", id, adminUserId);

    LegalDocument document = legalDocumentService.toggleDocumentStatus(id, adminUserId);
    LegalDocumentResponse response = legalDocumentService.toDto(document);

    return ResponseEntity.ok(response);
}
```

---

## 🔐 Security

- ✅ **ADMIN Only:** Requires `@PreAuthorize("hasRole('ADMIN')")`
- ✅ **JWT Protected:** Requires valid Bearer token
- ✅ **No Body Required:** Simple POST with just the ID
- ✅ **Logged:** All actions are logged with admin ID and timestamp

---

## ✨ Use Cases

### 1. Quick Corrections
Admin accidentally approved a document → Toggle back to REJECTED immediately

### 2. Second Review
Admin wants to re-review an approved document → Toggle to REJECTED, review again, toggle back to VALID

### 3. Status Management
Easy way to manage document approval status without separate approve/reject endpoints

### 4. Bulk Operations
Toggle multiple documents quickly using a loop:
```bash
for i in 1 2 3 4 5; do
  curl -X POST http://localhost:8081/api/admin/documents/$i/toggle-status \
    -H "Authorization: Bearer TOKEN"
done
```

---

## 📊 Logging Output

When you toggle a document status, you'll see logs like:

```
Admin 1 toggling status for document 5
Document 5 status changed from VALID to REJECTED by admin 1
✓ Document status toggled: ID 5 - New Status: REJECTED
```

Or:

```
Admin 1 toggling status for document 5
Document 5 status changed from REJECTED to VALID by admin 1
✓ Document status toggled: ID 5 - New Status: VALID
```

---

## 🧪 Testing

### Test Case 1: Toggle VALID to REJECTED
```
Given: Document with status VALID
When: POST /api/admin/documents/1/toggle-status
Then: Document status changes to REJECTED
      Response 200 OK
```

### Test Case 2: Toggle REJECTED to VALID
```
Given: Document with status REJECTED
When: POST /api/admin/documents/1/toggle-status
Then: Document status changes to VALID
      Response 200 OK
```

### Test Case 3: Try to toggle PENDING
```
Given: Document with status PENDING
When: POST /api/admin/documents/1/toggle-status
Then: Returns 400 Bad Request with error message
```

### Test Case 4: Document not found
```
Given: No document with ID 999
When: POST /api/admin/documents/999/toggle-status
Then: Returns 404 Not Found
```

---

## 📚 API Comparison

| Operation | Endpoint | Behavior |
|-----------|----------|----------|
| **Approve** | `POST /api/admin/documents/{id}/approve` | PENDING → VALID |
| **Reject** | `POST /api/admin/documents/{id}/reject` | PENDING → REJECTED |
| **Toggle** | `POST /api/admin/documents/{id}/toggle-status` | VALID ↔ REJECTED |
| **Request Update** | `POST /api/admin/documents/{id}/request-update` | PENDING → REQUEST_UPDATE |

---

## 🎯 Summary

✅ **Method Added:** `toggleDocumentStatus(Long documentId, Long adminUserId)`  
✅ **Endpoint Added:** `POST /api/admin/documents/{id}/toggle-status`  
✅ **Status Support:** VALID ↔ REJECTED  
✅ **Validation:** Only VALID and REJECTED documents can toggle  
✅ **Logging:** Full audit trail with admin ID  
✅ **Security:** Admin role required, JWT protected  
✅ **Ready to Use:** No additional changes needed

**Start using:** Send a POST request to `/api/admin/documents/{documentId}/toggle-status`


