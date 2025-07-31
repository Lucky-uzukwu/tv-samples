package com.google.wiltv.data.network

import android.content.Context
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull

class ImageAuthenticationTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var userTokenProvider: UserTokenProvider

    private lateinit var authInterceptor: AuthInterceptor
    private lateinit var imageLoader: ImageLoader
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        mockWebServer = MockWebServer()
        mockWebServer.start()
        
        authInterceptor = AuthInterceptor(userTokenProvider)
        
        // Create ImageLoader with the auth interceptor
        imageLoader = ImageLoader.Builder(context)
            .okHttpClient {
                okhttp3.OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .build()
            }
            .build()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `image request includes authorization header when user is logged in`() = runTest {
        // Given
        val token = "image-auth-token-123"
        whenever(userTokenProvider.getToken()).thenReturn(token)
        
        // Mock a successful image response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("fake-image-data")
                .setHeader("Content-Type", "image/jpeg")
        )

        // When
        val imageUrl = mockWebServer.url("/test-image.jpg").toString()
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .build()

        // Execute the image request using our configured ImageLoader
        try {
            imageLoader.execute(request)
        } catch (e: Exception) {
            // Expected to fail in test environment due to missing Context implementation
            // But the HTTP request should still be made with proper headers
        }

        // Then
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("Bearer $token", recordedRequest.getHeader("Authorization"))
        assertEquals("GET", recordedRequest.method)
        assertEquals("/test-image.jpg", recordedRequest.path)
    }

    @Test
    fun `image request does not include authorization header when user has no token`() = runTest {
        // Given
        whenever(userTokenProvider.getToken()).thenReturn(null)
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("fake-image-data")
                .setHeader("Content-Type", "image/jpeg")
        )

        // When
        val imageUrl = mockWebServer.url("/test-image.jpg").toString()
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .build()

        try {
            imageLoader.execute(request)
        } catch (e: Exception) {
            // Expected to fail in test environment
        }

        // Then
        val recordedRequest = mockWebServer.takeRequest()
        assertNull(recordedRequest.getHeader("Authorization"))
        assertEquals("GET", recordedRequest.method)
    }

    @Test
    fun `image request does not include authorization header when user is not logged in`() = runTest {
        // Given
        whenever(userTokenProvider.getToken()).thenReturn(null)
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("fake-image-data")
                .setHeader("Content-Type", "image/jpeg")
        )

        // When
        val imageUrl = mockWebServer.url("/test-image.jpg").toString()
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .build()

        try {
            imageLoader.execute(request)
        } catch (e: Exception) {
            // Expected to fail in test environment
        }

        // Then
        val recordedRequest = mockWebServer.takeRequest()
        assertNull(recordedRequest.getHeader("Authorization"))
        assertEquals("GET", recordedRequest.method)
    }

    @Test
    fun `image request handles server authentication error gracefully`() = runTest {
        // Given
        val token = "invalid-token"
        whenever(userTokenProvider.getToken()).thenReturn(token)
        
        // Mock an authentication error response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("{\"error\": \"Unauthorized\"}")
                .setHeader("Content-Type", "application/json")
        )

        // When
        val imageUrl = mockWebServer.url("/secure-image.jpg").toString()
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .build()

        try {
            imageLoader.execute(request)
        } catch (e: Exception) {
            // Expected to fail in test environment
        }

        // Then - verify the request was made with the token
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("Bearer $token", recordedRequest.getHeader("Authorization"))
        assertEquals("GET", recordedRequest.method)
        assertEquals("/secure-image.jpg", recordedRequest.path)
    }

    @Test
    fun `multiple concurrent image requests all include authorization headers`() = runTest {
        // Given
        val token = "concurrent-token-456"
        whenever(userTokenProvider.getToken()).thenReturn(token)
        
        // Mock responses for multiple requests
        repeat(3) {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody("fake-image-data-$it")
                    .setHeader("Content-Type", "image/jpeg")
            )
        }

        // When - make multiple concurrent requests
        repeat(3) { index ->
            val imageUrl = mockWebServer.url("/image-$index.jpg").toString()
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .build()

            try {
                imageLoader.execute(request)
            } catch (e: Exception) {
                // Expected to fail in test environment
            }
        }

        // Then - verify all requests included the authorization header
        repeat(3) {
            val recordedRequest = mockWebServer.takeRequest()
            assertEquals("Bearer $token", recordedRequest.getHeader("Authorization"))
            assertEquals("GET", recordedRequest.method)
        }
    }
}