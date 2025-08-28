// ABOUTME: Unit tests for image placeholder components with retry functionality
// ABOUTME: Validates error state handling and user interaction patterns

package com.google.wiltv.presentation.common

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Tests for image placeholder components ensuring proper error handling
 * and retry functionality follows TV navigation patterns
 */
@RunWith(AndroidJUnit4::class)
class ImagePlaceholdersTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun moviePosterPlaceholder_displaysTitle() {
        val testTitle = "Test Movie Title"

        composeTestRule.setContent {
            MoviePosterPlaceholder(
                title = testTitle,
                onRetry = { }
            )
        }

        composeTestRule
            .onNodeWithText(testTitle)
            .assertExists()
    }

    @Test
    fun moviePosterPlaceholder_retryButtonWorks() {
        var retryClicked = false
        val testTitle = "Test Movie"

        composeTestRule.setContent {
            MoviePosterPlaceholder(
                title = testTitle,
                onRetry = { retryClicked = true }
            )
        }

        composeTestRule
            .onNodeWithText("Retry")
            .performClick()

        assertTrue("Retry callback should be called", retryClicked)
    }

    @Test
    fun backdropPlaceholder_displaysContentInfo() {
        val testTitle = "Test Movie Title"
        val testSubtitle = "2024 • Action"

        composeTestRule.setContent {
            BackdropPlaceholder(
                title = testTitle,
                subtitle = testSubtitle,
                onRetry = { }
            )
        }

        composeTestRule
            .onNodeWithText(testTitle)
            .assertExists()

        composeTestRule
            .onNodeWithText(testSubtitle)
            .assertExists()
    }

    @Test
    fun backdropPlaceholder_retryButtonWorks() {
        var retryClicked = false

        composeTestRule.setContent {
            BackdropPlaceholder(
                title = "Test Movie",
                onRetry = { retryClicked = true }
            )
        }

        composeTestRule
            .onNodeWithText("Retry Image")
            .performClick()

        assertTrue("Retry callback should be called", retryClicked)
    }

    @Test
    fun genericImagePlaceholder_retryButtonWorks() {
        var retryClicked = false

        composeTestRule.setContent {
            GenericImagePlaceholder(
                onRetry = { retryClicked = true }
            )
        }

        composeTestRule
            .onNodeWithText("Retry")
            .performClick()

        assertTrue("Retry callback should be called", retryClicked)
    }

    @Test
    fun backdropPlaceholder_handlesNullSubtitle() {
        val testTitle = "Test Movie Title"

        composeTestRule.setContent {
            BackdropPlaceholder(
                title = testTitle,
                subtitle = null,
                onRetry = { }
            )
        }

        composeTestRule
            .onNodeWithText(testTitle)
            .assertExists()

        // Should not crash with null subtitle
    }
}