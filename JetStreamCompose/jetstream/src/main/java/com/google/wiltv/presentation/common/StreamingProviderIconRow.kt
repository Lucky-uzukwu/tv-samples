package com.google.wiltv.presentation.common

import android.util.Log
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.PivotOffsets
import androidx.tv.foundation.lazy.list.TvLazyListState
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.google.wiltv.data.models.StreamingProvider


@OptIn(ExperimentalComposeUiApi::class, ExperimentalTvMaterial3Api::class)
@Composable
fun StreamingProvidersRow(
    modifier: Modifier = Modifier,
    onClick: (streamingProvider: StreamingProvider, index: Int) -> Unit,
    streamingProviders: List<StreamingProvider>,
    aboveFocusRequester: FocusRequester,
    lazyRowState: TvLazyListState,
    focusRequesters: Map<Int, FocusRequester> = emptyMap(),
    onItemFocused: (Int) -> Unit = {}
) {
    var hasFocus by remember { mutableStateOf(false) }

    Column {
        TvLazyRow(
            state = lazyRowState,
            pivotOffsets = PivotOffsets(0.1f, 0f),
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
                val focusRequester = focusRequesters[index]
                val streamingProvider = streamingProviders[index]
                val imageUrl = streamingProvider.logoUrl

                imageUrl?.let {
                    CustomCard(
                        onClick = { onClick(streamingProvider, index) },
                        modifier = Modifier
                            .then(
                                if (focusRequester != null) Modifier.focusRequester(focusRequester)
                                else Modifier
                            )
                            .onFocusChanged { focusState ->
                                if (focusState.hasFocus) {
                                    onItemFocused(index)
                                }
                            }
                            .focusProperties {
                                up = aboveFocusRequester
                            },
                        imageUrl = it,
                    )
                }
            }
        }

    }
}