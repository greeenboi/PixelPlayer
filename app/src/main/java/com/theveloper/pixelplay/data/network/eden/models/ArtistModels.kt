package com.theveloper.pixelplay.data.network.eden.models

import com.google.gson.annotations.SerializedName

/**
 * Artist profile information
 */
data class ArtistResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("bio")
    val bio: String?,
    @SerializedName("avatarUrl")
    val avatarUrl: String?,
    @SerializedName("verified")
    val verified: Boolean,
    @SerializedName("monthlyListeners")
    val monthlyListeners: Int?
)

/**
 * List artists response
 */
data class ListArtistsResponse(
    @SerializedName("artists")
    val artists: List<ArtistResponse>,
    @SerializedName("pagination")
    val pagination: PaginationInfo
)

/**
 * Artist tracks response
 */
data class ArtistTracksResponse(
    @SerializedName("tracks")
    val tracks: List<TrackWithRelationsResponse>,
    @SerializedName("pagination")
    val pagination: PaginationInfo
)

/**
 * Search artists response
 */
data class SearchArtistsResponse(
    @SerializedName("artists")
    val artists: List<ArtistResponse>,
    @SerializedName("total")
    val total: Int
)
