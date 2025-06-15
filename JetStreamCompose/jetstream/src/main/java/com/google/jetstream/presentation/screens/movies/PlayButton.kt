package com.google.jetstream.presentation.screens.movies

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.jetstream.R
import com.google.jetstream.presentation.theme.JetStreamButtonShape
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
        modifier = modifier
            .height(40.dp)
            .focusRequester(focusRequester),
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        shape = ButtonDefaults.shape(shape = JetStreamButtonShape),
        colors = ButtonDefaults.colors(
            containerColor = md_theme_light_outline,
            contentColor = md_theme_light_onTertiary,
            focusedContainerColor = md_theme_light_onTertiary,
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