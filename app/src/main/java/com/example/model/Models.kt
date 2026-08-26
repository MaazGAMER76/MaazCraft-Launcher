// FILE: app/src/main/java/com/example/model/Models.kt
package com.example.model

/**
 * Hardware profile detected on device.
 */
data class DeviceProfile(
    val cpuModel: String,
    val socName: String,
    val cpuCores: Int,
    val cpuArch: String,
    val totalRamMb: Long,
    val availableRamMb: Long,
    val gpuVendor: String,
    val gpuRenderer: String,
    val isSnapdragon680: Boolean,
    val recommendedRamMb: Int,
    val recommendedRenderDistance: Int,
    val recommendedGraphics: String, // "Fast" or "Fancy"
    val recommendedDriver: String,
    val recommendedJava: String
)

/**
 * Java Runtime environment model.
 */
data class JavaRuntime(
    val versionMajor: Int,
    val name: String,
    val arch: String,
    val installPath: String,
    val isInstalled: Boolean,
    val sizeMb: Double,
    val downloadUrl: String,
    val supportedMcRange: String
)

/**
 * GPU graphics driver definition.
 */
data class DriverInfo(
    val id: String,
    val name: String,
    val targetGpu: String,
    val targetSoC: String,
    val driverType: String,
    val version: String,
    val recommended: Boolean,
    val features: List<String>,
    val recommendedFlags: String,
    val isInstalled: Boolean = true
)

/**
 * Mobile touch button definition.
 */
data class TouchButton(
    val id: String,
    val label: String,
    val keyCode: Int,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val type: String
)

/**
 * Mobile control profile layout.
 */
data class ControlProfile(
    val profileName: String,
    val version: Int,
    val buttonScale: Float,
    val buttonOpacity: Float,
    val touchSensitivity: Float,
    val vibrationFeedback: Boolean,
    val buttons: List<TouchButton>
)

/**
 * Minecraft Version item model.
 */
data class MinecraftVersion(
    val id: String,
    val type: String, // "release" or "snapshot"
    val url: String,
    val releaseTime: String,
    val javaVersion: Int,
    val recommendedJava: String,
    val description: String,
    val isInstalled: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val clientJarSizeMb: Double = 24.5,
    val modLoader: String = "Vanilla" // "Vanilla", "Fabric", "Forge"
)

/**
 * Modpack definition model.
 */
data class Modpack(
    val id: String,
    val name: String,
    val version: String,
    val loader: String,
    val loaderVersion: String,
    val author: String,
    val description: String,
    val downloads: String,
    val modsCount: Int,
    val sizeMb: Double,
    val downloadUrl: String,
    val category: String,
    val tags: List<String>,
    val isInstalled: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f
)

/**
 * Server model.
 */
data class ServerInfo(
    val id: String,
    val name: String,
    val host: String,
    val port: Int,
    val version: String,
    val motd: String,
    val onlinePlayers: Int,
    val maxPlayers: Int,
    val pingMs: Long,
    val featured: Boolean = false,
    val category: String = "Multiplayer"
)

/**
 * Minecraft player account.
 */
data class Account(
    val id: String,
    val username: String,
    val uuid: String,
    val isMicrosoft: Boolean,
    val skinType: String = "Steve",
    val accessToken: String = "offline_token"
)

/**
 * Optimization wizard progress state.
 */
data class OptimizationStep(
    val stepIndex: Int,
    val title: String,
    val detail: String,
    val isDone: Boolean = false,
    val isRunning: Boolean = false,
    val progress: Float = 0f
)
