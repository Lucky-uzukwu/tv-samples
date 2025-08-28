// ABOUTME: Search error suggestion components with different error type handling
// ABOUTME: Provides helpful alternatives and suggestions when search fails or returns no results

package com.google.wiltv.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

@Composable
fun SearchErrorSuggestions(
    modifier: Modifier = Modifier,
    errorType: SearchErrorType,
    query: String,
    suggestion: String? = null,
    onBrowseCategoriesClick: () -> Unit,
    onTrendingContentClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    when (errorType) {
        is SearchErrorType.NetworkError -> NetworkErrorFallback(
            query = query,
            onBrowseCategoriesClick = onBrowseCategoriesClick,
            onTrendingContentClick = onTrendingContentClick,
            onRetryClick = onRetryClick,
            modifier = modifier
        )

        is SearchErrorType.NoResults -> NoResultsSuggestions(
            query = query,
            onBrowseCategoriesClick = onBrowseCategoriesClick,
            onTrendingContentClick = onTrendingContentClick,
            onRetryClick = onRetryClick,
            modifier = modifier
        )

        is SearchErrorType.QueryError -> QueryErrorSuggestions(
            query = query,
            suggestion = suggestion ?: "Try different keywords or check your spelling",
            onRetryClick = onRetryClick,
            modifier = modifier
        )
    }
}

@Composable
fun NetworkErrorFallback(
    query: String,
    onBrowseCategoriesClick: () -> Unit,
    onTrendingContentClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Search is temporarily unavailable",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "We're having trouble connecting to our servers. Here are some alternatives:",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CustomFillButton(
                text = "Browse Categories",
                onClick = onBrowseCategoriesClick,
                modifier = Modifier.width(180.dp),
                buttonColor = ButtonDefaults.colors(
                    containerColor = Color.White.copy(alpha = 0.1f),
                    focusedContainerColor = Color(0xFFA855F7),
                    contentColor = Color.White,
                    focusedContentColor = Color.White
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Center
                )
            )

            CustomFillButton(
                text = "Trending Content",
                onClick = onTrendingContentClick,
                modifier = Modifier.width(180.dp),
                buttonColor = ButtonDefaults.colors(
                    containerColor = Color.White.copy(alpha = 0.1f),
                    focusedContainerColor = Color(0xFFA855F7),
                    contentColor = Color.White,
                    focusedContentColor = Color.White
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Center
                )
            )

            CustomFillButton(
                text = "Try Again",
                onClick = onRetryClick,
                modifier = Modifier.width(120.dp),
                buttonColor = ButtonDefaults.colors(
                    containerColor = Color.White.copy(alpha = 0.1f),
                    focusedContainerColor = Color(0xFFA855F7),
                    contentColor = Color.White,
                    focusedContentColor = Color.White
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

@Composable
fun NoResultsSuggestions(
    query: String,
    onBrowseCategoriesClick: () -> Unit,
    onTrendingContentClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No content found for \"$query\"",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Don't worry! Here are some ways to discover great content:",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CustomFillButton(
                    text = "Browse Categories",
                    onClick = onBrowseCategoriesClick,
                    modifier = Modifier.width(180.dp),
                    buttonColor = ButtonDefaults.colors(
                        containerColor = Color.White.copy(alpha = 0.1f),
                        focusedContainerColor = Color(0xFFA855F7),
                        contentColor = Color.White,
                        focusedContentColor = Color.White
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Center
                    )
                )

                CustomFillButton(
                    text = "Popular Content",
                    onClick = onTrendingContentClick,
                    modifier = Modifier.width(180.dp),
                    buttonColor = ButtonDefaults.colors(
                        containerColor = Color.White.copy(alpha = 0.1f),
                        focusedContainerColor = Color(0xFFA855F7),
                        contentColor = Color.White,
                        focusedContentColor = Color.White
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Center
                    )
                )
            }

            Text(
                text = "or try a different search term",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun QueryErrorSuggestions(
    query: String,
    suggestion: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Let's improve that search",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Search term: \"$query\"",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = suggestion,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(24.dp))

        CustomFillButton(
            text = "Try Again",
            onClick = onRetryClick,
            modifier = Modifier.width(140.dp),
            buttonColor = ButtonDefaults.colors(
                containerColor = Color.White.copy(alpha = 0.1f),
                focusedContainerColor = Color(0xFFA855F7),
                contentColor = Color.White,
                focusedContentColor = Color.White
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tips: Use specific keywords, check spelling, try different terms",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

sealed interface SearchErrorType {
    data object NetworkError : SearchErrorType
    data object NoResults : SearchErrorType
    data object QueryError : SearchErrorType
}