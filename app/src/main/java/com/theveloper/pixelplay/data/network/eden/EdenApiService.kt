package com.theveloper.pixelplay.data.network.eden

import com.theveloper.pixelplay.data.network.eden.models.*
import retrofit2.http.*

/**
 * Retrofit service interface for Eden Server API
 * Base URL: https://eden-server.suvan-gowrishanker-204.workers.dev
 */
interface EdenApiService {
    
    // ==================== Authentication ====================
    
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
    
    @POST("api/auth/signup")
    suspend fun signup(@Body request: SignupRequest): AuthResponse
    
    // ==================== Tracks ====================
    
    @GET("api/tracks/published")
    suspend fun getPublishedTracks(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("artistId") artistId: String? = null,
        @Query("albumId") albumId: String? = null
    ): PublishedTracksResponse
    
    @GET("api/tracks/search")
    suspend fun searchTracks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): SearchTracksResponse
    
    @GET("api/tracks/{id}")
    suspend fun getTrack(@Path("id") id: String): TrackWithRelationsResponse
    
    @GET("api/tracks/{id}/stream")
    suspend fun getStreamUrl(@Path("id") id: String): StreamResponse
    
    // ==================== Albums ====================
    
    @GET("api/albums")
    suspend fun getAlbums(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("artistId") artistId: String? = null
    ): ListAlbumsResponse
    
    @GET("api/albums/{id}")
    suspend fun getAlbum(@Path("id") id: String): AlbumDetailResponse
    
    // ==================== Artists ====================
    
    @GET("api/artists")
    suspend fun getArtists(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ListArtistsResponse
    
    @GET("api/artists/{id}")
    suspend fun getArtist(@Path("id") id: String): ArtistResponse
    
    @GET("api/artists/{id}/tracks")
    suspend fun getArtistTracks(
        @Path("id") id: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("status") status: String = "published"
    ): ArtistTracksResponse
    
    @GET("api/artists/search")
    suspend fun searchArtists(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): SearchArtistsResponse
}
