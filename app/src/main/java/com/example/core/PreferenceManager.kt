// FILE: app/src/main/java/com/example/core/PreferenceManager.kt
package com.example.core

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("maazcraft_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_FIRST_LAUNCH_DONE = "is_first_launch_done"
        private const val KEY_ALLOCATED_RAM_MB = "allocated_ram_mb"
        private const val KEY_JAVA_VERSION = "selected_java_version"
        private const val KEY_DRIVER_ID = "selected_driver_id"
        private const val KEY_RENDER_DISTANCE = "render_distance"
        private const val KEY_GRAPHICS_MODE = "graphics_mode"
        private const val KEY_RESOLUTION_SCALE = "resolution_scale"
        private const val KEY_CONTROL_SCALE = "control_scale"
        private const val KEY_CONTROL_OPACITY = "control_opacity"
        private const val KEY_TOUCH_SENSITIVITY = "touch_sensitivity"
        private const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
        private const val KEY_SELECTED_VERSION = "selected_version"
        private const val KEY_SELECTED_LOADER = "selected_loader"
        private const val KEY_ACCOUNT_USERNAME = "account_username"
        private const val KEY_ACCOUNT_UUID = "account_uuid"
        private const val KEY_ACCOUNT_TYPE = "account_type"
        private const val KEY_JVM_ARGS = "custom_jvm_args"
        private const val KEY_AUTO_OPTIMIZED_ONCE = "auto_optimized_once"
        private const val KEY_GITHUB_OWNER = "github_owner"
        private const val KEY_GITHUB_REPO = "github_repo"
        private const val KEY_GITHUB_TOKEN = "github_pat_token"
        private const val KEY_GITHUB_AUTO_CHECK = "github_auto_check"
    }

    var isFirstLaunchDone: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH_DONE, false)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH_DONE, value).apply()

    var allocatedRamMb: Int
        get() = prefs.getInt(KEY_ALLOCATED_RAM_MB, 2048)
        set(value) = prefs.edit().putInt(KEY_ALLOCATED_RAM_MB, value).apply()

    var selectedJavaVersion: String
        get() = prefs.getString(KEY_JAVA_VERSION, "Auto (Recommended)") ?: "Auto (Recommended)"
        set(value) = prefs.edit().putString(KEY_JAVA_VERSION, value).apply()

    var selectedDriverId: String
        get() = prefs.getString(KEY_DRIVER_ID, "turnip-zink-adreno-610") ?: "turnip-zink-adreno-610"
        set(value) = prefs.edit().putString(KEY_DRIVER_ID, value).apply()

    var renderDistance: Int
        get() = prefs.getInt(KEY_RENDER_DISTANCE, 8)
        set(value) = prefs.edit().putInt(KEY_RENDER_DISTANCE, value).apply()

    var graphicsMode: String
        get() = prefs.getString(KEY_GRAPHICS_MODE, "Fast") ?: "Fast"
        set(value) = prefs.edit().putString(KEY_GRAPHICS_MODE, value).apply()

    var resolutionScale: Int
        get() = prefs.getInt(KEY_RESOLUTION_SCALE, 100) // Percentage: 50% - 125%
        set(value) = prefs.edit().putInt(KEY_RESOLUTION_SCALE, value).apply()

    var controlScale: Float
        get() = prefs.getFloat(KEY_CONTROL_SCALE, 1.25f)
        set(value) = prefs.edit().putFloat(KEY_CONTROL_SCALE, value).apply()

    var controlOpacity: Float
        get() = prefs.getFloat(KEY_CONTROL_OPACITY, 0.85f)
        set(value) = prefs.edit().putFloat(KEY_CONTROL_OPACITY, value).apply()

    var touchSensitivity: Float
        get() = prefs.getFloat(KEY_TOUCH_SENSITIVITY, 1.15f)
        set(value) = prefs.edit().putFloat(KEY_TOUCH_SENSITIVITY, value).apply()

    var hapticFeedback: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC_FEEDBACK, value).apply()

    var selectedVersionId: String
        get() = prefs.getString(KEY_SELECTED_VERSION, "1.21.1") ?: "1.21.1"
        set(value) = prefs.edit().putString(KEY_SELECTED_VERSION, value).apply()

    var selectedModLoader: String
        get() = prefs.getString(KEY_SELECTED_LOADER, "Vanilla") ?: "Vanilla"
        set(value) = prefs.edit().putString(KEY_SELECTED_LOADER, value).apply()

    var accountUsername: String
        get() = prefs.getString(KEY_ACCOUNT_USERNAME, "MaazPlayer") ?: "MaazPlayer"
        set(value) = prefs.edit().putString(KEY_ACCOUNT_USERNAME, value).apply()

    var accountUuid: String
        get() = prefs.getString(KEY_ACCOUNT_UUID, "00000000-0000-0000-0000-000000000001") ?: "00000000-0000-0000-0000-000000000001"
        set(value) = prefs.edit().putString(KEY_ACCOUNT_UUID, value).apply()

    var accountType: String
        get() = prefs.getString(KEY_ACCOUNT_TYPE, "Offline") ?: "Offline"
        set(value) = prefs.edit().putString(KEY_ACCOUNT_TYPE, value).apply()

    var customJvmArgs: String
        get() = prefs.getString(KEY_JVM_ARGS, "-XX:+UseG1GC -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M")
            ?: "-XX:+UseG1GC -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M"
        set(value) = prefs.edit().putString(KEY_JVM_ARGS, value).apply()

    var githubOwner: String
        get() = prefs.getString(KEY_GITHUB_OWNER, "monusonummba") ?: "monusonummba"
        set(value) = prefs.edit().putString(KEY_GITHUB_OWNER, value).apply()

    var githubRepo: String
        get() = prefs.getString(KEY_GITHUB_REPO, "MaazCraft-Launcher-V3") ?: "MaazCraft-Launcher-V3"
        set(value) = prefs.edit().putString(KEY_GITHUB_REPO, value).apply()

    var githubPatToken: String
        get() = prefs.getString(KEY_GITHUB_TOKEN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_GITHUB_TOKEN, value).apply()

    var githubAutoCheck: Boolean
        get() = prefs.getBoolean(KEY_GITHUB_AUTO_CHECK, true)
        set(value) = prefs.edit().putBoolean(KEY_GITHUB_AUTO_CHECK, value).apply()

    fun resetOptimizationFlags() {
        isFirstLaunchDone = false
    }
}
