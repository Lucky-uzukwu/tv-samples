// ABOUTME: Profile entity representing a user profile with avatar, type, and metadata
// ABOUTME: Supports both Default and Kids profile types for content personalization
package com.google.wiltv.data.entities

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val type: ProfileType,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
enum class ProfileType {
    DEFAULT,

    KIDS
}