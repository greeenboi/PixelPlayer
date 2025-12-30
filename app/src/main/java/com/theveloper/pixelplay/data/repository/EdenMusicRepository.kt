package com.theveloper.pixelplay.data.repository

import com.theveloper.pixelplay.data.cache.StreamUrlCache
import com.theveloper.pixelplay.data.model.Album
import com.theveloper.pixelplay.data.model.Artist
import com.theveloper.pixelplay.data.model.ArtistRef
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.data.network.eden.EdenApiService
import com.theveloper.pixelplay.data.network.eden.models.TrackWithRelationsResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that wraps Eden API calls and provides data mapping
 */
@Singleton
class EdenMusicRepository @Inject constructor(
    private val edenApiService: EdenApiService,
    private val streamUrlCache: StreamUrlCache
) {

    /**
     * Map Eden track response to Song model
     */
    private fun TrackWithRelationsResponse.toSong(streamUrl: String? = null): Song {
        val artistName = artist.name
        val albumTitle = album?.title ?: "Unknown Album"
        val albumArt = artworkUrl ?: album?.artworkUrl
        
        return Song(
            id = id,
            title = title,
            artist = artistName,
            artistId = id.hashCode().toLong(), // Convert UUID string to Long
            artists = listOf(
                ArtistRef(
                    id = artistId.hashCode().toLong(),
                    name = artistName,
                    isPrimary = true
                )
            ),
            album = albumTitle,
            albumId = (albumId ?: id).hashCode().toLong(),
            albumArtist = artistName,
            path = streamUrl ?: "",
            contentUriString = streamUrl ?: "",
            albumArtUriString = albumArt,
            duration = (duration * 1000).toLong(), // Convert seconds to milliseconds
            genre = genre,
            lyrics = null,
            isFavorite = false,
            trackNumber = 0,
            year = 0,
            dateAdded = System.currentTimeMillis(),
            mimeType = "audio/mpeg",
            bitrate = 320000,
            sampleRate = 44100
        )
    }

    /**
     * Get published tracks with pagination
     */
    suspend fun getPublishedTracks(page: Int = 1, limit: Int = 20): List<Song> {
        val response = edenApiService.getPublishedTracks(page, limit)
        return response.tracks.map { it.toSong() }
    }

    /**
     * Get published tracks as Flow
     */
    fun getPublishedTracksFlow(page: Int = 1, limit: Int = 20): Flow<List<Song>> = flow {
        emit(getPublishedTracks(page, limit))
    }

    /**
     * Search tracks by query
     */
    suspend fun searchTracks(query: String, limit: Int = 20): List<Song> {
        val response = edenApiService.searchTracks(query, limit)
        return response.tracks.map { it.toSong() }
    }

    /**
     * Get track by ID with stream URL
     */
    suspend fun getTrackWithStreamUrl(trackId: String): Pair<Song, String> {
        // Check cache first
        val cachedUrl = streamUrlCache.get(trackId)
        
        return if (cachedUrl != null) {
            val track = edenApiService.getTrack(trackId)
            Pair(track.toSong(cachedUrl), cachedUrl)
        } else {
            // Get fresh stream URL
            val streamResponse = edenApiService.getStreamUrl(trackId)
            streamUrlCache.put(trackId, streamResponse.streamUrl, streamResponse.expiresIn)
            Pair(streamResponse.track.toSong(streamResponse.streamUrl), streamResponse.streamUrl)
        }
    }

    /**
     * Get stream URL for a track
     */
    suspend fun getStreamUrl(trackId: String): String {
        // Check cache first
        streamUrlCache.get(trackId)?.let { return it }
        
        // Get fresh URL from API
        val response = edenApiService.getStreamUrl(trackId)
        streamUrlCache.put(trackId, response.streamUrl, response.expiresIn)
        return response.streamUrl
    }

    /**
     * Get albums with pagination
     */
    suspend fun getAlbums(page: Int = 1, limit: Int = 20): List<Album> {
        val response = edenApiService.getAlbums(page, limit)
        return response.albums.map { albumSummary ->
            Album(
                id = albumSummary.id.hashCode().toLong(),
                title = albumSummary.title,
                artist = "Unknown Artist", // Not provided in summary
                artworkUri = albumSummary.artworkUrl,
                year = albumSummary.releaseDate?.substring(0, 4)?.toIntOrNull() ?: 0,
                songCount = albumSummary.trackCount ?: 0
            )
        }
    }

    /**
     * Get album detail by ID
     */
    suspend fun getAlbumDetail(albumId: String): Pair<Album, List<Song>> {
        val response = edenApiService.getAlbum(albumId)
        
        val album = Album(
            id = response.id.hashCode().toLong(),
            title = response.title,
            artist = response.artist.name,
            artworkUri = response.artworkUrl,
            year = response.releaseDate?.substring(0, 4)?.toIntOrNull() ?: 0,
            songCount = response.tracks.size
        )
        
        val songs = response.tracks.map { track ->
            Song(
                id = track.id,
                title = track.title,
                artist = response.artist.name,
                artistId = response.artistId.hashCode().toLong(),
                artists = listOf(
                    ArtistRef(
                        id = response.artistId.hashCode().toLong(),
                        name = response.artist.name,
                        isPrimary = true
                    )
                ),
                album = response.title,
                albumId = response.id.hashCode().toLong(),
                albumArtist = response.artist.name,
                path = "",
                contentUriString = "",
                albumArtUriString = response.artworkUrl,
                duration = (track.duration * 1000).toLong(),
                genre = track.genre,
                lyrics = null,
                isFavorite = false,
                trackNumber = 0,
                year = response.releaseDate?.substring(0, 4)?.toIntOrNull() ?: 0,
                dateAdded = System.currentTimeMillis(),
                mimeType = "audio/mpeg",
                bitrate = 320000,
                sampleRate = 44100
            )
        }
        
        return Pair(album, songs)
    }

    /**
     * Get artists with pagination
     */
    suspend fun getArtists(page: Int = 1, limit: Int = 20): List<Artist> {
        val response = edenApiService.getArtists(page, limit)
        return response.artists.map { artistResponse ->
            Artist(
                id = artistResponse.id.hashCode().toLong(),
                name = artistResponse.name,
                imageUrl = artistResponse.avatarUrl,
                songCount = 0 // Not provided by API
            )
        }
    }

    /**
     * Get artist tracks
     */
    suspend fun getArtistTracks(artistId: String, page: Int = 1, limit: Int = 20): List<Song> {
        val response = edenApiService.getArtistTracks(artistId, page, limit)
        return response.tracks.map { it.toSong() }
    }

    /**
     * Search artists by query
     */
    suspend fun searchArtists(query: String, limit: Int = 20): List<Artist> {
        val response = edenApiService.searchArtists(query, limit)
        return response.artists.map { artistResponse ->
            Artist(
                id = artistResponse.id.hashCode().toLong(),
                name = artistResponse.name,
                imageUrl = artistResponse.avatarUrl,
                songCount = 0
            )
        }
    }
}
