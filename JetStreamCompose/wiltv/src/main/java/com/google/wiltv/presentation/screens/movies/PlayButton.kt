package com.google.wiltv.presentation.screens.movies

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.focusRequester
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
fun PlayButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    focusRequester: FocusRequester,
) {

    Button(
        onClick = onClick,
        modifier = Modifier
            .height(40.dp)
            .focusRequester(focusRequester)
            .then(modifier),
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        shape = ButtonDefaults.shape(shape = WilTvButtonShape),
        colors = ButtonDefaults.colors(
            containerColor = md_theme_light_outline,
            contentColor = md_theme_light_onTertiary,
            focusedContainerColor = Color(0xFFA855F7),
            focusedContentColor = md_theme_light_shadow,
        ),
        scale = ButtonDefaults.scale(scale = 1f)
    ) {
        Icon(
            imageVector = Icons.Outlined.PlayArrow,
            contentDescription = null,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = stringResource(R.string.play),
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@Composable
fun ComingSoonButton(
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester,
) {
    Button(
        onClick = { }, // No action when pressed
        modifier = Modifier
            .height(40.dp)
            .focusRequester(focusRequester)
            .then(modifier),
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        shape = ButtonDefaults.shape(shape = WilTvButtonShape),
        colors = ButtonDefaults.colors(
            containerColor = Color(0xFF6B7280), // Gray background for disabled state
            contentColor = Color.White.copy(alpha = 0.8f),
            focusedContainerColor = Color(0xFF9CA3AF), // Lighter gray when focused
            focusedContentColor = Color.White,
        ),
        scale = ButtonDefaults.scale(scale = 1f)
    ) {
        Icon(
            imageVector = Icons.Outlined.Schedule,
            contentDescription = null,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = stringResource(R.string.coming_soon),
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@Composable
fun ResumePlayButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    focusRequester: FocusRequester,
    hasProgress: Boolean = false,
    progressPercentage: Float = 0f
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(40.dp)
            .focusRequester(focusRequester)
            .then(modifier),
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        shape = ButtonDefaults.shape(shape = WilTvButtonShape),
        colors = ButtonDefaults.colors(
            containerColor = md_theme_light_outline,
            contentColor = md_theme_light_onTertiary,
            focusedContainerColor = Color(0xFFA855F7),
            focusedContentColor = md_theme_light_shadow,
        ),
        scale = ButtonDefaults.scale(scale = 1f)
    ) {
        Icon(
            imageVector = if (hasProgress) Icons.Outlined.Replay else Icons.Outlined.PlayArrow,
            contentDescription = null,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = if (hasProgress) {
                "Resume (${(progressPercentage * 100).toInt()}%)"
            } else {
                stringResource(R.string.play)
            },
            style = MaterialTheme.typography.titleSmall
        )
    }
}