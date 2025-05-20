package com.google.jetstream.data.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.jetstream.data.entities.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

@Singleton
class UserRepository @Inject constructor(
    private val context: Context
) {
    companion object {
        private val KEY_USER_TOKEN = stringPreferencesKey("user_token")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_PASSWORD = stringPreferencesKey("user_password")
        private val KEY_USER_CLIENT_IP = stringPreferencesKey("user_client_ip")
        private val KEY_USER_DEVICE_NAME = stringPreferencesKey("user_device_name")
        private val KEY_USER_DEVICE_MAC_ADDRESS = stringPreferencesKey("user_device_mac_address")
        private val KEY_USER_ACCESS_CODE = stringPreferencesKey("user_access_code")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_PROFILE_PHOTO_PATH = stringPreferencesKey("user_profile_photo_path")
        private val KEY_USER_PROFILE_PHOTO_URL = stringPreferencesKey("user_profile_photo_url")
    }

    // Reading methods
    val userToken: Flow<String?> = context.dataStore.data.map { it[KEY_USER_TOKEN] }
    val userId: Flow<String?> = context.dataStore.data.map { it[KEY_USER_ID] }
    val userEmail: Flow<String?> = context.dataStore.data.map { it[KEY_USER_EMAIL] }
    val userAccessCode: Flow<String?> = context.dataStore.data.map { it[KEY_USER_ACCESS_CODE] }
    val userName: Flow<String?> = context.dataStore.data.map { it[KEY_USER_NAME] }
    val userPassword: Flow<String?> = context.dataStore.data.map { it[KEY_USER_PASSWORD] }
    val userClientIp: Flow<String?> = context.dataStore.data.map { it[KEY_USER_CLIENT_IP] }
    val userDeviceName: Flow<String?> = context.dataStore.data.map { it[KEY_USER_DEVICE_NAME] }
    val userDeviceMacAddress: Flow<String?> =
        context.dataStore.data.map { it[KEY_USER_DEVICE_MAC_ADDRESS] }
    val userProfilePhotoPath: Flow<String?> =
        context.dataStore.data.map { it[KEY_USER_PROFILE_PHOTO_PATH] }
    val userProfilePhotoUrl: Flow<String?> =
        context.dataStore.data.map { it[KEY_USER_PROFILE_PHOTO_URL] }

    // Writing methods
    suspend fun saveUserToken(token: String) {
        context.dataStore.edit { it[KEY_USER_TOKEN] = token }
    }

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { it[KEY_USER_ID] = userId }
    }

    suspend fun saveUserEmail(email: String) {
        context.dataStore.edit { it[KEY_USER_EMAIL] = email }
    }

    suspend fun saveUserAccessCode(accessCode: String) {
        context.dataStore.edit { it[KEY_USER_ACCESS_CODE] = accessCode }
    }

    suspend fun saveUserName(userName: String) {
        context.dataStore.edit { it[KEY_USER_NAME] = userName }
    }

    suspend fun saveUserPassword(password: String) {
        context.dataStore.edit { it[KEY_USER_PASSWORD] = password }
    }

    suspend fun saveUserClientIp(clientIp: String) {
        context.dataStore.edit { it[KEY_USER_CLIENT_IP] = clientIp }
    }

    suspend fun saveUserDeviceName(deviceName: String) {
        context.dataStore.edit { it[KEY_USER_DEVICE_NAME] = deviceName }
    }

    suspend fun saveUserDeviceMacAddress(deviceMacAddress: String) {
        context.dataStore.edit { it[KEY_USER_DEVICE_MAC_ADDRESS] = deviceMacAddress }
    }

    suspend fun saveUserProfilePhotoPath(profilePhotoPath: String) {
        context.dataStore.edit { it[KEY_USER_PROFILE_PHOTO_PATH] = profilePhotoPath }
    }

    suspend fun saveUserProfilePhotoUrl(profilePhotoUrl: String) {
        context.dataStore.edit { it[KEY_USER_PROFILE_PHOTO_URL] = profilePhotoUrl }
    }

    suspend fun getUser(): User? {
        return User(
            id = userId.first() ?: "",
            email = userEmail.first() ?: "",
            accessCode = userAccessCode.first() ?: "",
            name = userName.first() ?: "",
            password = userPassword.first() ?: "",
            clientIp = userClientIp.first() ?: "",
            deviceName = userDeviceName.first() ?: "",
            deviceMacAddress = userDeviceMacAddress.first() ?: "",
            profilePhotoPath = userProfilePhotoPath.first() ?: "",
            profilePhotoUrl = userProfilePhotoUrl.first() ?: ""
        )
    }

    // Clear all user data
    suspend fun clearUserData() {
        context.dataStore.edit { it.clear() }
    }
}