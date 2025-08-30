// ABOUTME: Utility class for determining content filtering parameters based on user profile type
// ABOUTME: Maps ProfileType to appropriate isAdultContent/isKidsContent values for API calls
package com.google.wiltv.data.utils

import com.google.wiltv.data.entities.Profile
import com.google.wiltv.data.entities.ProfileType

object ProfileContentHelper {

    data class ContentFilterParams(
        val isAdultContent: Int?,
        val isKidsContent: Int?
    )

    /**
     * Determines the appropriate content filtering parameters based on the selected profile.
     *
     * @param selectedProfile The currently selected user profile, or null if none selected
     * @return ContentFilterParams with appropriate values for API calls
     *         - KIDS profile: isKidsContent=1, isAdultContent=0
     *         - DEFAULT profile: isAdultContent=1, isKidsContent=0
     *         - No profile: both null (no filtering)
     */
    fun getContentFilterParams(selectedProfile: Profile?): ContentFilterParams {
        return when (selectedProfile?.type) {
            ProfileType.KIDS -> ContentFilterParams(
                isAdultContent = null,
                isKidsContent = 1
            )

            ProfileType.DEFAULT -> ContentFilterParams(
                isAdultContent = null,
                isKidsContent = null
            )

            null -> ContentFilterParams(
                isAdultContent = null,
                isKidsContent = null
            )
        }
    }
}