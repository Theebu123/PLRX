package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppSortOrder
import com.example.data.model.LauncherSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.launcherDataStore: DataStore<Preferences> by preferencesDataStore(name = "plx132_launcher_prefs")

class LauncherPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val ACCENT_COLOR_INDEX = intPreferencesKey("accent_color_index")
        val GRID_COLUMNS = intPreferencesKey("grid_columns")
        val ICON_SCALE = floatPreferencesKey("icon_scale")
        val SHOW_LABELS = booleanPreferencesKey("show_labels")
        val SHOW_CLOCK = booleanPreferencesKey("show_clock")
        val SHOW_DATE = booleanPreferencesKey("show_date")
        val SHOW_BATTERY = booleanPreferencesKey("show_battery")
        val SHOW_NETWORK = booleanPreferencesKey("show_network")
        val SHOW_PERFORMANCE_PANEL = booleanPreferencesKey("show_performance_panel")
        val WALLPAPER_INDEX = intPreferencesKey("wallpaper_index")
        val APP_SORT_ORDER = stringPreferencesKey("app_sort_order")
        val PINNED_DOCK_PACKAGES = stringSetPreferencesKey("pinned_dock_packages")
        val PINNED_HOME_PACKAGES = stringSetPreferencesKey("pinned_home_packages")
        val CUSTOM_GAME_PACKAGES = stringSetPreferencesKey("custom_game_packages")
        val HIDDEN_PACKAGES = stringSetPreferencesKey("hidden_packages")
        val AUTO_DETECT_GAMES = booleanPreferencesKey("auto_detect_games")
        val LAUNCH_COUNTS = stringPreferencesKey("launch_counts") // format: "pkg:count|pkg:count"
    }

    val settingsFlow: Flow<LauncherSettings> = context.launcherDataStore.data.map { prefs ->
        val sortName = prefs[PreferencesKeys.APP_SORT_ORDER] ?: AppSortOrder.ALPHABETICAL_ASC.name
        val sortOrder = try {
            AppSortOrder.valueOf(sortName)
        } catch (_: Exception) {
            AppSortOrder.ALPHABETICAL_ASC
        }

        LauncherSettings(
            accentColorIndex = prefs[PreferencesKeys.ACCENT_COLOR_INDEX] ?: 0,
            gridColumns = prefs[PreferencesKeys.GRID_COLUMNS] ?: 4,
            iconScale = prefs[PreferencesKeys.ICON_SCALE] ?: 1.0f,
            showLabels = prefs[PreferencesKeys.SHOW_LABELS] ?: true,
            showClock = prefs[PreferencesKeys.SHOW_CLOCK] ?: true,
            showDate = prefs[PreferencesKeys.SHOW_DATE] ?: true,
            showBattery = prefs[PreferencesKeys.SHOW_BATTERY] ?: true,
            showNetwork = prefs[PreferencesKeys.SHOW_NETWORK] ?: true,
            showPerformancePanel = prefs[PreferencesKeys.SHOW_PERFORMANCE_PANEL] ?: true,
            wallpaperIndex = prefs[PreferencesKeys.WALLPAPER_INDEX] ?: 0,
            appSortOrder = sortOrder,
            pinnedDockPackages = prefs[PreferencesKeys.PINNED_DOCK_PACKAGES]?.toList() ?: emptyList(),
            pinnedHomePackages = prefs[PreferencesKeys.PINNED_HOME_PACKAGES]?.toList() ?: emptyList(),
            customGamePackages = prefs[PreferencesKeys.CUSTOM_GAME_PACKAGES] ?: emptySet(),
            hiddenPackages = prefs[PreferencesKeys.HIDDEN_PACKAGES] ?: emptySet(),
            autoDetectGames = prefs[PreferencesKeys.AUTO_DETECT_GAMES] ?: true
        )
    }

    val launchCountsFlow: Flow<Map<String, Int>> = context.launcherDataStore.data.map { prefs ->
        val raw = prefs[PreferencesKeys.LAUNCH_COUNTS] ?: ""
        parseLaunchCounts(raw)
    }

    private fun parseLaunchCounts(raw: String): Map<String, Int> {
        if (raw.isBlank()) return emptyMap()
        val result = mutableMapOf<String, Int>()
        raw.split("|").forEach { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                val pkg = parts[0]
                val count = parts[1].toIntOrNull() ?: 0
                if (pkg.isNotEmpty() && count > 0) {
                    result[pkg] = count
                }
            }
        }
        return result
    }

    suspend fun recordAppLaunch(packageName: String) {
        context.launcherDataStore.edit { prefs ->
            val raw = prefs[PreferencesKeys.LAUNCH_COUNTS] ?: ""
            val counts = parseLaunchCounts(raw).toMutableMap()
            counts[packageName] = (counts[packageName] ?: 0) + 1
            val serialized = counts.entries.joinToString("|") { "${it.key}:${it.value}" }
            prefs[PreferencesKeys.LAUNCH_COUNTS] = serialized
        }
    }

    suspend fun setAccentColorIndex(index: Int) {
        context.launcherDataStore.edit { it[PreferencesKeys.ACCENT_COLOR_INDEX] = index }
    }

    suspend fun setGridColumns(columns: Int) {
        context.launcherDataStore.edit { it[PreferencesKeys.GRID_COLUMNS] = columns }
    }

    suspend fun setIconScale(scale: Float) {
        context.launcherDataStore.edit { it[PreferencesKeys.ICON_SCALE] = scale }
    }

    suspend fun setShowLabels(show: Boolean) {
        context.launcherDataStore.edit { it[PreferencesKeys.SHOW_LABELS] = show }
    }

    suspend fun setShowClock(show: Boolean) {
        context.launcherDataStore.edit { it[PreferencesKeys.SHOW_CLOCK] = show }
    }

    suspend fun setShowDate(show: Boolean) {
        context.launcherDataStore.edit { it[PreferencesKeys.SHOW_DATE] = show }
    }

    suspend fun setShowBattery(show: Boolean) {
        context.launcherDataStore.edit { it[PreferencesKeys.SHOW_BATTERY] = show }
    }

    suspend fun setShowNetwork(show: Boolean) {
        context.launcherDataStore.edit { it[PreferencesKeys.SHOW_NETWORK] = show }
    }

    suspend fun setShowPerformancePanel(show: Boolean) {
        context.launcherDataStore.edit { it[PreferencesKeys.SHOW_PERFORMANCE_PANEL] = show }
    }

    suspend fun setWallpaperIndex(index: Int) {
        context.launcherDataStore.edit { it[PreferencesKeys.WALLPAPER_INDEX] = index }
    }

    suspend fun setAppSortOrder(order: AppSortOrder) {
        context.launcherDataStore.edit { it[PreferencesKeys.APP_SORT_ORDER] = order.name }
    }

    suspend fun setPinnedDockPackages(packages: List<String>) {
        context.launcherDataStore.edit { it[PreferencesKeys.PINNED_DOCK_PACKAGES] = packages.toSet() }
    }

    suspend fun setPinnedHomePackages(packages: List<String>) {
        context.launcherDataStore.edit { it[PreferencesKeys.PINNED_HOME_PACKAGES] = packages.toSet() }
    }

    suspend fun toggleCustomGamePackage(packageName: String) {
        context.launcherDataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.CUSTOM_GAME_PACKAGES] ?: emptySet()
            prefs[PreferencesKeys.CUSTOM_GAME_PACKAGES] = if (current.contains(packageName)) {
                current - packageName
            } else {
                current + packageName
            }
        }
    }

    suspend fun toggleHiddenPackage(packageName: String) {
        context.launcherDataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.HIDDEN_PACKAGES] ?: emptySet()
            prefs[PreferencesKeys.HIDDEN_PACKAGES] = if (current.contains(packageName)) {
                current - packageName
            } else {
                current + packageName
            }
        }
    }

    suspend fun unhideAllPackages() {
        context.launcherDataStore.edit { prefs ->
            prefs[PreferencesKeys.HIDDEN_PACKAGES] = emptySet()
        }
    }

    suspend fun setHiddenPackages(packages: Set<String>) {
        context.launcherDataStore.edit { prefs ->
            prefs[PreferencesKeys.HIDDEN_PACKAGES] = packages
        }
    }

    suspend fun setAutoDetectGames(enable: Boolean) {
        context.launcherDataStore.edit { it[PreferencesKeys.AUTO_DETECT_GAMES] = enable }
    }
}
