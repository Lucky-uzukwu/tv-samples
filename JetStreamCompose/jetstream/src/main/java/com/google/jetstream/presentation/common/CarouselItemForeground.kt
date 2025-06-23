package com.google.jetstream.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.jetstream.R
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.models.TvShow
import com.google.jetstream.presentation.theme.JetStreamButtonShape
import com.google.jetstream.presentation.utils.formatPLot
import com.google.jetstream.presentation.utils.formatVotes
import com.google.jetstream.presentation.utils.getImdbRating
import md_theme_light_onTertiary
import md_theme_light_outline
import md_theme_light_shadow

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CarouselItemForeground(
    movie: MovieNew,
    modifier: Modifier = Modifier,
    onWatchNowClick: () -> Unit,
    isCarouselFocused: Boolean = false
) {
    val combinedGenre = movie.genres.take(2).joinToString(" ") { genre -> genre.name }
    val getYear = movie.releaseDate?.substring(0, 4)
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
                duration = movie.duration
            )
        }
        DisplayFilmTitle(
            title = movie.title,
            style = MaterialTheme.typography.displaySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            maxLines = 2
        )
        val formattedPlot = movie.plot.formatPLot()
        DisplayFilmGenericText(
            modifier = Modifier.padding(top = 4.dp),
            text = formattedPlot,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            maxLines = 3
        )

        Row(
            modifier = Modifier.padding(top = 12.dp, bottom = 28.dp)
        ) {
            DisplayFilmGenericText(
                text = "${
                    movie.imdbRating.getImdbRating()
                }/10 - ${movie.imdbVotes.toString().formatVotes()} IMDB Votes",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                fontWeight = FontWeight.Light
            )
            Spacer(modifier = Modifier.width(8.dp))
            IMDbLogo()
        }

        AnimatedVisibility(visible = isCarouselFocused) {
            if (movie.video != null) {
                CustomFillButton(
                    onClick = onWatchNowClick,
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
                CustomFillButton(
                    onClick = { },
                    text = stringResource(R.string.coming_soon),
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

@Composable
fun CarouselItemForeground(
    tvShow: TvShow,
    modifier: Modifier = Modifier,
    onMoreInfoClick: () -> Unit,
    displayTitleFocusRequester: FocusRequester,
    moreInfoButtonFocusRequester: FocusRequester,
) {
    val combinedGenre = tvShow.genres?.joinToString(" ") { genre -> genre.name }
    val getYear = tvShow.releaseDate?.substring(0, 4)
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .focusGroup()
                .padding(32.dp),
            verticalArrangement = Arrangement.Top
        ) {
            tvShow.title?.let {
                DisplayFilmTitle(
                    title = it,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1
                )
            }
            val formattedPlot = tvShow.plot.formatPLot()
            DisplayFilmGenericText(
                modifier = Modifier.padding(top = 12.dp, bottom = 28.dp),
                text = formattedPlot,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(10.dp))
//            DisplayFilmExtraInfo(getYear, combinedGenre, tvShow.duration)


            Spacer(modifier = Modifier.height(10.dp))

            Row {
                IMDbLogo()
                Spacer(modifier = Modifier.width(8.dp))
                DisplayFilmGenericText(
                    modifier = Modifier.padding(top = 12.dp, bottom = 28.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 3,
                    text = "${
                        tvShow.imdbRating.getImdbRating()
                    }/10 - ${tvShow.imdbVotes.toString().formatVotes()} IMDB Votes"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            AnimatedVisibility(
                visible = true,
                content = {
                    Row {
                        MoreInfoButton(
                            onClick = onMoreInfoClick,
                            focusRequester = moreInfoButtonFocusRequester,
                        )
                    }
                }
            )
        }
    }
}


@Composable
fun MoreInfoButton(
    onClick: () -> Unit,
    focusRequester: FocusRequester
) {
    Button(
        onClick = onClick,
        modifier = Modifier
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
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = stringResource(R.string.more_info),
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@Composable
fun ComingSoonButton(
    onClick: () -> Unit,
    focusRequester: FocusRequester,
) {

    Button(
        onClick = onClick,
        modifier = Modifier
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
            imageVector = Icons.Outlined.Add,
            contentDescription = null,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = stringResource(R.string.coming_soon),
            style = MaterialTheme.typography.titleSmall
        )
    }
}

