//package com.google.wiltv.data.network
//
//import kotlinx.coroutines.test.runTest
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.mockwebserver.MockResponse
//import okhttp3.mockwebserver.MockWebServer
//import org.junit.After
//import org.junit.Before
//import org.junit.Test
//import org.mockito.Mock
//import org.mockito.MockitoAnnotations
//import org.mockito.kotlin.whenever
//import org.junit.Assert.assertEquals
//import org.junit.Assert.assertNull
//
//class AuthInterceptorTest {
//
//    @Mock
//    private lateinit var userTokenProvider: UserTokenProvider
//
//    private lateinit var authInterceptor: AuthInterceptor
//    private lateinit var mockWebServer: MockWebServer
//    private lateinit var client: OkHttpClient
//
//    @Before
//    fun setup() {
//        MockitoAnnotations.openMocks(this)
//        authInterceptor = AuthInterceptor(userTokenProvider)
//        mockWebServer = MockWebServer()
//        mockWebServer.start()
//
//        client = OkHttpClient.Builder()
//            .addInterceptor(authInterceptor)
//            .build()
//    }
//
//    @After
//    fun tearDown() {
//        mockWebServer.shutdown()
//    }
//
//    @Test
//    fun `intercept adds authorization header when user has token`() = runTest {
//        // Given
//        val token = "test-token-123"
//        whenever(userTokenProvider.getToken()).thenReturn(token)
//
//        mockWebServer.enqueue(MockResponse().setBody("success"))
//
//        // When
//        val request = Request.Builder()
//            .url(mockWebServer.url("/test"))
//            .build()
//
//        val response = client.newCall(request).execute()
//
//        // Then
//        val recordedRequest = mockWebServer.takeRequest()
//        assertEquals("Bearer $token", recordedRequest.getHeader("Authorization"))
//        assertEquals(200, response.code)
//    }
//
//    @Test
//    fun `intercept does not add authorization header when user has null token`() = runTest {
//        // Given
//        whenever(userTokenProvider.getToken()).thenReturn(null)
//
//        mockWebServer.enqueue(MockResponse().setBody("success"))
//
//        // When
//        val request = Request.Builder()
//            .url(mockWebServer.url("/test"))
//            .build()
//
//        val response = client.newCall(request).execute()
//
//        // Then
//        val recordedRequest = mockWebServer.takeRequest()
//        assertNull(recordedRequest.getHeader("Authorization"))
//        assertEquals(200, response.code)
//    }
//
//    @Test
//    fun `intercept does not add authorization header when user has blank token`() = runTest {
//        // Given
//        whenever(userTokenProvider.getToken()).thenReturn("")
//
//        mockWebServer.enqueue(MockResponse().setBody("success"))
//
//        // When
//        val request = Request.Builder()
//            .url(mockWebServer.url("/test"))
//            .build()
//
//        val response = client.newCall(request).execute()
//
//        // Then
//        val recordedRequest = mockWebServer.takeRequest()
//        assertNull(recordedRequest.getHeader("Authorization"))
//        assertEquals(200, response.code)
//    }
//
//    @Test
//    fun `intercept does not add authorization header when user is not logged in`() = runTest {
//        // Given
//        whenever(userTokenProvider.getToken()).thenReturn(null)
//
//        mockWebServer.enqueue(MockResponse().setBody("success"))
//
//        // When
//        val request = Request.Builder()
//            .url(mockWebServer.url("/test"))
//            .build()
//
//        val response = client.newCall(request).execute()
//
//        // Then
//        val recordedRequest = mockWebServer.takeRequest()
//        assertNull(recordedRequest.getHeader("Authorization"))
//        assertEquals(200, response.code)
//    }
//
//    @Test
//    fun `intercept preserves existing headers`() = runTest {
//        // Given
//        val token = "test-token-456"
//        whenever(userTokenProvider.getToken()).thenReturn(token)
//
//        mockWebServer.enqueue(MockResponse().setBody("success"))
//
//        // When
//        val request = Request.Builder()
//            .url(mockWebServer.url("/test"))
//            .addHeader("Content-Type", "application/json")
//            .addHeader("X-Custom-Header", "custom-value")
//            .build()
//
//        val response = client.newCall(request).execute()
//
//        // Then
//        val recordedRequest = mockWebServer.takeRequest()
//        assertEquals("Bearer $token", recordedRequest.getHeader("Authorization"))
//        assertEquals("application/json", recordedRequest.getHeader("Content-Type"))
//        assertEquals("custom-value", recordedRequest.getHeader("X-Custom-Header"))
//        assertEquals(200, response.code)
//    }
//
//    @Test
//    fun `intercept handles whitespace-only token as blank`() = runTest {
//        // Given
//        whenever(userTokenProvider.getToken()).thenReturn("   ")
//
//        mockWebServer.enqueue(MockResponse().setBody("success"))
//
//        // When
//        val request = Request.Builder()
//            .url(mockWebServer.url("/test"))
//            .build()
//
//        val response = client.newCall(request).execute()
//
//        // Then
//        val recordedRequest = mockWebServer.takeRequest()
//        assertNull(recordedRequest.getHeader("Authorization"))
//        assertEquals(200, response.code)
//    }
//}