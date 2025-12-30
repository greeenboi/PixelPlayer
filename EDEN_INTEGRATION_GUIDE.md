# PixelPlayer Eden API Integration - Implementation Guide

## Overview
This document provides guidance for completing the transformation of PixelPlayer from a local music player to a streaming app using the Eden Server API.

## Completed Work

### Phase 1: Network & Data Layer Foundation ✅
- **Eden API Service** (`EdenApiService.kt`): Complete Retrofit interface with all endpoints
- **API Models** (models package): Request/Response DTOs for Auth, Tracks, Albums, Artists
- **AuthInterceptor**: Adds JWT Bearer token to all API requests
- **StreamUrlCache**: LRU cache with 10-minute TTL for stream URLs
- **UserPreferencesRepository**: Extended with auth token storage methods
- **AppModule.kt**: DI setup for Eden API, including Retrofit instance and interceptors

### Phase 2: Authentication System ✅
- **AuthRepository**: Handles login/signup operations and token management
- **AuthViewModel**: ViewModel with auth state and error handling
- **LoginScreen**: Material 3 UI with email/password fields
- **SignupScreen**: Material 3 UI with name/email/password fields
- **Navigation**: Auth routes added to AppNavigation with proper transitions
- **MainActivity**: Auth gate implemented - users must login before accessing app

### Phase 3: Streaming & Download Infrastructure ✅
- **DownloadedTrackEntity**: Room entity for offline tracks
- **DownloadedTrackDao**: DAO with CRUD operations for downloads
- **PixelPlayDatabase**: Updated to v12 with migration for downloaded_tracks table
- **DownloadRepository**: Manages download operations and file cleanup
- **TrackDownloadWorker**: HiltWorker for background downloads with progress

### Phase 4: Repository Integration ✅ (Partial)
- **EdenMusicRepository**: Complete implementation with data mapping
  - Maps TrackWithRelationsResponse → Song
  - Converts Eden API responses to PixelPlayer models
  - Handles stream URL caching
  - Provides methods for tracks, albums, artists, and search

## Remaining Work

### A. MusicRepositoryImpl Integration

The `MusicRepositoryImpl` currently scans local files using MediaStore. You need to integrate EdenMusicRepository as the primary data source.

**Steps:**
1. Inject `EdenMusicRepository` into `MusicRepositoryImpl` constructor
2. Add a preference for "streaming mode" vs "local mode"
3. Update `getAudioFiles()` to call `edenMusicRepository.getPublishedTracksFlow()`
4. Update `getAlbums()` to fetch from Eden API
5. Update `getArtists()` to fetch from Eden API
6. Update search methods to use Eden API
7. Keep local file scanning as fallback or secondary mode

**Example:**
```kotlin
@Singleton
class MusicRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val searchHistoryDao: SearchHistoryDao,
    private val musicDao: MusicDao,
    private val lyricsRepository: LyricsRepository,
    private val edenMusicRepository: EdenMusicRepository // ADD THIS
) : MusicRepository {

    // Add streaming mode preference
    private val isStreamingMode: Flow<Boolean> = 
        userPreferencesRepository.authTokenFlow
            .map { it != null }

    override fun getAudioFiles(): Flow<List<Song>> {
        return isStreamingMode.flatMapLatest { streaming ->
            if (streaming) {
                // Fetch from Eden API
                edenMusicRepository.getPublishedTracksFlow()
            } else {
                // Use existing local file scanning
                allSongsFlow // existing implementation
            }
        }
    }
    
    // Similar updates for getAlbums(), getArtists(), etc.
}
```

### B. MusicService Updates for Streaming

Update `MusicService.kt` to handle stream URLs and check for downloaded tracks.

**Steps:**
1. Inject `DownloadRepository` and `EdenMusicRepository`
2. Before playing a track, check if it's downloaded
3. If downloaded, use local file URI
4. If not downloaded, fetch stream URL from cache or API
5. Handle stream URL expiration (re-fetch if needed)

**Example:**
```kotlin
private suspend fun prepareTrack(song: Song) {
    val uri = if (downloadRepository.isTrackDownloaded(song.id).first()) {
        // Use local file
        downloadRepository.getLocalUri(song.id)
    } else {
        // Get streaming URL
        val streamUrl = edenMusicRepository.getStreamUrl(song.id)
        Uri.parse(streamUrl)
    }
    
    exoPlayer.setMediaItem(MediaItem.fromUri(uri))
    exoPlayer.prepare()
}
```

### C. UI Updates

#### 1. Download Button in Song Items
Add download functionality to song list items and player UI.

**Files to modify:**
- `app/src/main/java/com/theveloper/pixelplay/presentation/components/SongItem.kt`
- Player screen composables

**Example:**
```kotlin
@Composable
fun SongItem(
    song: Song,
    onDownloadClick: (String) -> Unit
) {
    val downloadRepository: DownloadRepository = hiltViewModel()
    val isDownloaded by downloadRepository.isTrackDownloaded(song.id).collectAsState(false)
    
    Row {
        // ... existing song UI ...
        
        IconButton(onClick = { 
            if (isDownloaded) {
                // Delete download
                scope.launch { downloadRepository.deleteDownload(song.id) }
            } else {
                // Start download
                scope.launch { downloadRepository.downloadTrack(song.id) }
            }
        }) {
            Icon(
                imageVector = if (isDownloaded) Icons.Default.Check else Icons.Default.Download,
                contentDescription = if (isDownloaded) "Downloaded" else "Download"
            )
        }
    }
}
```

#### 2. Downloads Section in Library
Add a new tab or section showing downloaded tracks.

**File:** `app/src/main/java/com/theveloper/pixelplay/presentation/screens/LibraryScreen.kt`

Add a "Downloads" tab that displays tracks from `downloadRepository.getAllDownloadedTracks()`.

#### 3. Logout Button in Settings
Add logout functionality to clear auth data and return to login.

**File:** `app/src/main/java/com/theveloper/pixelplay/presentation/screens/SettingsScreen.kt`

**Example:**
```kotlin
@Composable
fun SettingsScreen() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val userInfo by authViewModel.userInfo.collectAsState()
    
    Column {
        // ... existing settings ...
        
        // User info section
        userInfo?.let { user ->
            ListItem(
                headlineContent = { Text(user.name) },
                supportingContent = { Text(user.email) }
            )
        }
        
        // Logout button
        Button(onClick = { authViewModel.logout() }) {
            Text("Logout")
        }
    }
}
```

### D. Error Handling

Add error handling for common scenarios:

1. **401 Unauthorized**: Token expired
   - Clear auth data
   - Navigate to login screen
   - Show message to user

2. **Network Errors**: No internet connection
   - Show error message
   - Attempt to use cached data
   - Fall back to downloaded tracks

3. **Stream URL Expiration**: Cached URL expired
   - Re-fetch from API
   - Seamlessly continue playback

**Example Interceptor:**
```kotlin
class UnauthorizedInterceptor @Inject constructor(
    private val authRepository: AuthRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        
        if (response.code == 401) {
            // Token expired - clear auth data
            runBlocking { authRepository.logout() }
            // Navigate to login handled by MainActivity's isAuthenticated flow
        }
        
        return response
    }
}
```

### E. Build Configuration

Fix the AGP version issue:

**File:** `gradle/libs.versions.toml`

Change:
```toml
agp = "8.5.2"
```

To a stable version like:
```toml
agp = "8.3.0"
```

Or check the latest stable version from: https://developer.android.com/build/releases/gradle-plugin

### F. Testing

1. **Authentication Flow**
   - Test login with valid/invalid credentials
   - Test signup with valid/invalid data
   - Test auth gate (redirects to login when not authenticated)
   - Test logout functionality

2. **Streaming Playback**
   - Test playing tracks from Eden API
   - Test stream URL caching
   - Test URL expiration and re-fetch
   - Test playback with poor network conditions

3. **Download Functionality**
   - Test downloading a track
   - Test download progress tracking
   - Test playing downloaded tracks offline
   - Test deleting downloads

4. **Error Scenarios**
   - Test with no internet connection
   - Test with invalid auth token (401)
   - Test with API errors (500, etc.)

## Architecture Notes

### Data Flow
```
User Action
    ↓
ViewModel
    ↓
Repository (EdenMusicRepository)
    ↓
Eden API Service
    ↓
Network (OkHttp + AuthInterceptor)
    ↓
Eden Server
```

### Caching Strategy
- **Stream URLs**: 10-minute LRU cache (StreamUrlCache)
- **Auth Token**: DataStore (persistent)
- **Downloaded Tracks**: Local files + Room database

### Offline Support
- Downloaded tracks stored in `app.filesDir/downloads/`
- Metadata in Room database (DownloadedTrackEntity)
- Playback automatically uses local file if available

## Security Considerations

1. **JWT Token Storage**: Stored in encrypted DataStore
2. **HTTPS Only**: All API calls use HTTPS
3. **Stream URL Expiration**: URLs expire to prevent unauthorized sharing
4. **No Credentials in Logs**: AuthInterceptor uses sensitive logging carefully

## Performance Optimizations

1. **Pagination**: API calls use pagination (20 items per page)
2. **Stream URL Caching**: Reduces API calls for repeated playback
3. **Background Downloads**: WorkManager ensures downloads continue in background
4. **Flow-based Updates**: Reactive UI updates using StateFlow/Flow

## Next Steps

1. Fix AGP version and ensure project builds
2. Wire EdenMusicRepository into MusicRepositoryImpl
3. Update MusicService for streaming
4. Add download buttons to UI
5. Add logout functionality
6. Test end-to-end with real Eden API
7. Handle error scenarios gracefully
8. Run security scan (CodeQL)
9. Performance test with large playlists
10. User acceptance testing

## API Testing

Use the Eden API documentation at:
https://eden-server.suvan-gowrishanker-204.workers.dev/scalar

Test endpoints with curl or Postman before integrating.

Example:
```bash
# Login
curl -X POST https://eden-server.suvan-gowrishanker-204.workers.dev/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# Get published tracks (with auth token)
curl https://eden-server.suvan-gowrishanker-204.workers.dev/api/tracks/published \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## Conclusion

The foundation for streaming functionality is complete. The remaining work focuses on:
- Wiring existing components together
- Adding UI for new features
- Error handling and testing
- Build configuration fixes

All major architectural decisions have been made and infrastructure is in place.
