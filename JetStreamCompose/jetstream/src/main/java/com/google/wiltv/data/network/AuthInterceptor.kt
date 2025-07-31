// ABOUTME: HTTP interceptor that adds authentication headers to Coil image requests
// ABOUTME: Uses UserStateHolder to get current user token and adds Bearer authorization header

package com.google.wiltv.data.network

import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val userTokenProvider: UserTokenProvider
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        Log.d("AuthInterceptor", "Intercepting request to: ${originalRequest.url}")
        
        // Get current user token
        val token = runBlocking {
            userTokenProvider.getToken()
        }
        
        if (token.isNullOrBlank()) {
            Log.w("AuthInterceptor", "No token available for ${originalRequest.url}, proceeding without authentication")
            return chain.proceed(originalRequest)
        } else {
            Log.d("AuthInterceptor", "Token available (length: ${token.length}) for ${originalRequest.url}")
        }
        
        // Add Authorization header with Bearer token
        val authenticatedRequest = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
            
        Log.d("AuthInterceptor", "Added Authorization header to request")
        val response = chain.proceed(authenticatedRequest)
        Log.d("AuthInterceptor", "Response code: ${response.code} for ${originalRequest.url}")
        return response
    }
}