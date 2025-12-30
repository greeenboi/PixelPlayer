package com.theveloper.pixelplay.data.network.eden.models

import com.google.gson.annotations.SerializedName

/**
 * Album detail information
 */
data class AlbumDetailResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("artistId")
    val artistId: String,
    @SerializedName("artworkUrl")
    val artworkUrl: String?,
    @SerializedName("releaseDate")
    val releaseDate: String?,
    @SerializedName("genre")
    val genre: String?,
    @SerializedName("artist")
    val artist: ArtistInfo,
    @SerializedName("tracks")
    val tracks: List<TrackResponse>
)

/**
 * List albums response
 */
data class ListAlbumsResponse(
    @SerializedName("albums")
    val albums: List<AlbumSummary>,
    @SerializedName("pagination")
    val pagination: PaginationInfo
)

/**
 * Album summary for list view
 */
data class AlbumSummary(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("artistId")
    val artistId: String,
    @SerializedName("artworkUrl")
    val artworkUrl: String?,
    @SerializedName("releaseDate")
    val releaseDate: String?,
    @SerializedName("genre")
    val genre: String?,
    @SerializedName("trackCount")
    val trackCount: Int?
)
