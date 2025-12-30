package com.theveloper.pixelplay.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.theveloper.pixelplay.data.database.DownloadedTrackDao
import com.theveloper.pixelplay.data.database.DownloadedTrackEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

/**
 * Worker for downloading tracks in the background
 */
@HiltWorker
class TrackDownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val downloadedTrackDao: DownloadedTrackDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val trackId = inputData.getString("trackId") ?: return@withContext Result.failure()
            val streamUrl = inputData.getString("streamUrl") ?: return@withContext Result.failure()

            // Create download directory if it doesn't exist
            val downloadDir = File(applicationContext.filesDir, "downloads")
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }

            // Create file for the track
            val fileName = "$trackId.mp3"
            val outputFile = File(downloadDir, fileName)

            // Download the file
            val url = URL(streamUrl)
            val connection = url.openConnection()
            connection.connect()

            val contentLength = connection.contentLength.toLong()

            connection.getInputStream().use { input ->
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytesRead = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                    }
                }
            }

            // Save to database
            val downloadedTrack = DownloadedTrackEntity(
                trackId = trackId,
                localPath = outputFile.absolutePath,
                downloadedAt = System.currentTimeMillis(),
                fileSize = outputFile.length()
            )

            downloadedTrackDao.insertDownloadedTrack(downloadedTrack)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
