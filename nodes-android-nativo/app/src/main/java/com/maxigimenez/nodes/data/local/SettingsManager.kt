package com.maxigimenez.nodes.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.maxigimenez.nodes.data.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// 1. Definición del DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

// 3. El Gestor
class SettingsManager(context: Context) {

    private val settingsDataStore = context.dataStore

    // 4. Claves (¡Actualizadas!)
    companion object {
        val KEY_LANGUAGE = stringPreferencesKey("key_language")
        val KEY_THEME = stringPreferencesKey("key_theme")
        val KEY_ALLOW_NOTIFICATIONS = booleanPreferencesKey("key_allow_notifications")
        val KEY_FONT_SIZE = intPreferencesKey("key_font_size") // <-- ¡AÑADIDO!
    }

    // 5. Flow para LEER (¡Actualizado!)
    val appSettingsFlow: Flow<AppSettings> = settingsDataStore.data.catch { exception ->
        if (exception is IOException) {
            Log.e("SettingsManager", "Error leyendo preferencias.", exception)
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        val language = preferences[KEY_LANGUAGE] ?: "Español"
        val theme = preferences[KEY_THEME] ?: "Automático (Sistema)"
        val allowNotifications = preferences[KEY_ALLOW_NOTIFICATIONS] ?: true
        val fontSize = preferences[KEY_FONT_SIZE] ?: 1 // Default: Mediana (1)

        AppSettings(theme = theme, language = language, fontSize = fontSize)
    }

    // 6. Funciones 'suspend' para ESCRIBIR (¡Actualizadas!)

    suspend fun setLanguage(language: String) {
        settingsDataStore.edit { it[KEY_LANGUAGE] = language }
    }

    suspend fun setTheme(theme: String) {
        settingsDataStore.edit { it[KEY_THEME] = theme }
    }

    suspend fun setAllowNotifications(isEnabled: Boolean) {
        settingsDataStore.edit { it[KEY_ALLOW_NOTIFICATIONS] = isEnabled }
        Log.e("SettingsManager", "El valor de allowNotifications cambió a: $isEnabled")
    }

    suspend fun setFontSize(size: Int) { // <-- ¡AÑADIDO!
        settingsDataStore.edit { it[KEY_FONT_SIZE] = size }
    }
}
