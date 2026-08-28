// FILE: app/src/main/java/com/example/core/OptionsManager.kt
package com.example.core

import android.content.Context
import android.util.Log
import com.example.model.DeviceProfile
import java.io.File

/**
 * Manages Minecraft Java Edition options.txt and Sodium / Iris / OptiFine config files.
 * Injects automatically calculated hardware render distance, graphics, resolution,
 * and performance optimizations directly before game launch.
 */
class OptionsManager(private val context: Context) {

    private val TAG = "OptionsManager"

    private val gameDir: File
        get() {
            val dir = File(context.getExternalFilesDir(null), "maazcraft/game")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    private val optionsFile: File
        get() = File(gameDir, "options.txt")

    /**
     * Injects device-optimized graphics settings into options.txt
     */
    fun applyOptimizedSettings(profile: DeviceProfile, prefs: PreferenceManager) {
        try {
            val optionsMap = mutableMapOf<String, String>()

            // Load existing options if file exists
            if (optionsFile.exists()) {
                optionsFile.readLines().forEach { line ->
                    val parts = line.split(":", limit = 2)
                    if (parts.size == 2) {
                        optionsMap[parts[0].trim()] = parts[1].trim()
                    }
                }
            }

            // Apply best render settings based on hardware profile and user preference
            val renderDist = prefs.renderDistance.coerceIn(2, 32)
            val graphicsFast = prefs.graphicsMode.equals("Fast", ignoreCase = true)
            val guiScale = 3

            optionsMap["renderDistance"] = renderDist.toString()
            optionsMap["graphicsMode"] = if (graphicsFast) "0" else "1" // 0: Fast, 1: Fancy
            optionsMap["ao"] = if (graphicsFast) "0" else "1" // Ambient occlusion (0: Off, 1: Min, 2: Max)
            optionsMap["maxFps"] = "60"
            optionsMap["vsync"] = "false"
            optionsMap["guiScale"] = guiScale.toString()
            optionsMap["particles"] = if (graphicsFast) "2" else "0" // 0: All, 1: Decreased, 2: Minimal
            optionsMap["entityShadows"] = if (graphicsFast) "false" else "true"
            optionsMap["mipmapLevels"] = if (graphicsFast) "0" else "2"
            optionsMap["renderClouds"] = if (graphicsFast) "false" else "fast"
            optionsMap["fullscreen"] = "true"
            optionsMap["autoJump"] = "true" // Recommended for touch controls
            optionsMap["pauseOnLostFocus"] = "false"
            optionsMap["fov"] = "70.0"
            optionsMap["gamma"] = "1.0" // Bright gamma for mobile screens

            // Touch screen mode
            optionsMap["touchscreen"] = "1"

            // Save back to options.txt
            val output = StringBuilder()
            optionsMap.forEach { (k, v) ->
                output.append("$k:$v\n")
            }
            optionsFile.writeText(output.toString())
            Log.d(TAG, "options.txt updated with renderDistance=$renderDist, graphicsFast=$graphicsFast")

            // Also configure Sodium / Embeddium options if config dir exists
            configureSodiumOptions(renderDist, graphicsFast)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply options.txt settings", e)
        }
    }

    private fun configureSodiumOptions(renderDistance: Int, isFast: Boolean) {
        try {
            val configDir = File(gameDir, "config")
            if (!configDir.exists()) configDir.mkdirs()

            val sodiumConfig = File(configDir, "sodium-options.json")
            val content = """
                {
                  "quality": {
                    "weather_quality": "${if (isFast) "FAST" else "FANCY"}",
                    "leaves_quality": "${if (isFast) "FAST" else "FANCY"}",
                    "particle_quality": "${if (isFast) "LOW" else "MEDIUM"}",
                    "smooth_lighting": ${!isFast},
                    "enable_vignette": false
                  },
                  "performance": {
                    "chunk_builder_threads": 0,
                    "always_defer_chunk_updates": true,
                    "use_block_face_culling": true,
                    "use_fog_occlusion": true,
                    "use_entity_culling": true,
                    "animate_only_visible_textures": true
                  },
                  "advanced": {
                    "allow_direct_memory_access": true,
                    "use_memory_tracing": false
                  }
                }
            """.trimIndent()
            sodiumConfig.writeText(content)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write sodium-options.json", e)
        }
    }
}
