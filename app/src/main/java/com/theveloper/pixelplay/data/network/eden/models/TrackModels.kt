package com.theveloper.pixelplay.data.network.eden.models

import com.google.gson.annotations.SerializedName

/**
 * Encoding information for a track
 */
data class EncodingInfo(
    @SerializedName("96kbps")
    val kbps96: String?,
    @SerializedName("160kbps")
    val kbps160: String?,
    @SerializedName("320kbps")
    val kbps320: String?,
    @SerializedName("flac")
    val flac: String?
)

/**
 * Basic track information
 */
data class TrackResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("artistId")
    val artistId: String,
    @SerializedName("albumId")
    val albumId: String?,
    @SerializedName("artworkUrl")
    val artworkUrl: String?,
    @SerializedName("title")
    val title: String,
    @SerializedName("duration")
    val duration: Double, // Duration in seconds
    @SerializedName("status")
    val status: String,
    @SerializedName("genre")
    val genre: String?,
    @SerializedName("explicit")
    val explicit: Boolean,
    @SerializedName("encodings")
    val encodings: EncodingInfo?
)

/**
 * Artist information in track relations
 */
data class ArtistInfo(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("avatarUrl")
    val avatarUrl: String?,
    @SerializedName("verified")
    val verified: Boolean
)

/**
 * Album information in track relations
 */
data class AlbumInfo(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("artworkUrl")
    val artworkUrl: String?
)

/**
 * Track with full artist and album information
 */
data class TrackWithRelationsResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("artistId")
    val artistId: String,
    @SerializedName("albumId")
    val albumId: String?,
    @SerializedName("artworkUrl")
    val artworkUrl: String?,
    @SerializedName("title")
    val title: String,
    @SerializedName("duration")
    val duration: Double,
    @SerializedName("status")
    val status: String,
    @SerializedName("genre")
    val genre: String?,
    @SerializedName("explicit")
    val explicit: Boolean,
    @SerializedName("encodings")
    val encodings: EncodingInfo?,
    @SerializedName("artist")
    val artist: ArtistInfo,
    @SerializedName("album")
    val album: AlbumInfo?
)

/**
 * Stream URL response
 */
data class StreamResponse(
    @SerializedName("streamUrl")
    val streamUrl: String,
    @SerializedName("expiresAt")
    val expiresAt: String, // ISO datetime
    @SerializedName("expiresIn")
    val expiresIn: Int, // Seconds
    @SerializedName("track")
    val track: TrackResponse
)

/**
 * Paginated response for published tracks
 */
data class PublishedTracksResponse(
    @SerializedName("tracks")
    val tracks: List<TrackWithRelationsResponse>,
    @SerializedName("pagination")
    val pagination: PaginationInfo
)

/**
 * Search tracks response
 */
data class SearchTracksResponse(
    @SerializedName("tracks")
    val tracks: List<TrackWithRelationsResponse>,
    @SerializedName("total")
    val total: Int
)

/**
 * Pagination information
 */
data class PaginationInfo(
    @SerializedName("page")
    val page: Int,
    @SerializedName("limit")
    val limit: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("totalPages")
    val totalPages: Int
)
