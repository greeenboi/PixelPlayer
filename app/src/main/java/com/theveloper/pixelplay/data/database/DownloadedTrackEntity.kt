package com.theveloper.pixelplay.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a downloaded track for offline playback
 */
@Entity(tableName = "downloaded_tracks")
data class DownloadedTrackEntity(
    @PrimaryKey
    val trackId: String,
    val localPath: String,
    val downloadedAt: Long,
    val fileSize: Long
)
