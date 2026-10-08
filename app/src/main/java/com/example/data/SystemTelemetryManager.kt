package com.example.data

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.example.data.model.NetworkType
import com.example.data.model.SystemTelemetry
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SystemTelemetryManager(private val context: Context) {

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager

    fun getTelemetrySnapshot(): SystemTelemetry {
        // 1. RAM info
        var totalRam = 0L
        var availRam = 0L
        var usedRam = 0L
        var usedRamPercent = 0
        var isLowRam = false

        try {
            val memInfo = ActivityManager.MemoryInfo()
            activityManager?.getMemoryInfo(memInfo)
            totalRam = memInfo.totalMem
            availRam = memInfo.availMem
            usedRam = totalRam - availRam
            usedRamPercent = if (totalRam > 0) {
                ((usedRam.toDouble() / totalRam.toDouble()) * 100).toInt().coerceIn(0, 100)
            } else 0
            isLowRam = memInfo.lowMemory
        } catch (_: Throwable) {}

        // 2. Battery info (sticky broadcast)
        var batteryPct = 100
        var isCharging = false
        var chargeType = "Battery"
        var tempC = 28.0f
        var voltage = 4000
        var health = "Normal"

        try {
            val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.registerReceiver(null, batteryFilter)
            } else {
                context.registerReceiver(null, batteryFilter)
            }

            if (batteryStatus != null) {
                val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 100
                val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
                val chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                chargeType = when (chargePlug) {
                    BatteryManager.BATTERY_PLUGGED_AC -> "AC"
                    BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                    BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                    else -> if (isCharging) "Charging" else "Battery"
                }
                val tempRaw = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
                tempC = if (tempRaw > 0) (tempRaw / 10.0f) else 28.0f
                voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                health = when (batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
                    BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                    BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                    BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                    else -> "Normal"
                }
            }
        } catch (_: Throwable) {}

        // 3. Network info
        var isOnline = false
        var netType = NetworkType.OFFLINE
        var linkSpeed = 0

        try {
            val activeNetwork = connectivityManager?.activeNetwork
            val caps = connectivityManager?.getNetworkCapabilities(activeNetwork)
            isOnline = caps != null && (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
            netType = when {
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> NetworkType.WIFI
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> NetworkType.CELLULAR
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> NetworkType.ETHERNET
                else -> NetworkType.OFFLINE
            }
            linkSpeed = (caps?.linkDownstreamBandwidthKbps ?: 0) / 1000
        } catch (_: Throwable) {}

        // 4. Display refresh rate (Must never throw UnsupportedOperationException on ApplicationContext)
        val refreshRate = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // context.display can throw UnsupportedOperationException if called on an Application context
                try {
                    context.display?.mode?.refreshRate?.toInt() ?: 60
                } catch (_: Throwable) {
                    @Suppress("DEPRECATION")
                    windowManager?.defaultDisplay?.mode?.refreshRate?.toInt() ?: 60
                }
            } else {
                @Suppress("DEPRECATION")
                windowManager?.defaultDisplay?.mode?.refreshRate?.toInt() ?: 60
            }
        } catch (_: Throwable) {
            60
        }

        // 5. Storage info
        val (freeStorage, totalStorage) = try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availBlocks = stat.availableBlocksLong
            val totalBytes = totalBlocks * blockSize
            val freeBytes = availBlocks * blockSize
            val totalG = totalBytes / (1024f * 1024f * 1024f)
            val freeG = freeBytes / (1024f * 1024f * 1024f)
            Pair(freeG, totalG)
        } catch (_: Throwable) {
            Pair(0f, 0f)
        }

        return SystemTelemetry(
            usedRamBytes = usedRam,
            totalRamBytes = totalRam,
            usedRamPercent = usedRamPercent,
            isLowRam = isLowRam,
            batteryPercent = batteryPct,
            isCharging = isCharging,
            chargeType = chargeType,
            temperatureCelsius = tempC,
            voltageMv = voltage,
            batteryHealth = health,
            networkType = netType,
            isOnline = isOnline,
            linkSpeedMbps = linkSpeed,
            refreshRateHz = refreshRate,
            freeStorageGb = freeStorage,
            totalStorageGb = totalStorage
        )
    }

    val telemetryFlow: Flow<SystemTelemetry> = callbackFlow {
        // Emit initial
        trySend(getTelemetrySnapshot())

        // Battery receiver
        val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                trySend(getTelemetrySnapshot())
            }
        }
        var isBatteryRegistered = false
        try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            ContextCompat.registerReceiver(
                context,
                batteryReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            isBatteryRegistered = true
        } catch (_: Throwable) {
            try {
                context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                isBatteryRegistered = true
            } catch (_: Throwable) {}
        }

        // Network callback
        var isNetworkRegistered = false
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(getTelemetrySnapshot())
            }

            override fun onLost(network: Network) {
                trySend(getTelemetrySnapshot())
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                trySend(getTelemetrySnapshot())
            }
        }
        try {
            connectivityManager?.registerDefaultNetworkCallback(networkCallback)
            isNetworkRegistered = true
        } catch (_: Throwable) {}

        // Periodic light refresh every 3 seconds for RAM and thermal readings
        val job = launch {
            while (isActive) {
                delay(3000L)
                trySend(getTelemetrySnapshot())
            }
        }

        awaitClose {
            job.cancel()
            if (isBatteryRegistered) {
                try {
                    context.unregisterReceiver(batteryReceiver)
                } catch (_: Throwable) {}
            }
            if (isNetworkRegistered) {
                try {
                    connectivityManager?.unregisterNetworkCallback(networkCallback)
                } catch (_: Throwable) {}
            }
        }
    }
}
