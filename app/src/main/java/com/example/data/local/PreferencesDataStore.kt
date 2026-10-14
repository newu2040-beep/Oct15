package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "oct15_settings")

class PreferencesDataStore(private val context: Context) {

    private val KEY_THEME = stringPreferencesKey("theme_name")
    private val KEY_BACKGROUND = stringPreferencesKey("background_style")
    private val KEY_FONT = stringPreferencesKey("font_style")
    private val KEY_NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
    private val KEY_CALL_REMINDER = booleanPreferencesKey("call_reminder_enabled")
    private val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_completed")
    private val KEY_DENSITY = stringPreferencesKey("layout_density")
    private val KEY_USER_NAME = stringPreferencesKey("user_display_name")
    private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
    private val KEY_USER_BIO = stringPreferencesKey("user_bio")
    private val KEY_USER_LOCATION = stringPreferencesKey("user_location")
    private val KEY_USER_AGE = stringPreferencesKey("user_age")
    private val KEY_USER_GENDER = stringPreferencesKey("user_gender")
    private val KEY_USER_PHOTO_URI = stringPreferencesKey("user_photo_uri")
    private val KEY_CUSTOM_BACKGROUND_URI = stringPreferencesKey("custom_background_uri")

    val themeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME] ?: "Lavender Dream"
    }

    val backgroundFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_BACKGROUND] ?: "Pastel"
    }

    val customBackgroundUriFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_CUSTOM_BACKGROUND_URI]
    }

    val fontFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_FONT] ?: "Default"
    }

    val notificationsFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_NOTIFICATIONS] ?: true
    }

    val callReminderFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_CALL_REMINDER] ?: true
    }

    val onboardingDoneFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_DONE] ?: false
    }

    private val KEY_COMPACT_MODE = stringPreferencesKey("compact_mode")

    val compactModeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_COMPACT_MODE] ?: "Auto"
    }

    val layoutDensityFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_DENSITY] ?: "Comfortable"
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_NAME] ?: "Rahul Shah"
    }

    val userEmailFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_EMAIL] ?: "rahul.shah@gmail.com"
    }

    val userBioFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_BIO] ?: "Collecting moments, not things."
    }

    val userLocationFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_LOCATION] ?: "Kathmandu, Nepal"
    }

    val userAgeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_AGE] ?: "28"
    }

    val userGenderFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_GENDER] ?: "Male"
    }

    val userPhotoUriFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_PHOTO_URI]
    }

    suspend fun setTheme(themeName: String) {
        context.dataStore.edit { it[KEY_THEME] = themeName }
    }

    suspend fun setBackground(background: String) {
        context.dataStore.edit { it[KEY_BACKGROUND] = background }
    }

    suspend fun setCustomBackground(uri: String) {
        context.dataStore.edit {
            it[KEY_BACKGROUND] = "Custom"
            it[KEY_CUSTOM_BACKGROUND_URI] = uri
        }
    }

    suspend fun setFont(font: String) {
        context.dataStore.edit { it[KEY_FONT] = font }
    }

    suspend fun setNotifications(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NOTIFICATIONS] = enabled }
    }

    suspend fun setCallReminder(enabled: Boolean) {
        context.dataStore.edit { it[KEY_CALL_REMINDER] = enabled }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_DONE] = done }
    }

    suspend fun setLayoutDensity(density: String) {
        context.dataStore.edit { it[KEY_DENSITY] = density }
    }

    suspend fun setCompactMode(mode: String) {
        context.dataStore.edit { it[KEY_COMPACT_MODE] = mode }
    }

    suspend fun updateProfilePhoto(uri: String?) {
        context.dataStore.edit {
            if (uri != null) it[KEY_USER_PHOTO_URI] = uri else it.remove(KEY_USER_PHOTO_URI)
        }
    }

    suspend fun updateUserProfile(
        name: String,
        email: String,
        bio: String,
        location: String,
        age: String,
        gender: String,
        photoUri: String? = null
    ) {
        val safeGender = if (gender.equals("Female", ignoreCase = true)) "Female" else "Male"
        context.dataStore.edit {
            it[KEY_USER_NAME] = name
            it[KEY_USER_EMAIL] = email
            it[KEY_USER_BIO] = bio
            it[KEY_USER_LOCATION] = location
            it[KEY_USER_AGE] = age
            it[KEY_USER_GENDER] = safeGender
            if (photoUri != null) {
                it[KEY_USER_PHOTO_URI] = photoUri
            }
        }
    }
}
