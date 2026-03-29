package com.pickit.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "pickit_settings")

data class SettingsPreferences(
    val apiBaseUrl: String = DEFAULT_API_BASE_URL,
    val webDavPath: String = DEFAULT_WEBDAV_PATH,
)

const val DEFAULT_API_BASE_URL = "https://example.com"
const val DEFAULT_WEBDAV_PATH = "/goodstash/"

@Singleton
class SettingsPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val ApiBaseUrl = stringPreferencesKey("api_base_url")
        val WebDavPath = stringPreferencesKey("webdav_path")
    }

    val settingsFlow: Flow<SettingsPreferences> = context.settingsDataStore.data.map { preferences ->
        SettingsPreferences(
            apiBaseUrl = preferences[Keys.ApiBaseUrl] ?: DEFAULT_API_BASE_URL,
            webDavPath = preferences[Keys.WebDavPath] ?: DEFAULT_WEBDAV_PATH,
        )
    }

    suspend fun updateApiBaseUrl(value: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.ApiBaseUrl] = value.trim().ifEmpty { DEFAULT_API_BASE_URL }
        }
    }

    suspend fun updateWebDavPath(value: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.WebDavPath] = value.trim().ifEmpty { DEFAULT_WEBDAV_PATH }
        }
    }
}
