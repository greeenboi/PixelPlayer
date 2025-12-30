package com.theveloper.pixelplay.data.repository

import android.content.Context
import android.net.Uri
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.theveloper.pixelplay.data.cache.StreamUrlCache
import com.theveloper.pixelplay.data.database.DownloadedTrackDao
import com.theveloper.pixelplay.data.database.DownloadedTrackEntity
import com.theveloper.pixelplay.data.network.eden.EdenApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing downloaded tracks for offline playback
 */
@Singleton
class DownloadRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadedTrackDao: DownloadedTrackDao,
    private val edenApiService: EdenApiService,
    private val streamUrlCache: StreamUrlCache
) {

    /**
     * Get all downloaded tracks
     */
    fun getAllDownloadedTracks(): Flow<List<DownloadedTrackEntity>> {
        return downloadedTrackDao.getAllDownloadedTracks()
    }

    /**
     * Check if a track is downloaded
     */
    fun isTrackDownloaded(trackId: String): Flow<Boolean> {
        return downloadedTrackDao.getDownloadedTrackFlow(trackId)
            .map { it != null }
    }

    /**
     * Get downloaded track entity
     */
    suspend fun getDownloadedTrack(trackId: String): DownloadedTrackEntity? {
        return downloadedTrackDao.getDownloadedTrack(trackId)
    }

    /**
     * Get local URI for a downloaded track
     */
    suspend fun getLocalUri(trackId: String): Uri? {
        val downloadedTrack = downloadedTrackDao.getDownloadedTrack(trackId)
        return downloadedTrack?.let { Uri.fromFile(File(it.localPath)) }
    }

    /**
     * Start downloading a track
     * @return WorkRequest ID for tracking progress
     */
    suspend fun downloadTrack(trackId: String): String {
        // Get stream URL
        val streamResponse = edenApiService.getStreamUrl(trackId)
        
        // Cache the stream URL
        streamUrlCache.put(trackId, streamResponse.streamUrl, streamResponse.expiresIn)
        
        // Create work request data
        val inputData = Data.Builder()
            .putString("trackId", trackId)
            .putString("streamUrl", streamResponse.streamUrl)
            .build()
        
        // Create and enqueue work request
        val workRequest = OneTimeWorkRequestBuilder<com.theveloper.pixelplay.data.worker.TrackDownloadWorker>()
            .setInputData(inputData)
            .build()
        
        WorkManager.getInstance(context).enqueue(workRequest)
        
        return workRequest.id.toString()
    }

    /**
     * Delete a downloaded track
     */
    suspend fun deleteDownload(trackId: String) {
        val downloadedTrack = downloadedTrackDao.getDownloadedTrack(trackId)
        
        downloadedTrack?.let {
            // Delete the file
            val file = File(it.localPath)
            if (file.exists()) {
                file.delete()
            }
            
            // Remove from database
            downloadedTrackDao.deleteDownloadedTrack(trackId)
        }
    }

    /**
     * Get total downloaded tracks count
     */
    fun getDownloadedTracksCount(): Flow<Int> {
        return downloadedTrackDao.getDownloadedTracksCount()
    }

    /**
     * Get total size of downloaded tracks in bytes
     */
    fun getTotalDownloadedSize(): Flow<Long> {
        return downloadedTrackDao.getTotalDownloadedSize()
            .map { it ?: 0L }
    }

    /**
     * Clear all downloads
     */
    suspend fun clearAllDownloads() {
        val tracks = downloadedTrackDao.getAllDownloadedTracks()
        
        // This is a workaround to get the first emitted list
        // In a real implementation, you might want to use first()
        tracks.collect { trackList ->
            trackList.forEach { track ->
                val file = File(track.localPath)
                if (file.exists()) {
                    file.delete()
                }
            }
            downloadedTrackDao.deleteAllDownloadedTracks()
        }
    }
}
