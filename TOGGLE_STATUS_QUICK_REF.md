# Quick Reference: Toggle Document Status

## 🚀 New Endpoint

```
POST /api/admin/documents/{documentId}/toggle-status
Authorization: Bearer JWT_TOKEN
```

## 📋 What It Does

Toggles a document status between:
- **VALID** → **REJECTED**
- **REJECTED** → **VALID**

## ✅ Requirements

- ✅ Document must exist
- ✅ Document status must be VALID or REJECTED
- ✅ User must have ADMIN role
- ✅ Valid JWT token required

## ❌ Cannot Toggle

- PENDING ❌
- EXPIRED ❌
- REQUEST_UPDATE ❌

## 📝 Example Request

```bash
curl -X POST http://localhost:8081/api/admin/documents/1/toggle-status \
  -H "Authorization: Bearer eyJhbGc..." \
  -H "Content-Type: application/json"
```

## 📋 Example Response (200 OK)

```json
{
  "id": 1,
  "userId": 5,
  "documentTypeId": 2,
  "documentUrl": "5/1775606291_6f83f7b9.pdf",
  "uploadDate": "2026-04-11T10:30:00",
  "expiryDate": "2027-04-11T00:00:00",
  "status": "REJECTED"
}
```

## 🔄 Status Flow

```
Before:  status = "VALID"
Request: POST /api/admin/documents/1/toggle-status
After:   status = "REJECTED"

---

Before:  status = "REJECTED"  
Request: POST /api/admin/documents/1/toggle-status
After:   status = "VALID"
```

## 💾 Files Modified

✅ `src/main/java/tn/esprit/pidev/service/admin/LegalDocumentService.java`
- Added `toggleDocumentStatus(Long documentId, Long adminUserId)` method

✅ `src/main/java/tn/esprit/pidev/controller/admin/AdminDocumentController.java`
- Added `toggleDocumentStatus()` endpoint

## 🧪 Test It

```bash
# 1. Get a document that has status VALID or REJECTED
GET /api/admin/documents/1

# 2. Note the current status
# 3. Toggle it
POST /api/admin/documents/1/toggle-status

# 4. Verify status changed
GET /api/admin/documents/1
```

## ⚡ Use Cases

1. **Quick Fix:** Accidentally approved? Toggle back to reject
2. **Review Again:** Toggle to re-review a document
3. **Batch Updates:** Loop through multiple documents to toggle

## 🔐 Security

- Admin role required
- JWT token required
- Full audit logging
- Admin ID tracked in logs

---

**That's it! The method is ready to use!** 🎉

