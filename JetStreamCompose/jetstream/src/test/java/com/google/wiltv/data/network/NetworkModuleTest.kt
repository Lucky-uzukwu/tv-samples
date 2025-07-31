package com.google.wiltv.data.network

import android.content.Context
import coil.ImageLoader
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

class NetworkModuleTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var authInterceptor: AuthInterceptor

    private lateinit var networkModule: NetworkModule

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        networkModule = NetworkModule()
    }

    @Test
    fun `provideAuthenticatedImageLoader creates ImageLoader with auth interceptor`() {
        // When
        val imageLoader = networkModule.provideAuthenticatedImageLoader(context, authInterceptor)

        // Then
        assertNotNull(imageLoader)
        assertTrue(imageLoader is ImageLoader)
        
        // Verify that the ImageLoader was built with custom OkHttpClient
        // Note: Since Coil's ImageLoader doesn't expose its OkHttpClient directly,
        // we can only verify that the ImageLoader was created successfully
        // The actual interceptor integration is tested in AuthInterceptorTest
    }

    @Test
    fun `provideAuthInterceptor creates AuthInterceptor with UserTokenProvider`() {
        // Given
        val userTokenProvider = org.mockito.kotlin.mock<UserTokenProvider>()

        // When
        val interceptor = networkModule.provideAuthInterceptor(userTokenProvider)

        // Then
        assertNotNull(interceptor)
        assertTrue(interceptor is AuthInterceptor)
    }

    @Test
    fun `multiple calls to provideAuthenticatedImageLoader create different instances`() {
        // When
        val imageLoader1 = networkModule.provideAuthenticatedImageLoader(context, authInterceptor)
        val imageLoader2 = networkModule.provideAuthenticatedImageLoader(context, authInterceptor)

        // Then
        assertNotNull(imageLoader1)
        assertNotNull(imageLoader2)
        // Since it's not a singleton without @Singleton annotation in test,
        // each call should create a new instance
        assertTrue(imageLoader1 !== imageLoader2)
    }
}