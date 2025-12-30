package com.theveloper.pixelplay.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for managing downloaded tracks
 */
@Dao
interface DownloadedTrackDao {
    
    @Query("SELECT * FROM downloaded_tracks")
    fun getAllDownloadedTracks(): Flow<List<DownloadedTrackEntity>>
    
    @Query("SELECT * FROM downloaded_tracks WHERE trackId = :trackId")
    suspend fun getDownloadedTrack(trackId: String): DownloadedTrackEntity?
    
    @Query("SELECT * FROM downloaded_tracks WHERE trackId = :trackId")
    fun getDownloadedTrackFlow(trackId: String): Flow<DownloadedTrackEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadedTrack(track: DownloadedTrackEntity)
    
    @Query("DELETE FROM downloaded_tracks WHERE trackId = :trackId")
    suspend fun deleteDownloadedTrack(trackId: String)
    
    @Query("DELETE FROM downloaded_tracks")
    suspend fun deleteAllDownloadedTracks()
    
    @Query("SELECT COUNT(*) FROM downloaded_tracks")
    fun getDownloadedTracksCount(): Flow<Int>
    
    @Query("SELECT SUM(fileSize) FROM downloaded_tracks")
    fun getTotalDownloadedSize(): Flow<Long?>
}
