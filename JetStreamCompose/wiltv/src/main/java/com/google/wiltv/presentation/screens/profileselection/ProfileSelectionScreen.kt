// ABOUTME: Profile selection screen with TV-optimized circular avatar grid layout
// ABOUTME: Implements focus management, navigation patterns and profile switching for streaming app
package com.google.wiltv.presentation.screens.profileselection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.wiltv.R
import com.google.wiltv.data.entities.Profile
import com.google.wiltv.data.entities.ProfileType
import com.google.wiltv.presentation.common.Error
import com.google.wiltv.presentation.common.Loading
import com.google.wiltv.presentation.screens.ErrorScreen

@OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileSelectionScreen(
    onProfileSelected: (Profile) -> Unit,
    onManageProfiles: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: ProfileSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is ProfileSelectionUiState.Loading -> {
            Loading(modifier = Modifier.fillMaxSize())
        }

        is ProfileSelectionUiState.Error -> {
            ErrorScreen(
                uiText = state.uiText,
                onRetry = { viewModel.retryOperation() },
                modifier = Modifier.fillMaxSize()
            )
        }

        is ProfileSelectionUiState.Ready -> {
            ProfileSelectionContent(
                profiles = state.profiles,
                selectedProfile = state.selectedProfile,
                catalogValidationPassed = state.catalogValidationPassed,
                onProfileSelected = { profile ->
                    viewModel.selectProfile(profile)
                    if (state.catalogValidationPassed) {
                        onProfileSelected(profile)
                    }
                },
                onManageProfiles = onManageProfiles,
                onLogout = onLogout
            )
        }
    }
}

@Composable
private fun ProfileSelectionContent(
    profiles: List<Profile>,
    selectedProfile: Profile?,
    onProfileSelected: (Profile) -> Unit,
    catalogValidationPassed: Boolean,
    onManageProfiles: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Who's Watching?",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Select a profile to personalize your WilTV experience.",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 64.dp)
        )

        // Profile Grid
        FlowRow(
            modifier = Modifier.padding(bottom = 64.dp),
            horizontalArrangement = Arrangement.spacedBy(48.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            maxItemsInEachRow = 3
        ) {
            profiles.forEach { profile ->
                ProfileAvatar(
                    profile = profile,
                    isSelected = profile.id == selectedProfile?.id,
                    onClick = { onProfileSelected(profile) }
                )
            }
        }

        // Logout Button
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            Button(
                onClick = onLogout,
                modifier = Modifier.padding(bottom = 32.dp),
                colors = ButtonDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    focusedContentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = ButtonDefaults.border(
                    focusedBorder = Border(
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface),
                        shape = MaterialTheme.shapes.medium
                    )
                ),
                scale = ButtonDefaults.scale(
                    focusedScale = 1.1f
                )
            ) {
                Text(
                    text = "Log Out",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

    }
}

@Composable
private fun ProfileAvatar(
    profile: Profile,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(160.dp)
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .size(110.dp)
                .focusRequester(focusRequester)
                .onFocusChanged { isFocused = it.isFocused },
            shape = CardDefaults.shape(shape = CircleShape),
            colors = CardDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = CardDefaults.border(
                focusedBorder = Border(
                    border = BorderStroke(4.dp, MaterialTheme.colorScheme.onSurface)
                ),
                pressedBorder = Border(
                    border = BorderStroke(4.dp, MaterialTheme.colorScheme.onSurface)
                )
            ),
            scale = CardDefaults.scale(
                focusedScale = 1.2f
            )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (profile.avatarUrl.startsWith("http")) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(profile.avatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile avatar for ${profile.name}",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Default avatar icon
                    val avatarRes = when (profile.type) {
                        ProfileType.KIDS -> R.drawable.kids_avatar_new
                        ProfileType.ADULT -> R.drawable.default_avatar
                    }
                    AsyncImage(
                        model = avatarRes,
                        contentDescription = "Profile avatar for ${profile.name}",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = profile.name,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 18.sp
            ),
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}