package com.theveloper.pixelplay.data.network.eden

import com.theveloper.pixelplay.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Interceptor that adds JWT Bearer token to all API requests
 */
class AuthInterceptor @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { 
            userPreferencesRepository.authTokenFlow.first() 
        }
        
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        
        return chain.proceed(request)
    }
}
