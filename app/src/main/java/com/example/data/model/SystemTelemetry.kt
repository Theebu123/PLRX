package com.example.data.model

enum class NetworkType {
    WIFI,
    CELLULAR,
    ETHERNET,
    OFFLINE
}

data class SystemTelemetry(
    // Real RAM (ActivityManager.getMemoryInfo)
    val usedRamBytes: Long = 0L,
    val totalRamBytes: Long = 0L,
    val usedRamPercent: Int = 0,
    val isLowRam: Boolean = false,

    // Real Battery (BatteryManager / Intent.ACTION_BATTERY_CHANGED)
    val batteryPercent: Int = 100,
    val isCharging: Boolean = false,
    val chargeType: String = "Battery",
    val temperatureCelsius: Float = 28.0f,
    val voltageMv: Int = 4000,
    val batteryHealth: String = "Good",

    // Real Network (ConnectivityManager / NetworkCapabilities)
    val networkType: NetworkType = NetworkType.OFFLINE,
    val isOnline: Boolean = false,
    val linkSpeedMbps: Int = 0,

    // Real Display (Display.mode.refreshRate)
    val refreshRateHz: Int = 60,

    // Real Internal Storage (StatFs)
    val freeStorageGb: Float = 0f,
    val totalStorageGb: Float = 0f
)
