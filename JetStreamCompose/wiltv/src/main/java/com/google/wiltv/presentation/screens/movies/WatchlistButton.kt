// ABOUTME: WatchlistButton component for toggling content in user watchlist
// ABOUTME: Matches PlayButton styling with proper TV focus handling and visual feedback
package com.google.wiltv.presentation.screens.movies

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.wiltv.R
import com.google.wiltv.presentation.theme.WilTvButtonShape
import md_theme_light_onTertiary
import md_theme_light_outline
import md_theme_light_shadow

@Composable
fun WatchlistButton(
    modifier: Modifier = Modifier,
    isInWatchlist: Boolean,
    isLoading: Boolean = false,
    onClick: () -> Unit,
    focusRequester: FocusRequester,
) {
    val buttonText = when {
        isLoading -> stringResource(R.string.message_loading)
        isInWatchlist -> stringResource(R.string.in_watchlist)
        else -> stringResource(R.string.watchlist)
    }
    
    val icon = if (isInWatchlist) {
        Icons.Outlined.Bookmark
    } else {
        Icons.Outlined.BookmarkBorder
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .height(40.dp)
            .focusRequester(focusRequester)
            .then(modifier),
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        shape = ButtonDefaults.shape(shape = WilTvButtonShape),
        colors = ButtonDefaults.colors(
            containerColor = if (isInWatchlist) Color(0xFF059669) else md_theme_light_outline, // Green when in watchlist
            contentColor = md_theme_light_onTertiary,
            focusedContainerColor = Color(0xFFA855F7),
            focusedContentColor = md_theme_light_shadow,
        ),
        scale = ButtonDefaults.scale(scale = 1f),
        enabled = !isLoading
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = buttonText,
            style = MaterialTheme.typography.titleSmall
        )
    }
}