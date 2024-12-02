package com.rajmani7584.payloaddumper

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.dataStore by preferencesDataStore(name = "settings")

@Suppress("PrivatePropertyName")
class SettingDataStore(private val context: Context) {

    private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
    private val TRUE_BLACK_KEY = booleanPreferencesKey("true_black")
    private val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
    private val CONCURRENCY_KEY = intPreferencesKey("concurrency")
    private val LIST_VIEW_KEY = booleanPreferencesKey("list_view")

    // Save settings
    suspend fun saveTrueBlack(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TRUE_BLACK_KEY] = enabled
        }
    }
    suspend fun saveDarkTheme(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_THEME_KEY] = enabled
        }
    }

    suspend fun saveDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_KEY] = enabled
        }
    }

    suspend fun saveConcurrency(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[CONCURRENCY_KEY] = value
        }
    }

    suspend fun saveListView(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[LIST_VIEW_KEY] = enabled
        }
    }

    // Read settings
    val darkTheme: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_THEME_KEY] == true
    }

    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DYNAMIC_COLOR_KEY] == true
    }

    val concurrency: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[CONCURRENCY_KEY] ?: 4
    }

    val listView: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[LIST_VIEW_KEY] == true
    }

    val trueBlack: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[TRUE_BLACK_KEY] == true
    }
}