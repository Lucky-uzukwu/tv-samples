// ABOUTME: DataStore-based implementation of ProfileRepository for local profile management
// ABOUTME: Persists profiles as JSON in Preferences DataStore with selected profile tracking
package com.google.wiltv.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.wiltv.data.entities.Profile
import com.google.wiltv.data.entities.ProfileType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val gson: Gson
) : ProfileRepository {

    private val PROFILES_KEY = stringPreferencesKey("profiles")
    private val SELECTED_PROFILE_ID_KEY = stringPreferencesKey("selected_profile_id")

    override fun getAllProfiles(): Flow<List<Profile>> {
        return dataStore.data.map { preferences ->
            val profilesJson = preferences[PROFILES_KEY] ?: "[]"
            val type = object : TypeToken<List<Profile>>() {}.type
            gson.fromJson(profilesJson, type) ?: emptyList()
        }
    }

    override fun getSelectedProfile(): Flow<Profile?> {
        return dataStore.data.map { preferences ->
            val selectedId = preferences[SELECTED_PROFILE_ID_KEY]
            if (selectedId != null) {
                val profilesJson = preferences[PROFILES_KEY] ?: "[]"
                val type = object : TypeToken<List<Profile>>() {}.type
                val profiles: List<Profile> = gson.fromJson(profilesJson, type) ?: emptyList()
                profiles.find { it.id == selectedId }
            } else {
                null
            }
        }
    }

    override suspend fun createProfile(name: String, avatarUrl: String, type: ProfileType): Profile {
        val newProfile = Profile(
            id = UUID.randomUUID().toString(),
            name = name,
            avatarUrl = avatarUrl,
            type = type,
            isDefault = false
        )

        dataStore.edit { preferences ->
            val profilesJson = preferences[PROFILES_KEY] ?: "[]"
            val typeToken = object : TypeToken<List<Profile>>() {}.type
            val currentProfiles: MutableList<Profile> = 
                gson.fromJson<List<Profile>>(profilesJson, typeToken)?.toMutableList() ?: mutableListOf()
            
            currentProfiles.add(newProfile)
            preferences[PROFILES_KEY] = gson.toJson(currentProfiles)
        }

        return newProfile
    }

    override suspend fun selectProfile(profileId: String) {
        dataStore.edit { preferences ->
            preferences[SELECTED_PROFILE_ID_KEY] = profileId
        }
    }

    override suspend fun updateProfile(profile: Profile) {
        dataStore.edit { preferences ->
            val profilesJson = preferences[PROFILES_KEY] ?: "[]"
            val typeToken = object : TypeToken<List<Profile>>() {}.type
            val currentProfiles: MutableList<Profile> = 
                gson.fromJson<List<Profile>>(profilesJson, typeToken)?.toMutableList() ?: mutableListOf()
            
            val index = currentProfiles.indexOfFirst { it.id == profile.id }
            if (index != -1) {
                currentProfiles[index] = profile
                preferences[PROFILES_KEY] = gson.toJson(currentProfiles)
            }
        }
    }

    override suspend fun deleteProfile(profileId: String) {
        dataStore.edit { preferences ->
            val profilesJson = preferences[PROFILES_KEY] ?: "[]"
            val typeToken = object : TypeToken<List<Profile>>() {}.type
            val currentProfiles: MutableList<Profile> = 
                gson.fromJson<List<Profile>>(profilesJson, typeToken)?.toMutableList() ?: mutableListOf()
            
            currentProfiles.removeAll { it.id == profileId }
            preferences[PROFILES_KEY] = gson.toJson(currentProfiles)

            // Clear selected profile if it was deleted
            if (preferences[SELECTED_PROFILE_ID_KEY] == profileId) {
                preferences.remove(SELECTED_PROFILE_ID_KEY)
            }
        }
    }

    override suspend fun initializeDefaultProfiles() {
        dataStore.edit { preferences ->
            val profilesJson = preferences[PROFILES_KEY] ?: "[]"
            val typeToken = object : TypeToken<List<Profile>>() {}.type
            val currentProfiles: List<Profile> = gson.fromJson(profilesJson, typeToken) ?: emptyList()
            
            if (currentProfiles.isEmpty()) {
                val defaultProfiles = listOf(
                    Profile(
                        id = "default-adult",
                        name = "Adult",
                        avatarUrl = "default_avatar",
                        type = ProfileType.ADULT,
                        isDefault = true
                    ),
                    Profile(
                        id = "default-kids",
                        name = "Kids",
                        avatarUrl = "kids_avatar_new",
                        type = ProfileType.KIDS,
                        isDefault = false
                    )
                )
                preferences[PROFILES_KEY] = gson.toJson(defaultProfiles)
            }
        }
    }

    override suspend fun clearAllProfiles() {
        dataStore.edit { preferences ->
            preferences.remove(PROFILES_KEY)
            preferences.remove(SELECTED_PROFILE_ID_KEY)
        }
    }
}