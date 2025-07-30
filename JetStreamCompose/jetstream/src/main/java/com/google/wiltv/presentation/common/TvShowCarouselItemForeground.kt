package com.google.wiltv.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import com.google.wiltv.R
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.presentation.utils.formatVotes
import com.google.wiltv.presentation.utils.getImdbRating

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvShowCarouselItemForeground(
    tvShow: TvShow,
    modifier: Modifier = Modifier,
    onMoreInfoClick: () -> Unit,
    isCarouselFocused: Boolean = false
) {
    val combinedGenre = tvShow.genres?.joinToString(" ") { genre -> genre.name }
    val getYear = tvShow.releaseDate?.substring(0, 4)
    Column(
        modifier = modifier
            .padding(start = 34.dp, bottom = 32.dp)
            .width(360.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Row(
            modifier = Modifier.padding(bottom = 5.dp),
        ) {
            DisplayFilmExtraInfo(
                getYear = getYear,
                combinedGenre = combinedGenre,
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
            CustomFillButton(
                onClick = onMoreInfoClick,
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
