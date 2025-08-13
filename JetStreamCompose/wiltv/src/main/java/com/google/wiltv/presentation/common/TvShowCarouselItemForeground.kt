package com.google.wiltv.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.wiltv.R
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.presentation.utils.formatVotes
import com.google.wiltv.presentation.utils.getImdbRating
import kotlinx.coroutines.launch

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TvShowCarouselItemForeground(
    tvShow: TvShow,
    modifier: Modifier = Modifier,
    onMoreInfoClick: () -> Unit,
    isCarouselFocused: Boolean = false
) {
    val combinedGenre = tvShow.genres?.joinToString(" ") { genre -> genre.name }
    val getYear = tvShow.releaseDate?.substring(0, 4)
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .padding(start = 34.dp, bottom = 32.dp)
            .width(360.dp)
            .bringIntoViewRequester(bringIntoViewRequester),
        verticalArrangement = Arrangement.Bottom
    ) {
        Row(
            modifier = Modifier.padding(bottom = 5.dp),
        ) {
            DisplayFilmExtraInfo(
                getYear = getYear ?: "",
                combinedGenre = combinedGenre ?: "",
                duration = tvShow.duration
            )
        }
        tvShow.title?.let {
            DisplayFilmTitle(
                title = it,
                style = MaterialTheme.typography.displaySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 2
            )
        }
        tvShow.plot?.let {
            DisplayFilmGenericText(
                modifier = Modifier.padding(top = 4.dp),
                text = it,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 3
            )
        }

        Row(
            modifier = Modifier.padding(top = 12.dp, bottom = 28.dp)
        ) {
            DisplayFilmGenericText(
                text = "${
                    tvShow.imdbRating.getImdbRating()
                }/10 - ${tvShow.imdbVotes.toString().formatVotes()} Votes",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                fontWeight = FontWeight.Light
            )
            Spacer(modifier = Modifier.width(8.dp))
            IMDbLogo()
        }

        AnimatedVisibility(visible = isCarouselFocused) {
            if (tvShow.seasons?.first()?.episodes?.first()?.video != null) {
                CustomFillButton(
                    onClick = {
                        coroutineScope.launch {
                            bringIntoViewRequester.bringIntoView()
                        }
                        onMoreInfoClick()
                    },
                    text = stringResource(R.string.play),
                    icon = R.drawable.play_icon,
                    iconTint = MaterialTheme.colorScheme.inverseOnSurface,
                    buttonColor = ButtonDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.inverseSurface,
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        focusedContentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    ),
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CustomFillButton(
                        onClick = {
                            coroutineScope.launch {
                                bringIntoViewRequester.bringIntoView()
                            }
                            onMoreInfoClick()
                        },
                        text = stringResource(R.string.more_info),
                        icon = R.drawable.ic_info,
                        iconTint = MaterialTheme.colorScheme.inverseOnSurface,
                        buttonColor = ButtonDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.inverseSurface,
                            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                            focusedContentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        ),
                    )
                }
            }

        }
    }

}
