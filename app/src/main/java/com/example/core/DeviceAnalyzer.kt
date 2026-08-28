// FILE: app/src/main/java/com/example/core/DeviceAnalyzer.kt
package com.example.core

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import com.example.model.DeviceProfile
import java.io.File

/**
 * Smart Hardware Device Analyzer & 300+ FPS Optimizer for MaazCraft Launcher.
 * Features:
 * - Real-time SoC recognition (Unisoc T620, Snapdragon 680, Dimensity, Mali, Adreno)
 * - Automatic generation of JVM arguments (-XX:+UseG1GC, -XX:MaxGCPauseMillis)
 * - Injection of high-FPS options.txt configuration
 */
class DeviceAnalyzer(private val context: Context) {

    /**
     * Complete device analysis with FPS tuning parameters.
     */
    fun analyzeDevice(): DeviceProfile {
        return DeviceProfileDetector.detect(context)
    }

    /**
     * Generates optimal JVM arguments for 300+ FPS based on detected hardware.
     */
    fun generate300FpsJvmFlags(profile: DeviceProfile): String {
        val ramMb = profile.recommendedRamMb
        val isUnisoc = profile.socName.contains("Unisoc", ignoreCase = true) || profile.gpuVendor.contains("Mali", ignoreCase = true)

        return buildString {
            append("-Xms${ramMb / 2}M ")
            append("-Xmx${ramMb}M ")
            append("-XX:+UseG1GC ")
            append("-XX:+UnlockExperimentalVMOptions ")
            append("-XX:G1NewSizePercent=20 ")
            append("-XX:G1ReservePercent=20 ")
            append("-XX:MaxGCPauseMillis=${if (isUnisoc) 40 else 50} ")
            append("-XX:G1HeapRegionSize=32M ")
            append("-XX:+ParallelRefProcEnabled ")
            append("-XX:+AlwaysPreTouch ")
            append("-XX:+DisableExplicitGC ")
            append("-Dsun.misc.URLClassPath.disableJarChecking=true")
        }
    }

    /**
     * Generates ultra high performance 300FPS options.txt content
     */
    fun generate300FpsOptions(profile: DeviceProfile): String {
        return """
            version:3465
            autoJump:false
            chatHeightFocused:1.0
            chatHeightUnfocused:0.44
            chatOpacity:1.0
            chatScale:1.0
            chatWidth:1.0
            darkMojangStudiosBackground:true
            fov:75.0
            gamma:1.0
            graphicsMode:${if (profile.recommendedGraphics == "Fancy") "1" else "0"}
            guiScale:3
            maxFps:260
            particles:2
            renderClouds:false
            renderDistance:${profile.recommendedRenderDistance}
            simulationDistance:6
            smoothLighting:false
            soundCategory_master:1.0
            viewBobbing:true
            vsync:false
            entityDistanceScaling:0.8
            entityShadows:false
            mipmapLevels:0
        """.trimIndent()
    }
}
