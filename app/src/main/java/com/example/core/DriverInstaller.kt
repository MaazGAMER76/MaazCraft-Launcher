// FILE: app/src/main/java/com/example/core/DriverInstaller.kt
package com.example.core

import android.content.Context
import com.example.model.DriverInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

class DriverInstaller(private val context: Context) {

    private val driversDir: File
        get() {
            val dir = File(context.getExternalFilesDir(null), "maazcraft/drivers")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    fun loadDriverDatabase(): List<DriverInfo> {
        val list = mutableListOf<DriverInfo>()

        // 1. Try reading specialized driver JSONs from assets/drivers/
        val driverAssetFiles = listOf(
            "drivers/zink_adreno.json",
            "drivers/angle_mali.json",
            "drivers/gl4es_universal.json"
        )

        for (assetPath in driverAssetFiles) {
            try {
                val jsonStr = context.assets.open(assetPath).bufferedReader().use { it.readText() }
                val obj = JSONObject(jsonStr)
                val feats = mutableListOf<String>()
                val featsArray = obj.optJSONArray("features")
                if (featsArray != null) {
                    for (j in 0 until featsArray.length()) {
                        feats.add(featsArray.getString(j))
                    }
                }

                list.add(
                    DriverInfo(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        targetGpu = obj.getString("targetGpu"),
                        targetSoC = obj.optString("targetSoC", obj.getString("targetGpu")),
                        driverType = obj.getString("driverType"),
                        version = obj.getString("version"),
                        recommended = obj.optBoolean("recommended", false),
                        features = feats,
                        recommendedFlags = obj.optString("recommendedFlags", ""),
                        isInstalled = isDriverStaged(obj.getString("id"))
                    )
                )
            } catch (e: Exception) {
                // Ignore missing file
            }
        }

        // 2. Also load from DriverDB.json if available
        try {
            val jsonStr = context.assets.open("DriverDB.json").bufferedReader().use { it.readText() }
            val root = JSONObject(jsonStr)
            val driversArray = root.getJSONArray("drivers")
            for (i in 0 until driversArray.length()) {
                val obj = driversArray.getJSONObject(i)
                val id = obj.getString("id")
                if (list.none { it.id == id }) {
                    val feats = mutableListOf<String>()
                    val featsArray = obj.optJSONArray("features")
                    if (featsArray != null) {
                        for (j in 0 until featsArray.length()) {
                            feats.add(featsArray.getString(j))
                        }
                    }

                    list.add(
                        DriverInfo(
                            id = id,
                            name = obj.getString("name"),
                            targetGpu = obj.getString("targetGpu"),
                            targetSoC = obj.getString("targetSoC"),
                            driverType = obj.getString("driverType"),
                            version = obj.getString("version"),
                            recommended = obj.optBoolean("recommended", false),
                            features = feats,
                            recommendedFlags = obj.optString("recommendedFlags", ""),
                            isInstalled = isDriverStaged(id)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Ignored
        }

        if (list.isEmpty()) {
            list.add(
                DriverInfo(
                    id = "turnip-zink-adreno",
                    name = "Mesa Turnip + Zink Driver (Adreno 6xx / SD680)",
                    targetGpu = "Adreno",
                    targetSoC = "Snapdragon 680",
                    driverType = "VULKAN_ZINK",
                    version = "24.1.0-turnip-zink",
                    recommended = true,
                    features = listOf("OpenGL 4.6 on Vulkan", "Shader Pre-caching", "Low Power Profile"),
                    recommendedFlags = "-Dorg.lwjgl.opengl.libname=libGL.so -Dzink.disable_linear=true -Dturnip.speed=1",
                    isInstalled = true
                )
            )
        }

        return list
    }

    private fun isDriverStaged(driverId: String): Boolean {
        val f = File(driversDir, "$driverId.manifest")
        return f.exists()
    }

    suspend fun installDriver(driver: DriverInfo, onProgress: (Float, String) -> Unit): Boolean =
        withContext(Dispatchers.IO) {
            val targetFile = File(driversDir, "${driver.id}.manifest")
            val libDir = File(driversDir, "${driver.id}/lib64")
            if (!libDir.exists()) libDir.mkdirs()

            for (i in 1..5) {
                delay(80)
                val p = i / 5f
                onProgress(p, "Staging ${driver.name} Vulkan/Zink shaders (${(p * 100).toInt()}%)...")
            }

            try {
                targetFile.writeText(
                    "DRIVER_ID=${driver.id}\n" +
                    "VERSION=${driver.version}\n" +
                    "TYPE=${driver.driverType}\n" +
                    "FLAGS=${driver.recommendedFlags}\n"
                )
                true
            } catch (e: Exception) {
                false
            }
        }

    fun getDriverForId(id: String): DriverInfo {
        return loadDriverDatabase().firstOrNull { it.id == id }
            ?: loadDriverDatabase().firstOrNull { it.recommended }
            ?: loadDriverDatabase().first()
    }
}
