package com.example.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.collection.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.data.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val iconBitmapCache = LruCache<String, ImageBitmap>(256)

    suspend fun getInstalledApps(
        customGamePackages: Set<String>,
        launchCounts: Map<String, Int>
    ): List<AppItem> = withContext(Dispatchers.IO) {
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(
                    launcherIntent,
                    PackageManager.ResolveInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentActivities(launcherIntent, 0)
            }
        } catch (_: Throwable) {
            emptyList()
        }

        val myPackage = context.packageName
        val apps = mutableListOf<AppItem>()

        for (info in resolveInfos) {
            try {
                val actInfo = info.activityInfo ?: continue
                val pkg = actInfo.packageName ?: continue
                // Skip the launcher itself
                if (pkg == myPackage) continue

                val activityName = actInfo.name ?: ""
                val label = try {
                    info.loadLabel(packageManager)?.toString() ?: actInfo.name ?: pkg
                } catch (_: Throwable) {
                    actInfo.name ?: pkg
                }

                val appInfo = actInfo.applicationInfo
                val isSystemGame = if (appInfo != null) isGameApp(appInfo) else false
                val isGame = isSystemGame || customGamePackages.contains(pkg)

                val installTime = try {
                    packageManager.getPackageInfo(pkg, 0).firstInstallTime
                } catch (_: Throwable) {
                    0L
                }

                // Cache or load iconBitmap
                val cachedBitmap = iconBitmapCache.get(pkg)
                val iconBitmap = cachedBitmap ?: run {
                    val drawable = try {
                        info.loadIcon(packageManager)
                    } catch (_: Throwable) {
                        try {
                            packageManager.defaultActivityIcon
                        } catch (_: Throwable) {
                            null
                        }
                    }
                    createSafeImageBitmap(drawable)?.also {
                        iconBitmapCache.put(pkg, it)
                    }
                }

                val count = launchCounts[pkg] ?: 0

                apps.add(
                    AppItem(
                        packageName = pkg,
                        activityName = activityName,
                        label = label,
                        isGame = isGame,
                        installTime = installTime,
                        launchCount = count,
                        iconBitmap = iconBitmap
                    )
                )
            } catch (_: Throwable) {
                // Ignore single app crash and keep going
            }
        }

        apps.sortedBy { it.label.lowercase() }
    }

    private fun createSafeImageBitmap(drawable: Drawable?): ImageBitmap? {
        if (drawable == null) return null
        return try {
            val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth.coerceIn(64, 144) else 96
            val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight.coerceIn(64, 144) else 96
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap.asImageBitmap()
        } catch (_: Throwable) {
            try {
                if (drawable is BitmapDrawable && drawable.bitmap != null && !drawable.bitmap.isRecycled) {
                    val bm = drawable.bitmap
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bm.config == Bitmap.Config.HARDWARE) {
                        bm.copy(Bitmap.Config.ARGB_8888, false)?.asImageBitmap()
                    } else {
                        bm.asImageBitmap()
                    }
                } else {
                    null
                }
            } catch (_: Throwable) {
                null
            }
        }
    }

    private fun isGameApp(appInfo: ApplicationInfo): Boolean {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (appInfo.category == ApplicationInfo.CATEGORY_GAME) {
                    return true
                }
            }
            @Suppress("DEPRECATION")
            if ((appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0) {
                return true
            }
        } catch (_: Throwable) {}
        return false
    }

    fun launchApp(packageName: String, activityName: String? = null): Boolean {
        return try {
            // First try standard launch intent
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            }
            if (launchIntent != null) {
                context.startActivity(launchIntent)
                true
            } else if (!activityName.isNullOrEmpty()) {
                val explicitIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    component = ComponentName(packageName, activityName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                }
                context.startActivity(explicitIntent)
                true
            } else {
                false
            }
        } catch (_: Throwable) {
            false
        }
    }

    fun openAppInfo(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Throwable) {}
    }

    fun requestUninstall(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Throwable) {}
    }

    fun isDefaultLauncher(): Boolean {
        return try {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            val resolveInfo = packageManager.resolveActivity(
                homeIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            resolveInfo?.activityInfo?.packageName == context.packageName
        } catch (_: Throwable) {
            false
        }
    }

    fun openDefaultLauncherSettings() {
        try {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Throwable) {
            try {
                val fallbackIntent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (_: Throwable) {
                try {
                    val generalSettings = Intent(Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(generalSettings)
                } catch (_: Throwable) {}
            }
        }
    }
}
