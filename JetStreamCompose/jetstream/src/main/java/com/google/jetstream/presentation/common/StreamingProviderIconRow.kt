package com.google.jetstream.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.rememberTvLazyListState
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.google.jetstream.data.models.StreamingProvider


@OptIn(ExperimentalComposeUiApi::class, ExperimentalTvMaterial3Api::class)
@Composable
fun StreamingProvidersRow(
    modifier: Modifier = Modifier,
    onClick: (streamingProvider: StreamingProvider) -> Unit,
    streamingProviders: List<StreamingProvider>,
) {
    val lazyRowState = rememberTvLazyListState()
    var hasFocus by remember { mutableStateOf(false) }

    Column {
        TvLazyRow(
            state = lazyRowState,
            modifier = modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    hasFocus = focusState.hasFocus
                },
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = 32.dp)
        ) {
            items(
                count = streamingProviders.size,
                key = { index -> streamingProviders[index].id }
            ) { index ->
                val streamingProvider = streamingProviders[index]
                if (streamingProvider.logoPath != null) {
                    val imageUrl = "https://stage.nortv.xyz/storage/${streamingProvider.logoPath}"
                    CustomCard(
                        onClick = { onClick(streamingProvider) },
                        modifier = Modifier,
                        imageUrl = imageUrl,
                    )
                }

            }
        }

    }
}