package com.google.wiltv.presentation.screens.movies

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ClassicCard
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.wiltv.R
import com.google.wiltv.data.util.StringConstants
import com.google.wiltv.presentation.common.EnhancedProfileImage
import com.google.wiltv.presentation.screens.dashboard.rememberChildPadding
import com.google.wiltv.presentation.screens.moviedetails.PersonToCharacter
import com.google.wiltv.presentation.theme.WilTvBorderWidth
import com.google.wiltv.presentation.theme.WilTvCardShape
import com.google.wiltv.presentation.utils.ourColors

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CastAndCrewList(
    castAndCrew: List<PersonToCharacter>,
) {
    val childPadding = rememberChildPadding()

    Column {
        Text(
            text = stringResource(R.string.cast_and_crew),
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.9f),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp
            ),
            modifier = Modifier.padding(start = childPadding.start)
        )
        // ToDo: specify the pivot offset
        LazyRow(
            modifier = Modifier
                .padding(top = 16.dp)
                .focusRestorer(),
            contentPadding = PaddingValues(start = childPadding.start)
        ) {
            items(castAndCrew, key = { it.person.id }) {
                CastAndCrewItem(it, modifier = Modifier.width(144.dp))
            }
        }
    }
}

@Composable
private fun CastAndCrewItem(
    personToCharacter: PersonToCharacter,
    modifier: Modifier = Modifier,
) {
    val castImageUrl =
        "https://api.nortv.xyz/" + "storage/" + personToCharacter.person.profilePath
    ClassicCard(
        modifier = modifier
            .padding(end = 20.dp, bottom = 16.dp)
            .aspectRatio(1 / 1.8f),
        shape = CardDefaults.shape(shape = WilTvCardShape),
        scale = CardDefaults.scale(focusedScale = 1f),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(
                    width = WilTvBorderWidth,
                    color = Color.White
                ),
                shape = WilTvCardShape
            )
        ),
        title = {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .padding(horizontal = 12.dp),
                text = personToCharacter.character ?: "",
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium,
                overflow = TextOverflow.Ellipsis
            )
        },
        subtitle = {
            Text(
                text = personToCharacter.person.name,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .alpha(0.75f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                overflow = TextOverflow.Ellipsis
            )
        },
        image = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.725f)
                    .background(ourColors.random())
            )
            EnhancedProfileImage(
                imageUrl = castImageUrl,
                contentDescription = StringConstants
                    .Composable
                    .ContentDescription
                    .image(personToCharacter.person.name),
                modifier = modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.725f),
                contentScale = ContentScale.Crop
            )
        },
        onClick = {}
    )
}
