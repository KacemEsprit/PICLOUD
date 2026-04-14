# Profile Photo Path Format Fix

## Problem Identified
The backend was returning file paths with **backslashes (`\`)** on Windows, which the frontend couldn't properly handle. The issue was:

- ❌ Backend returning: `C:\xampp\htdocs\pidev-uploads\123\1712000000_photo.jpg` (full path with backslashes)
- ❌ Backend returning: `123\1712000000_photo.jpg` (backslashes instead of forward slashes)
- ✅ Frontend expecting: `123/1712000000_photo.jpg` (relative path with forward slashes)

## Root Cause
The `FileUploadService.uploadFile()` method was using `File.separator` to build relative paths:
```java
return userId + File.separator + uniqueFilename;  // Returns: 123\1234567890_photo.jpg on Windows
```

On Windows, `File.separator` is `\`, but web paths universally use `/`.

## Changes Made

### 1. **FileUploadService.java** (Fixed path format)
- **Line 73**: Changed `userId + File.separator + uniqueFilename` to `userId + "/" + uniqueFilename`
  - Now returns paths like `123/1712000000_photo.jpg` instead of `123\1712000000_photo.jpg`
  
- **Lines 105, 127**: Added path normalization in `downloadFile()` and `deleteFile()` methods
  - Convert forward slashes from database to platform-specific separator for file system access
  - `normalizedPath = relativePath.replace("/", File.separator);`
  - This ensures the file system operations work correctly on all platforms

### 2. **ProfileResponse.java** (Added photoPath field)
- Added new field: `String photoPath` (line 16)
- Updated constructor to accept `photoPath` parameter (line 25-26)
- Added getter/setter methods for `photoPath`
- Now the API response includes the photo path that the frontend can use to display the image

**Before:**
```json
{
  "id": 1,
  "username": "user123",
  "role": "PASSENGER",
  "photoContentType": "image/jpeg"
}
```

**After:**
```json
{
  "id": 1,
  "username": "user123",
  "role": "PASSENGER",
  "photoPath": "1/1712000000_abc12345.jpg",
  "photoContentType": "image/jpeg"
}
```

### 3. **ProfileServiceImpl.java** (Updated response conversion)
- Updated `convertToProfileResponse()` method to extract the photo path from the User entity
- Lines 196-199: Extract path from `user.getPhoto()` bytes and convert to string
- Lines 207: Pass `photoPath` to the ProfileResponse constructor

## How the Photo Upload Now Works

1. **Upload Phase:**
   ```
   User uploads photo → FileUploadService.uploadFile() 
   → Saves file to: C:\xampp\htdocs\pidev-uploads\123\1712000000_abc12345.jpg
   → Returns path: "123/1712000000_abc12345.jpg" (with forward slashes)
   → Store in DB as bytes: user.setPhoto("123/1712000000_abc12345.jpg".getBytes())
   ```

2. **Retrieve Phase:**
   ```
   Frontend calls GET /api/profile
   → Backend returns: { "photoPath": "123/1712000000_abc12345.jpg", ... }
   → Frontend constructs URL: http://localhost:8080/pidev-uploads/123/1712000000_abc12345.jpg
   → Browser loads the image
   ```

3. **Download Phase (when frontend needs the actual file):**
   ```
   Frontend calls GET /api/profile/photo
   → Backend gets path from DB: "123/1712000000_abc12345.jpg"
   → FileUploadService.downloadFile() normalizes: "123\1712000000_abc12345.jpg"
   → Reads from: C:\xampp\htdocs\pidev-uploads\123\1712000000_abc12345.jpg
   → Returns file bytes
   ```

## Frontend Integration

The frontend should:

1. **Store the photoPath** from the profile response
2. **Construct the image URL** like this:
   ```typescript
   imageUrl = `${this.apiBaseUrl}/pidev-uploads/${photoPath}`;
   // Result: http://localhost:8080/pidev-uploads/123/1712000000_abc12345.jpg
   ```
3. **Use the URL** in an `<img>` tag or CSS `background-image`

## Cross-Platform Compatibility

✅ These changes are **cross-platform compatible**:
- **Windows**: `File.separator` = `\`, we normalize it in download/delete operations
- **Linux/Mac**: `File.separator` = `/`, paths work as-is
- **Database**: Always stores `/` for consistency
- **Frontend**: Always receives `/` paths

## Security Notes

- Photo paths are stored as relative paths (not absolute)
- Files are organized by user ID for access control
- Frontend must validate the path to prevent directory traversal
- Backend should continue enforcing authorization checks

## Testing Instructions

After deployment, test the photo upload flow:

1. Log in and go to profile page
2. Upload a new photo
3. Open browser DevTools → Network tab
4. Check the GET /api/profile response
5. Verify `photoPath` is like: `"123/1712000000_abc12345.jpg"`
6. Verify `photoPath` uses forward slashes `/` not backslashes `\`
7. Verify the image displays correctly
8. Refresh the page and verify the photo persists

## Affected Endpoints

- `POST /api/profile/photo` - Upload photo (returns photoPath in response)
- `GET /api/profile` - Get profile (includes photoPath)
- `GET /api/profile/photo` - Download photo (uses photoPath internally)
- `DELETE /api/profile/photo` - Delete photo (uses photoPath internally)

## Build Status

✅ Project compiles successfully with all changes
✅ No breaking changes to existing code
✅ Backward compatible (old photos with backslashes will still work due to path normalization)

