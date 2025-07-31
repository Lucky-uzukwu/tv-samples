package com.google.wiltv.data.network

import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Request
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

class AuthenticationEdgeCasesTest {

    @Mock
    private lateinit var userTokenProvider: UserTokenProvider
    
    private lateinit var authInterceptor: AuthInterceptor
    private lateinit var mockWebServer: MockWebServer
    private lateinit var client: OkHttpClient

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        authInterceptor = AuthInterceptor(userTokenProvider)
        mockWebServer = MockWebServer()
        mockWebServer.start()
        
        client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `handles null token gracefully`() = runTest {
        // Given
        whenever(userTokenProvider.getToken()).thenReturn(null)

        mockWebServer.enqueue(MockResponse().setBody("success"))

        // When
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()
        
        val response = client.newCall(request).execute()

        // Then
        val recordedRequest = mockWebServer.takeRequest()
        assertNull(recordedRequest.getHeader("Authorization"))
        assertEquals(200, response.code)
    }

    @Test
    fun `handles empty string token gracefully`() = runTest {
        // Given
        whenever(userTokenProvider.getToken()).thenReturn("")

        mockWebServer.enqueue(MockResponse().setBody("success"))

        // When
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()
        
        val response = client.newCall(request).execute()

        // Then
        val recordedRequest = mockWebServer.takeRequest()
        assertNull(recordedRequest.getHeader("Authorization"))
        assertEquals(200, response.code)
    }

    @Test
    fun `handles whitespace-only token gracefully`() = runTest {
        // Given
        whenever(userTokenProvider.getToken()).thenReturn("   \t\n  ")

        mockWebServer.enqueue(MockResponse().setBody("success"))

        // When
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()
        
        val response = client.newCall(request).execute()

        // Then
        val recordedRequest = mockWebServer.takeRequest()
        assertNull(recordedRequest.getHeader("Authorization"))
        assertEquals(200, response.code)
    }

    @Test
    fun `handles user not logged in gracefully`() = runTest {
        // Given
        whenever(userTokenProvider.getToken()).thenReturn(null)

        mockWebServer.enqueue(MockResponse().setBody("success"))

        // When
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()
        
        val response = client.newCall(request).execute()

        // Then
        val recordedRequest = mockWebServer.takeRequest()
        assertNull(recordedRequest.getHeader("Authorization"))
        assertEquals(200, response.code)
    }

    @Test
    fun `handles very long token correctly`() = runTest {
        // Given
        val longToken = "a".repeat(1000) // 1000 character token
        whenever(userTokenProvider.getToken()).thenReturn(longToken)

        mockWebServer.enqueue(MockResponse().setBody("success"))

        // When
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()
        
        val response = client.newCall(request).execute()

        // Then
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("Bearer $longToken", recordedRequest.getHeader("Authorization"))
        assertEquals(200, response.code)
    }

    @Test
    fun `handles token with special characters correctly`() = runTest {
        // Given
        val specialToken = "token-with-special@chars#123!&*()+=[]{}|\\:;'\"<>,.?/"
        whenever(userTokenProvider.getToken()).thenReturn(specialToken)

        mockWebServer.enqueue(MockResponse().setBody("success"))

        // When
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()
        
        val response = client.newCall(request).execute()

        // Then
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("Bearer $specialToken", recordedRequest.getHeader("Authorization"))
        assertEquals(200, response.code)
    }

    @Test
    fun `handles token with unicode characters correctly`() = runTest {
        // Given
        val unicodeToken = "token-with-unicode-🔑-auth-token-测试"
        whenever(userTokenProvider.getToken()).thenReturn(unicodeToken)

        mockWebServer.enqueue(MockResponse().setBody("success"))

        // When
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()
        
        val response = client.newCall(request).execute()

        // Then
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("Bearer $unicodeToken", recordedRequest.getHeader("Authorization"))
        assertEquals(200, response.code)
    }

    @Test
    fun `preserves other authorization headers when token is blank`() = runTest {
        // Given
        whenever(userTokenProvider.getToken()).thenReturn("")

        mockWebServer.enqueue(MockResponse().setBody("success"))

        // When - try to add existing Authorization header
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .addHeader("Authorization", "Basic xyz123")
            .build()
        
        val response = client.newCall(request).execute()

        // Then - original authorization should be preserved when token is blank
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("Basic xyz123", recordedRequest.getHeader("Authorization"))
        assertEquals(200, response.code)
    }

    @Test
    fun `overrides existing authorization header when token is present`() = runTest {
        // Given
        val token = "valid-bearer-token"
        whenever(userTokenProvider.getToken()).thenReturn(token)

        mockWebServer.enqueue(MockResponse().setBody("success"))

        // When - try to add existing Authorization header
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .addHeader("Authorization", "Basic xyz123")
            .build()
        
        val response = client.newCall(request).execute()

        // Then - Bearer token should have priority over existing authorization
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("Bearer $token", recordedRequest.getHeader("Authorization"))
        assertEquals(200, response.code)
    }
}