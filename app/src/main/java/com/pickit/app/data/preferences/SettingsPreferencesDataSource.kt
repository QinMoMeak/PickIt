package com.pickit.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pickit.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "pickit_settings")

data class SettingsPreferences(
    val aiProvider: String = DEFAULT_AI_PROVIDER,
    val apiBaseUrl: String = DEFAULT_API_BASE_URL,
    val aiApiKey: String = DEFAULT_AI_API_KEY,
    val aiModel: String = DEFAULT_AI_MODEL,
    val aiTimeoutSeconds: Int = DEFAULT_AI_TIMEOUT_SECONDS,
    val aiEnableThinking: Boolean = DEFAULT_AI_ENABLE_THINKING,
    val aiMaxTokens: Int = DEFAULT_AI_MAX_TOKENS,
    val aiTemperature: Double = DEFAULT_AI_TEMPERATURE,
    val webDavPath: String = DEFAULT_WEBDAV_PATH,
)

const val DEFAULT_AI_PROVIDER = BuildConfig.AI_PROVIDER
const val DEFAULT_API_BASE_URL = BuildConfig.AI_BASE_URL
const val DEFAULT_AI_API_KEY = BuildConfig.AI_API_KEY
const val DEFAULT_AI_MODEL = BuildConfig.AI_MODEL
const val DEFAULT_AI_TIMEOUT_SECONDS = BuildConfig.AI_TIMEOUT_SECONDS
const val DEFAULT_AI_ENABLE_THINKING = BuildConfig.AI_ENABLE_THINKING
const val DEFAULT_AI_MAX_TOKENS = BuildConfig.AI_MAX_TOKENS
const val DEFAULT_AI_TEMPERATURE = BuildConfig.AI_TEMPERATURE
const val DEFAULT_WEBDAV_PATH = "/goodstash/"

@Singleton
class SettingsPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val AiProvider = stringPreferencesKey("ai_provider")
        val ApiBaseUrl = stringPreferencesKey("api_base_url")
        val AiApiKey = stringPreferencesKey("ai_api_key")
        val AiModel = stringPreferencesKey("ai_model")
        val AiTimeoutSeconds = intPreferencesKey("ai_timeout_seconds")
        val AiEnableThinking = booleanPreferencesKey("ai_enable_thinking")
        val AiMaxTokens = intPreferencesKey("ai_max_tokens")
        val AiTemperature = stringPreferencesKey("ai_temperature")
        val WebDavPath = stringPreferencesKey("webdav_path")
    }

    val settingsFlow: Flow<SettingsPreferences> = context.settingsDataStore.data.map { preferences ->
        SettingsPreferences(
            aiProvider = preferences[Keys.AiProvider] ?: DEFAULT_AI_PROVIDER,
            apiBaseUrl = preferences[Keys.ApiBaseUrl] ?: DEFAULT_API_BASE_URL,
            aiApiKey = preferences[Keys.AiApiKey] ?: DEFAULT_AI_API_KEY,
            aiModel = preferences[Keys.AiModel] ?: DEFAULT_AI_MODEL,
            aiTimeoutSeconds = preferences[Keys.AiTimeoutSeconds] ?: DEFAULT_AI_TIMEOUT_SECONDS,
            aiEnableThinking = preferences[Keys.AiEnableThinking] ?: DEFAULT_AI_ENABLE_THINKING,
            aiMaxTokens = preferences[Keys.AiMaxTokens] ?: DEFAULT_AI_MAX_TOKENS,
            aiTemperature = preferences[Keys.AiTemperature]?.toDoubleOrNull() ?: DEFAULT_AI_TEMPERATURE,
            webDavPath = preferences[Keys.WebDavPath] ?: DEFAULT_WEBDAV_PATH,
        )
    }

    suspend fun updateAiProvider(value: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.AiProvider] = value.trim().ifEmpty { DEFAULT_AI_PROVIDER }
        }
    }

    suspend fun updateApiBaseUrl(value: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.ApiBaseUrl] = value.trim().ifEmpty { DEFAULT_API_BASE_URL }
        }
    }

    suspend fun updateAiApiKey(value: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.AiApiKey] = value.trim().ifEmpty { DEFAULT_AI_API_KEY }
        }
    }

    suspend fun updateAiModel(value: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.AiModel] = value.trim().ifEmpty { DEFAULT_AI_MODEL }
        }
    }

    suspend fun updateAiTimeoutSeconds(value: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.AiTimeoutSeconds] = value.coerceAtLeast(1)
        }
    }

    suspend fun updateAiEnableThinking(value: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.AiEnableThinking] = value
        }
    }

    suspend fun updateAiMaxTokens(value: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.AiMaxTokens] = value.coerceAtLeast(1)
        }
    }

    suspend fun updateAiTemperature(value: Double) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.AiTemperature] = value.toString()
        }
    }

    suspend fun updateWebDavPath(value: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.WebDavPath] = value.trim().ifEmpty { DEFAULT_WEBDAV_PATH }
        }
    }
}
