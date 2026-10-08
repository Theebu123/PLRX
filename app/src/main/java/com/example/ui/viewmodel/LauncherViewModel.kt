package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.LauncherPreferencesRepository
import com.example.data.SystemTelemetryManager
import com.example.data.model.AppItem
import com.example.data.model.AppSortOrder
import com.example.data.model.DrawerCategory
import com.example.data.model.LauncherSettings
import com.example.data.model.SystemTelemetry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LauncherScreen {
    HOME,
    APP_DRAWER,
    GAMING_HUB,
    SETTINGS
}

class LauncherViewModel(
    private val appRepository: AppRepository,
    private val preferencesRepository: LauncherPreferencesRepository,
    private val telemetryManager: SystemTelemetryManager
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(LauncherScreen.HOME)
    val currentScreen: StateFlow<LauncherScreen> = _currentScreen.asStateFlow()

    private val _installedApps = MutableStateFlow<List<AppItem>>(emptyList())
    val installedApps: StateFlow<List<AppItem>> = _installedApps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeCategory = MutableStateFlow(DrawerCategory.ALL)
    val activeCategory: StateFlow<DrawerCategory> = _activeCategory.asStateFlow()

    private val _selectedAppForMenu = MutableStateFlow<AppItem?>(null)
    val selectedAppForMenu: StateFlow<AppItem?> = _selectedAppForMenu.asStateFlow()

    private val _isDefaultLauncher = MutableStateFlow(false)
    val isDefaultLauncher: StateFlow<Boolean> = _isDefaultLauncher.asStateFlow()

    val settings: StateFlow<LauncherSettings> = preferencesRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, LauncherSettings())

    val telemetry: StateFlow<SystemTelemetry> = telemetryManager.telemetryFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, telemetryManager.getTelemetrySnapshot())

    // Filtered & sorted apps for drawer (excluding hidden packages)
    val filteredApps: StateFlow<List<AppItem>> = combine(
        _installedApps,
        _searchQuery,
        _activeCategory,
        settings
    ) { apps, query, category, curSettings ->
        var result = apps.filterNot { curSettings.hiddenPackages.contains(it.packageName) }

        // Category filter
        result = when (category) {
            DrawerCategory.ALL -> result
            DrawerCategory.GAMES -> result.filter { it.isGame || curSettings.customGamePackages.contains(it.packageName) }
            DrawerCategory.FREQUENT -> result.filter { it.launchCount > 0 }.sortedByDescending { it.launchCount }
        }

        // Query filter
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter {
                it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q)
            }
        }

        // Sorting
        when (curSettings.appSortOrder) {
            AppSortOrder.ALPHABETICAL_ASC -> result.sortedBy { it.label.lowercase() }
            AppSortOrder.ALPHABETICAL_DESC -> result.sortedByDescending { it.label.lowercase() }
            AppSortOrder.MOST_USED -> result.sortedByDescending { it.launchCount }
            AppSortOrder.RECENTLY_INSTALLED -> result.sortedByDescending { it.installTime }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Pinned Home apps (excluding hidden packages)
    val pinnedHomeApps: StateFlow<List<AppItem>> = combine(
        _installedApps,
        settings
    ) { apps, curSettings ->
        val visibleApps = apps.filterNot { curSettings.hiddenPackages.contains(it.packageName) }
        val map = visibleApps.associateBy { it.packageName }
        if (curSettings.pinnedHomePackages.isNotEmpty()) {
            curSettings.pinnedHomePackages.mapNotNull { map[it] }
        } else {
            // Default: pick first 8 apps if none explicitly pinned
            visibleApps.take(8)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Dock apps (excluding hidden packages, up to 4 apps)
    val dockApps: StateFlow<List<AppItem>> = combine(
        _installedApps,
        settings
    ) { apps, curSettings ->
        val visibleApps = apps.filterNot { curSettings.hiddenPackages.contains(it.packageName) }
        val map = visibleApps.associateBy { it.packageName }
        if (curSettings.pinnedDockPackages.isNotEmpty()) {
            curSettings.pinnedDockPackages.mapNotNull { map[it] }.take(4)
        } else {
            // Smart defaults: prioritize common apps if installed
            val preferredPackages = listOf(
                "com.google.android.dialer", "com.android.dialer",
                "com.google.android.apps.messaging", "com.android.mms",
                "com.android.chrome", "org.chromium.chrome",
                "com.google.android.GoogleCamera", "com.android.camera2"
            )
            val foundDefaults = preferredPackages.mapNotNull { map[it] }.distinctBy { it.packageName }
            if (foundDefaults.isNotEmpty()) {
                (foundDefaults + visibleApps.filterNot { foundDefaults.contains(it) }).take(4)
            } else {
                visibleApps.take(4)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Games list (excluding hidden packages)
    val gamesList: StateFlow<List<AppItem>> = combine(
        _installedApps,
        settings
    ) { apps, curSettings ->
        apps.filterNot { curSettings.hiddenPackages.contains(it.packageName) }
            .filter { it.isGame || curSettings.customGamePackages.contains(it.packageName) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Dedicated Hidden apps list for management in Developer Options: Root ADB bootloader(flash OS)
    val hiddenAppsList: StateFlow<List<AppItem>> = combine(
        _installedApps,
        settings
    ) { apps, curSettings ->
        apps.filter { curSettings.hiddenPackages.contains(it.packageName) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        refreshInstalledApps()
        checkDefaultLauncher()
    }

    fun refreshInstalledApps() {
        viewModelScope.launch {
            try {
                val curSettings = settings.value
                val counts = try {
                    preferencesRepository.launchCountsFlow.first()
                } catch (_: Throwable) {
                    emptyMap()
                }
                val apps = appRepository.getInstalledApps(
                    customGamePackages = curSettings.customGamePackages,
                    launchCounts = counts
                )
                _installedApps.value = apps
            } catch (_: Throwable) {}
        }
    }

    fun checkDefaultLauncher() {
        _isDefaultLauncher.value = appRepository.isDefaultLauncher()
    }

    fun navigateTo(screen: LauncherScreen) {
        _currentScreen.value = screen
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setActiveCategory(category: DrawerCategory) {
        _activeCategory.value = category
    }

    fun setSelectedAppForMenu(app: AppItem?) {
        _selectedAppForMenu.value = app
    }

    fun launchApp(app: AppItem) {
        viewModelScope.launch {
            preferencesRepository.recordAppLaunch(app.packageName)
            appRepository.launchApp(app.packageName, app.activityName)
        }
    }

    fun openAppInfo(app: AppItem) {
        appRepository.openAppInfo(app.packageName)
    }

    fun requestUninstall(app: AppItem) {
        appRepository.requestUninstall(app.packageName)
    }

    fun openDefaultLauncherSettings() {
        appRepository.openDefaultLauncherSettings()
    }

    fun togglePinToHome(packageName: String) {
        viewModelScope.launch {
            val current = settings.value.pinnedHomePackages
            val updated = if (current.contains(packageName)) {
                current - packageName
            } else {
                current + packageName
            }
            preferencesRepository.setPinnedHomePackages(updated)
        }
    }

    fun togglePinToDock(packageName: String) {
        viewModelScope.launch {
            val current = settings.value.pinnedDockPackages
            val updated = if (current.contains(packageName)) {
                current - packageName
            } else {
                (current + packageName).take(4)
            }
            preferencesRepository.setPinnedDockPackages(updated)
        }
    }

    fun toggleCustomGame(packageName: String) {
        viewModelScope.launch {
            preferencesRepository.toggleCustomGamePackage(packageName)
        }
    }

    fun setAccentColor(index: Int) {
        viewModelScope.launch { preferencesRepository.setAccentColorIndex(index) }
    }

    fun setGridColumns(cols: Int) {
        viewModelScope.launch { preferencesRepository.setGridColumns(cols) }
    }

    fun setIconScale(scale: Float) {
        viewModelScope.launch { preferencesRepository.setIconScale(scale) }
    }

    fun setShowLabels(show: Boolean) {
        viewModelScope.launch { preferencesRepository.setShowLabels(show) }
    }

    fun setShowClock(show: Boolean) {
        viewModelScope.launch { preferencesRepository.setShowClock(show) }
    }

    fun setShowDate(show: Boolean) {
        viewModelScope.launch { preferencesRepository.setShowDate(show) }
    }

    fun setShowBattery(show: Boolean) {
        viewModelScope.launch { preferencesRepository.setShowBattery(show) }
    }

    fun setShowNetwork(show: Boolean) {
        viewModelScope.launch { preferencesRepository.setShowNetwork(show) }
    }

    fun setShowPerformancePanel(show: Boolean) {
        viewModelScope.launch { preferencesRepository.setShowPerformancePanel(show) }
    }

    fun setWallpaperIndex(index: Int) {
        viewModelScope.launch { preferencesRepository.setWallpaperIndex(index) }
    }

    fun setSortOrder(order: AppSortOrder) {
        viewModelScope.launch { preferencesRepository.setAppSortOrder(order) }
    }

    fun setAutoDetectGames(auto: Boolean) {
        viewModelScope.launch { preferencesRepository.setAutoDetectGames(auto) }
    }

    fun toggleHideApp(packageName: String) {
        viewModelScope.launch {
            preferencesRepository.toggleHiddenPackage(packageName)
        }
    }

    fun unhideAllApps() {
        viewModelScope.launch {
            preferencesRepository.unhideAllPackages()
        }
    }

    fun setHiddenPackages(packages: Set<String>) {
        viewModelScope.launch {
            preferencesRepository.setHiddenPackages(packages)
        }
    }

    fun openDeveloperSettings(context: Context) {
        try {
            val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Throwable) {
            try {
                val settingsIntent = android.content.Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(settingsIntent)
            } catch (_: Throwable) {}
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val appRepo = AppRepository(context.applicationContext)
                    val prefsRepo = LauncherPreferencesRepository(context.applicationContext)
                    val telemMgr = SystemTelemetryManager(context.applicationContext)
                    return LauncherViewModel(appRepo, prefsRepo, telemMgr) as T
                }
            }
    }
}
