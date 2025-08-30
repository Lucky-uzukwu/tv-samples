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
            deserializeProfiles(profilesJson)
        }
    }

    override fun getSelectedProfile(): Flow<Profile?> {
        return dataStore.data.map { preferences ->
            val selectedId = preferences[SELECTED_PROFILE_ID_KEY]
            if (selectedId != null) {
                val profilesJson = preferences[PROFILES_KEY] ?: "[]"
                val profiles = deserializeProfiles(profilesJson)
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
            val currentProfiles = deserializeProfiles(profilesJson).toMutableList()
            
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
            val currentProfiles = deserializeProfiles(profilesJson).toMutableList()
            
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
            val currentProfiles = deserializeProfiles(profilesJson).toMutableList()
            
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
            val currentProfiles = deserializeProfiles(profilesJson)
            
            if (currentProfiles.isEmpty()) {
                val defaultProfiles = listOf(
                    Profile(
                        id = "default-profile",
                        name = "Default",
                        avatarUrl = "default_avatar",
                        type = ProfileType.DEFAULT,
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

    /**
     * Deserializes profiles JSON with migration of legacy data
     * This handles any existing data that might still contain old values
     */
    private fun deserializeProfiles(profilesJson: String): List<Profile> {
        // Replace any legacy references
        val migratedJson = profilesJson
            .replace("\"ADULT\"", "\"DEFAULT\"")
            .replace("\"name\":\"Adult\"", "\"name\":\"Default\"")
            .replace("\"default-adult\"", "\"default-profile\"")
        val type = object : TypeToken<List<Profile>>() {}.type
        return gson.fromJson(migratedJson, type) ?: emptyList()
    }
}