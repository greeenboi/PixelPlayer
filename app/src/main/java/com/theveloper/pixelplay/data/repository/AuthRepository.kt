package com.theveloper.pixelplay.data.repository

import com.theveloper.pixelplay.data.network.eden.EdenApiService
import com.theveloper.pixelplay.data.network.eden.models.AuthResponse
import com.theveloper.pixelplay.data.network.eden.models.LoginRequest
import com.theveloper.pixelplay.data.network.eden.models.SignupRequest
import com.theveloper.pixelplay.data.network.eden.models.UserInfo
import com.theveloper.pixelplay.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling authentication operations
 */
@Singleton
class AuthRepository @Inject constructor(
    private val edenApiService: EdenApiService,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    /**
     * Flow that emits true if user is authenticated (has a token), false otherwise
     */
    val isAuthenticatedFlow: Flow<Boolean> =
        userPreferencesRepository.authTokenFlow.combine(
            userPreferencesRepository.userIdFlow
        ) { token, userId ->
            token != null && userId != null
        }

    /**
     * Flow that emits the current user info if authenticated
     */
    val userInfoFlow: Flow<UserInfo?> =
        combine(
            userPreferencesRepository.userIdFlow,
            userPreferencesRepository.userEmailFlow,
            userPreferencesRepository.userNameFlow
        ) { userId, email, name ->
            if (userId != null && email != null && name != null) {
                UserInfo(
                    id = userId,
                    email = email,
                    name = name,
                    role = "user" // Default role
                )
            } else {
                null
            }
        }

    /**
     * Login with email and password
     * @param email User's email
     * @param password User's password
     * @return Result containing AuthResponse on success, or error on failure
     */
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = edenApiService.login(LoginRequest(email, password))
            // Store auth data
            saveAuthData(response)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign up with email, password, and name
     * @param email User's email
     * @param password User's password
     * @param name User's display name
     * @return Result containing AuthResponse on success, or error on failure
     */
    suspend fun signup(email: String, password: String, name: String): Result<AuthResponse> {
        return try {
            val response = edenApiService.signup(SignupRequest(email, password, name))
            // Store auth data
            saveAuthData(response)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logout the current user
     * Clears all stored authentication data
     */
    suspend fun logout() {
        userPreferencesRepository.clearAuthData()
    }

    /**
     * Save authentication data to preferences
     */
    private suspend fun saveAuthData(authResponse: AuthResponse) {
        userPreferencesRepository.setAuthToken(authResponse.token)
        userPreferencesRepository.setUserId(authResponse.user.id)
        userPreferencesRepository.setUserEmail(authResponse.user.email)
        userPreferencesRepository.setUserName(authResponse.user.name)
    }
}
