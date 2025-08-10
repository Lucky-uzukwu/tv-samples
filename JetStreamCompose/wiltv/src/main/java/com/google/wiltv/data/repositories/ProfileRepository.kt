// ABOUTME: Repository interface for managing user profiles with CRUD operations
// ABOUTME: Handles profile creation, selection, deletion, and persistence using DataStore
package com.google.wiltv.data.repositories

import com.google.wiltv.data.entities.Profile
import com.google.wiltv.data.entities.ProfileType
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getAllProfiles(): Flow<List<Profile>>
    fun getSelectedProfile(): Flow<Profile?>
    suspend fun createProfile(name: String, avatarUrl: String, type: ProfileType): Profile
    suspend fun selectProfile(profileId: String)
    suspend fun updateProfile(profile: Profile)
    suspend fun deleteProfile(profileId: String)
    suspend fun initializeDefaultProfiles()
    suspend fun clearAllProfiles()
}