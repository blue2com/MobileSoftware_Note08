package com.example.note08.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private object SettingsKeys {
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val NOTIFICATION = booleanPreferencesKey("notification")
}

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val darkModeFlow: Flow<Boolean> = dataStore.data.map { it[SettingsKeys.DARK_MODE] ?: false }
    val notificationFlow: Flow<Boolean> = dataStore.data.map { it[SettingsKeys.NOTIFICATION] ?: true }

    suspend fun setDarkMode(value: Boolean) {
        dataStore.edit { it[SettingsKeys.DARK_MODE] = value }
    }

    suspend fun setNotification(value: Boolean) {
        dataStore.edit { it[SettingsKeys.NOTIFICATION] = value }
    }
}