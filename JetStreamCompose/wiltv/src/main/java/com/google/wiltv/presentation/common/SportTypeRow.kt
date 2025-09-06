package com.google.wiltv.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.PivotOffsets
import androidx.tv.foundation.lazy.list.TvLazyListState
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.wiltv.data.entities.SportType


@OptIn(ExperimentalComposeUiApi::class, ExperimentalTvMaterial3Api::class)
@Composable
fun SportTypeRow(
    modifier: Modifier = Modifier,
    onClick: (sportType: SportType, index: Int) -> Unit,
    sportTypes: List<SportType>,
    aboveFocusRequester: FocusRequester,
    lazyRowState: TvLazyListState,
    focusRequesters: Map<Int, FocusRequester> = emptyMap(),
    downFocusRequester: FocusRequester? = null,
    onItemFocused: (Int) -> Unit = {}
) {
    var hasFocus by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "Sports",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 30.sp
            ),
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier
                .alpha(1f)
                .padding(start = 80.dp, top = 16.dp, bottom = 16.dp)
        )
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
                count = sportTypes.size,
                key = { index -> sportTypes[index].id }
            ) { index ->
                val focusRequester = focusRequesters[index]
                val sportType = sportTypes[index]

                CustomCard(
                    onClick = { onClick(sportType, index) },
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
                            downFocusRequester?.let { down = it }
                        },
                    imageUrl = sportType.logoUrl,
                    text = sportType.name,
                    enhancedImageModifier = Modifier
                        .padding(all = 20.dp),
                    customShape = RoundedCornerShape(100.dp)
                )
            }
        }

    }
}