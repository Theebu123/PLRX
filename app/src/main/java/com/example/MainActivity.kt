package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.AppContextMenu
import com.example.ui.components.PLXBackground
import com.example.ui.screens.AppDrawerScreen
import com.example.ui.screens.GamingHubScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ACCENT_THEMES
import com.example.ui.theme.PLX132Theme
import com.example.ui.viewmodel.LauncherScreen
import com.example.ui.viewmodel.LauncherViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels {
        LauncherViewModel.provideFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val telemetry by viewModel.telemetry.collectAsState()
            val pinnedApps by viewModel.pinnedHomeApps.collectAsState()
            val dockApps by viewModel.dockApps.collectAsState()
            val filteredApps by viewModel.filteredApps.collectAsState()
            val allApps by viewModel.installedApps.collectAsState()
            val gamesList by viewModel.gamesList.collectAsState()
            val searchQuery by viewModel.searchQuery.collectAsState()
            val activeCategory by viewModel.activeCategory.collectAsState()
            val selectedAppForMenu by viewModel.selectedAppForMenu.collectAsState()
            val isDefaultLauncher by viewModel.isDefaultLauncher.collectAsState()

            val accentTheme = ACCENT_THEMES.getOrElse(settings.accentColorIndex) { ACCENT_THEMES[0] }
            val accentColor = accentTheme.primary

            // Package install/uninstall broadcast listener
            val context = LocalContext.current
            DisposableEffect(Unit) {
                val packageReceiver = object : BroadcastReceiver() {
                    override fun onReceive(c: Context?, intent: Intent?) {
                        viewModel.refreshInstalledApps()
                    }
                }
                val filter = IntentFilter().apply {
                    addAction(Intent.ACTION_PACKAGE_ADDED)
                    addAction(Intent.ACTION_PACKAGE_REMOVED)
                    addAction(Intent.ACTION_PACKAGE_REPLACED)
                    addDataScheme("package")
                }
                var isRegistered = false
                try {
                    ContextCompat.registerReceiver(
                        context,
                        packageReceiver,
                        filter,
                        ContextCompat.RECEIVER_EXPORTED
                    )
                    isRegistered = true
                } catch (_: Throwable) {
                    try {
                        context.registerReceiver(packageReceiver, filter)
                        isRegistered = true
                    } catch (_: Throwable) {}
                }
                onDispose {
                    if (isRegistered) {
                        try {
                            context.unregisterReceiver(packageReceiver)
                        } catch (_: Throwable) {}
                    }
                }
            }

            LaunchedEffect(Unit) {
                viewModel.checkDefaultLauncher()
            }

            androidx.activity.compose.BackHandler(enabled = true) {
                if (currentScreen != LauncherScreen.HOME) {
                    viewModel.navigateTo(LauncherScreen.HOME)
                    viewModel.setSearchQuery("")
                }
            }

            PLX132Theme(accentColorIndex = settings.accentColorIndex) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Futuristic Background
                    PLXBackground(
                        wallpaperIndex = settings.wallpaperIndex,
                        accentColor = accentColor
                    )

                    // Screen Navigation with clean animations
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            when {
                                targetState == LauncherScreen.APP_DRAWER ->
                                    (slideInVertically { height -> height } + fadeIn())
                                        .togetherWith(slideOutVertically { height -> -height / 4 } + fadeOut())
                                targetState == LauncherScreen.GAMING_HUB ->
                                    (slideInHorizontally { width -> width } + fadeIn())
                                        .togetherWith(slideOutHorizontally { width -> -width / 4 } + fadeOut())
                                targetState == LauncherScreen.SETTINGS ->
                                    (slideInHorizontally { width -> width } + fadeIn())
                                        .togetherWith(slideOutHorizontally { width -> -width / 4 } + fadeOut())
                                else ->
                                    (slideInVertically { height -> -height / 4 } + fadeIn())
                                        .togetherWith(slideOutVertically { height -> height } + fadeOut())
                            }
                        },
                        label = "LauncherScreenTransition"
                    ) { screen ->
                        when (screen) {
                            LauncherScreen.HOME -> {
                                HomeScreen(
                                    pinnedApps = pinnedApps,
                                    dockApps = dockApps,
                                    telemetry = telemetry,
                                    settings = settings,
                                    accentColor = accentColor,
                                    isDefaultLauncher = isDefaultLauncher,
                                    onAppClick = { app -> viewModel.launchApp(app) },
                                    onAppLongClick = { app -> viewModel.setSelectedAppForMenu(app) },
                                    onOpenDrawer = { viewModel.navigateTo(LauncherScreen.APP_DRAWER) },
                                    onOpenGamingHub = { viewModel.navigateTo(LauncherScreen.GAMING_HUB) },
                                    onOpenSettings = { viewModel.navigateTo(LauncherScreen.SETTINGS) },
                                    onSetDefaultLauncher = { viewModel.openDefaultLauncherSettings() }
                                )
                            }
                            LauncherScreen.APP_DRAWER -> {
                                AppDrawerScreen(
                                    apps = filteredApps,
                                    settings = settings,
                                    accentColor = accentColor,
                                    searchQuery = searchQuery,
                                    activeCategory = activeCategory,
                                    onSearchQueryChange = { q -> viewModel.setSearchQuery(q) },
                                    onCategoryChange = { cat -> viewModel.setActiveCategory(cat) },
                                    onSortOrderChange = { sort -> viewModel.setSortOrder(sort) },
                                    onAppClick = { app -> viewModel.launchApp(app) },
                                    onAppLongClick = { app -> viewModel.setSelectedAppForMenu(app) },
                                    onOpenSupportConsole = { viewModel.navigateTo(LauncherScreen.SETTINGS) },
                                    onBackToHome = { viewModel.navigateTo(LauncherScreen.HOME) }
                                )
                            }
                            LauncherScreen.GAMING_HUB -> {
                                GamingHubScreen(
                                    games = gamesList,
                                    allApps = allApps,
                                    telemetry = telemetry,
                                    accentColor = accentColor,
                                    onLaunchGame = { game -> viewModel.launchApp(game) },
                                    onToggleCustomGame = { pkg -> viewModel.toggleCustomGame(pkg) },
                                    onBackToHome = { viewModel.navigateTo(LauncherScreen.HOME) }
                                )
                            }
                            LauncherScreen.SETTINGS -> {
                                SettingsScreen(
                                    settings = settings,
                                    allApps = allApps,
                                    accentColor = accentColor,
                                    isDefaultLauncher = isDefaultLauncher,
                                    onSetDefaultLauncher = { viewModel.openDefaultLauncherSettings() },
                                    onAccentColorChange = { idx -> viewModel.setAccentColor(idx) },
                                    onGridColumnsChange = { cols -> viewModel.setGridColumns(cols) },
                                    onIconScaleChange = { sc -> viewModel.setIconScale(sc) },
                                    onShowLabelsChange = { s -> viewModel.setShowLabels(s) },
                                    onShowClockChange = { s -> viewModel.setShowClock(s) },
                                    onShowDateChange = { s -> viewModel.setShowDate(s) },
                                    onShowBatteryChange = { s -> viewModel.setShowBattery(s) },
                                    onShowNetworkChange = { s -> viewModel.setShowNetwork(s) },
                                    onShowTelemetryChange = { s -> viewModel.setShowPerformancePanel(s) },
                                    onWallpaperChange = { w -> viewModel.setWallpaperIndex(w) },
                                    onAutoDetectGamesChange = { a -> viewModel.setAutoDetectGames(a) },
                                    onToggleHideApp = { pkg -> viewModel.toggleHideApp(pkg) },
                                    onUnhideAllApps = { viewModel.unhideAllApps() },
                                    onOpenDeveloperSettings = { viewModel.openDeveloperSettings(this@MainActivity) },
                                    onLaunchApp = { app -> viewModel.launchApp(app) },
                                    onBackToHome = { viewModel.navigateTo(LauncherScreen.HOME) }
                                )
                            }
                        }
                    }

                    // Context Menu for App
                    selectedAppForMenu?.let { app ->
                        AppItemMenuDialog(
                            app = app,
                            accentColor = accentColor,
                            settings = settings,
                            viewModel = viewModel,
                            onDismiss = { viewModel.setSelectedAppForMenu(null) }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkDefaultLauncher()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Home button press when already active: return to home screen
        viewModel.navigateTo(LauncherScreen.HOME)
        viewModel.setSearchQuery("")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppItemMenuDialog(
    app: com.example.data.model.AppItem,
    accentColor: androidx.compose.ui.graphics.Color,
    settings: com.example.data.model.LauncherSettings,
    viewModel: LauncherViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    val isPinnedHome = settings.pinnedHomePackages.contains(app.packageName)
    val isPinnedDock = settings.pinnedDockPackages.contains(app.packageName)
    val isHidden = settings.hiddenPackages.contains(app.packageName)

    AppContextMenu(
        app = app,
        isPinnedToHome = isPinnedHome,
        isPinnedToDock = isPinnedDock,
        isHidden = isHidden,
        accentColor = accentColor,
        sheetState = sheetState,
        onDismiss = onDismiss,
        onLaunch = {
            viewModel.launchApp(app)
            onDismiss()
        },
        onTogglePinHome = {
            viewModel.togglePinToHome(app.packageName)
            onDismiss()
        },
        onTogglePinDock = {
            viewModel.togglePinToDock(app.packageName)
            onDismiss()
        },
        onToggleGame = {
            viewModel.toggleCustomGame(app.packageName)
            onDismiss()
        },
        onToggleHide = {
            viewModel.toggleHideApp(app.packageName)
            onDismiss()
        },
        onAppInfo = {
            viewModel.openAppInfo(app)
            onDismiss()
        },
        onUninstall = {
            viewModel.requestUninstall(app)
            onDismiss()
        }
    )
}
