package com.example.myapplication

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.sessionDataStore by preferencesDataStore(name = "user_session")

data class UserSession(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val provider: String = ""
) {
    val isLoggedIn get() = userId.isNotEmpty()
    val initials get() = name.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
        .joinToString("")
        .ifEmpty { if (email.isNotEmpty()) email.first().uppercaseChar().toString() else "?" }
}

class UserSessionManager(private val context: Context) {

    companion object {
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_NAME = stringPreferencesKey("name")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_PHOTO_URL = stringPreferencesKey("photo_url")
        private val KEY_PROVIDER = stringPreferencesKey("provider")
        private val KEY_PHONE = stringPreferencesKey("phone")
        private val KEY_COMPANY = stringPreferencesKey("company")
        private val KEY_ROLE = stringPreferencesKey("role")
        val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_2FA_ENABLED = booleanPreferencesKey("2fa_enabled")
        val KEY_NOTIFICATIONS = booleanPreferencesKey("notifications")
    }

    val sessionFlow: Flow<UserSession> = context.sessionDataStore.data.map { prefs ->
        UserSession(
            userId = prefs[KEY_USER_ID] ?: "",
            name = prefs[KEY_NAME] ?: "",
            email = prefs[KEY_EMAIL] ?: "",
            photoUrl = prefs[KEY_PHOTO_URL] ?: "",
            provider = prefs[KEY_PROVIDER] ?: ""
        )
    }

    val themeFlow: Flow<String> = context.sessionDataStore.data.map { it[KEY_THEME] ?: "system" }
    val languageFlow: Flow<String> = context.sessionDataStore.data.map { it[KEY_LANGUAGE] ?: "en" }
    val twoFaFlow: Flow<Boolean> = context.sessionDataStore.data.map { it[KEY_2FA_ENABLED] ?: false }
    val notificationsFlow: Flow<Boolean> = context.sessionDataStore.data.map { it[KEY_NOTIFICATIONS] ?: true }
    val phoneFlow: Flow<String> = context.sessionDataStore.data.map { it[KEY_PHONE] ?: "" }
    val companyFlow: Flow<String> = context.sessionDataStore.data.map { it[KEY_COMPANY] ?: "" }
    val roleFlow: Flow<String> = context.sessionDataStore.data.map { it[KEY_ROLE] ?: "" }

    fun getThemeSync(): String = runBlocking { themeFlow.first() }
    fun getLanguageSync(): String = runBlocking { languageFlow.first() }

    suspend fun saveUser(session: UserSession) {
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_USER_ID] = session.userId
            prefs[KEY_NAME] = session.name
            prefs[KEY_EMAIL] = session.email
            prefs[KEY_PHOTO_URL] = session.photoUrl
            prefs[KEY_PROVIDER] = session.provider
        }
    }

    suspend fun updatePersonalDetails(name: String, phone: String, company: String, role: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_NAME] = name
            prefs[KEY_PHONE] = phone
            prefs[KEY_COMPANY] = company
            prefs[KEY_ROLE] = role
        }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { prefs ->
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_NAME)
            prefs.remove(KEY_EMAIL)
            prefs.remove(KEY_PHOTO_URL)
            prefs.remove(KEY_PROVIDER)
        }
    }

    suspend fun saveTheme(theme: String) {
        context.sessionDataStore.edit { it[KEY_THEME] = theme }
    }

    suspend fun saveLanguage(language: String) {
        context.sessionDataStore.edit { it[KEY_LANGUAGE] = language }
    }

    suspend fun setTwoFaEnabled(enabled: Boolean) {
        context.sessionDataStore.edit { it[KEY_2FA_ENABLED] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.sessionDataStore.edit { it[KEY_NOTIFICATIONS] = enabled }
    }
}
