// ABOUTME: Profile section UI displaying current selected profile and switch profile functionality
// ABOUTME: TV-optimized layout with focus management and navigation to profile selection screen
package com.google.wiltv.presentation.screens.profile

import androidx.tv.material3.Border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import com.google.wiltv.presentation.screens.dashboard.rememberChildPadding

@Composable
fun ProfileSection(
    selectedProfile: Profile?,
    profiles: List<Profile>,
    onSwitchProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val childPadding = rememberChildPadding()
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = childPadding.start),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Current Profile Card
        selectedProfile?.let { profile ->
            Card(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(top = 32.dp),
                colors = CardDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = CardDefaults.border(
                    focusedBorder = Border(
                        border = androidx.compose.foundation.BorderStroke(
                            width = 2.dp,
                            color = Color(0xFFA855F7)
                        )
                    ),
                    pressedBorder = Border(
                        border = androidx.compose.foundation.BorderStroke(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Current Profile",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    // Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profile.avatarUrl.startsWith("http")) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(profile.avatarUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile avatar for ${profile.name}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            val avatarRes = when (profile.type) {
                                ProfileType.KIDS -> R.drawable.kids_avatar_new
                                ProfileType.DEFAULT -> R.drawable.default_avatar
                            }
                            AsyncImage(
                                model = avatarRes,
                                contentDescription = "Profile avatar for ${profile.name}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Profile Name
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    // Profile Type Badge
                    Text(
                        text = when (profile.type) {
                            ProfileType.DEFAULT -> "Default Profile"
                            ProfileType.KIDS -> "Kids Profile"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Switch Profile Button
        if (profiles.size > 1) {
            Button(
                onClick = onSwitchProfileClick,
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,

                modifier = Modifier
                    .focusRequester(focusRequester)
                    .width(200.dp),
                colors = ButtonDefaults.colors(
                    containerColor = Color(0xFFA855F7)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 2.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Switch Profile",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Summary
        if (profiles.size > 1) {
            Text(
                text = "${profiles.size} profiles available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = "No other profiles available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}