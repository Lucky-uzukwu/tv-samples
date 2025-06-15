package com.google.jetstream.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
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
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
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
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.presentation.theme.JetStreamButtonShape
import com.google.jetstream.presentation.utils.formatPLot
import com.google.jetstream.presentation.utils.formatVotes
import com.google.jetstream.presentation.utils.getImdbRating
import md_theme_light_onTertiary
import md_theme_light_outline
import md_theme_light_shadow

@Composable
fun CarouselItemForeground(
    movie: MovieNew,
    modifier: Modifier = Modifier,
    onWatchNowClick: () -> Unit,
    onMoreInfoClick: () -> Unit,
    playButtonFocusRequester: FocusRequester,
    displayTitleFocusRequester: FocusRequester,
    moreInfoButtonFocusRequester: FocusRequester,
) {
    LaunchedEffect(Unit) {
        playButtonFocusRequester.requestFocus()
    }

    val combinedGenre = movie.genres.joinToString(" ") { genre -> genre.name }
    val getYear = movie.releaseDate?.substring(0, 4)
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
            DisplayFilmTitle(
                movie.title, modifier = Modifier
                    .padding(top = 48.dp)
                    .focusRequester(displayTitleFocusRequester)
                    .focusable()
            )
            val formattedPlot = movie.plot.formatPLot()
            DisplayFilmGenericText(formattedPlot)

            Spacer(modifier = Modifier.height(10.dp))
            DisplayFilmExtraInfo(getYear, combinedGenre, movie.duration)


            Spacer(modifier = Modifier.height(10.dp))

            Row {
                IMDbLogo()
                Spacer(modifier = Modifier.width(8.dp))
                DisplayFilmGenericText(
                    "${
                        movie.imdbRating.getImdbRating()
                    }/10 - ${movie.imdbVotes.toString().formatVotes()} IMDB Votes"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            AnimatedVisibility(
                visible = true,
                content = {
                    Row {
                        if (movie.video != null) {
                            PlayButton(
                                onClick = onWatchNowClick,
                                focusRequester = playButtonFocusRequester,
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                        } else {
                            ComingSoonButton(
                                onClick = { },
                                focusRequester = playButtonFocusRequester,
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                        }
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
fun PlayButton(
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

