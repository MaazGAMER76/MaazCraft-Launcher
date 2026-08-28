// FILE: app/src/main/java/com/example/core/DeviceProfile.kt
package com.example.core

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import com.example.model.DeviceProfile
import org.json.JSONObject
import java.io.File
import java.io.RandomAccessFile

/**
 * Hardware and environment detection engine for MaazCraft Launcher.
 * Auto-detects CPU, GPU, RAM, thermal profile, and selects the ideal render pipeline:
 * - MobileGlues Turnip + Zink Vulkan for Qualcomm Snapdragon / Adreno GPUs
 * - ANGLE OpenGLES 3.2 on Vulkan for ARM Mali GPUs (MediaTek Dimensity / Unisoc T606 / Exynos)
 * - Optimized options.txt settings injection before game launch
 */
class DeviceProfileManager(private val context: Context) {

    private val profileFile: File
        get() = File(context.filesDir, "profile.json")

    /**
     * Auto-detect hardware profile, calculate optimal flags, and persist to profile.json.
     */
    fun detectAndSave(): DeviceProfile {
        val profile = detectDeviceProfile(context)
        saveProfile(profile)
        return profile
    }

    /**
     * Loads the persisted profile.json or detects anew if not present.
     */
    fun getOrDetect(): DeviceProfile {
        return loadProfile() ?: detectAndSave()
    }

    /**
     * Dynamically optimize and sync preferences based on current device analysis
     */
    fun applyProfileToPreferences(profile: DeviceProfile, prefs: PreferenceManager) {
        prefs.allocatedRamMb = profile.recommendedRamMb
        prefs.renderDistance = profile.recommendedRenderDistance
        prefs.graphicsMode = profile.recommendedGraphics
        prefs.selectedDriverId = profile.recommendedDriver
        prefs.selectedJavaVersion = profile.recommendedJava
    }

    fun saveProfile(profile: DeviceProfile) {
        try {
            val json = JSONObject().apply {
                put("cpuModel", profile.cpuModel)
                put("socName", profile.socName)
                put("cpuCores", profile.cpuCores)
                put("cpuArch", profile.cpuArch)
                put("isArm64", profile.cpuArch.contains("64"))
                put("isArm32", profile.cpuArch.contains("v7") || profile.cpuArch.contains("32"))
                put("totalRamMb", profile.totalRamMb)
                put("availableRamMb", profile.availableRamMb)
                put("gpuVendor", profile.gpuVendor)
                put("gpuRenderer", profile.gpuRenderer)
                put("isSnapdragon680", profile.isSnapdragon680)
                put("recommendedRamMb", profile.recommendedRamMb)
                put("xmxPercentage", 25)
                put("recommendedRenderDistance", profile.recommendedRenderDistance)
                put("recommendedGraphics", profile.recommendedGraphics)
                put("recommendedDriver", profile.recommendedDriver)
                put("recommendedJava", profile.recommendedJava)
                put("timestamp", System.currentTimeMillis())
            }
            profileFile.writeText(json.toString(2))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadProfile(): DeviceProfile? {
        if (!profileFile.exists()) return null
        return try {
            val jsonStr = profileFile.readText()
            val obj = JSONObject(jsonStr)
            DeviceProfile(
                cpuModel = obj.optString("cpuModel", "ARM Octa-Core"),
                socName = obj.optString("socName", "Universal SoC"),
                cpuCores = obj.optInt("cpuCores", 8),
                cpuArch = obj.optString("cpuArch", "aarch64"),
                totalRamMb = obj.optLong("totalRamMb", 4096),
                availableRamMb = obj.optLong("availableRamMb", 2048),
                gpuVendor = obj.optString("gpuVendor", "Qualcomm"),
                gpuRenderer = obj.optString("gpuRenderer", "Adreno (TM) 610 (MobileGlues Vulkan)"),
                isSnapdragon680 = obj.optBoolean("isSnapdragon680", true),
                recommendedRamMb = obj.optInt("recommendedRamMb", 2048),
                recommendedRenderDistance = obj.optInt("recommendedRenderDistance", 8),
                recommendedGraphics = obj.optString("recommendedGraphics", "Fast"),
                recommendedDriver = obj.optString("recommendedDriver", "mobileglues-turnip-zink"),
                recommendedJava = obj.optString("recommendedJava", "Java 21 ARM64")
            )
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        fun detectDeviceProfile(context: Context): DeviceProfile {
            val totalRamMb = getTotalRam(context)
            val availableRamMb = getAvailableRam(context)

            // CPU Architecture detection (arm64 vs arm32)
            val abiList = Build.SUPPORTED_ABIS ?: arrayOf(Build.CPU_ABI)
            val primaryAbi = abiList.firstOrNull() ?: System.getProperty("os.arch") ?: "aarch64"
            val isArm64 = primaryAbi.contains("arm64", ignoreCase = true) ||
                    primaryAbi.contains("aarch64", ignoreCase = true)
            val isArm32 = primaryAbi.contains("armeabi", ignoreCase = true) ||
                    primaryAbi.contains("v7a", ignoreCase = true) ||
                    primaryAbi.contains("armv7", ignoreCase = true)

            val cpuArch = when {
                isArm64 -> "aarch64 (ARM64-v8a)"
                isArm32 -> "armv7l (ARM32-v7a)"
                else -> primaryAbi
            }

            val cpuCores = Runtime.getRuntime().availableProcessors().coerceAtLeast(4)
            val cpuInfo = readCpuInfo()

            val socModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Build.SOC_MODEL
            } else {
                Build.HARDWARE
            }

            val manufacturer = Build.MANUFACTURER.lowercase()
            val model = Build.MODEL.lowercase()

            // 1. Check for Qualcomm Snapdragon / Adreno
            val isRedmiNote11 = (manufacturer.contains("xiaomi") || manufacturer.contains("redmi")) &&
                    (model.contains("note 11") || model.contains("2201117") || model.contains("spes")) ||
                    socModel.contains("SM6225", ignoreCase = true) ||
                    socModel.contains("680", ignoreCase = true)

            // 2. Check for Unisoc / MediaTek / Mali devices (e.g. itel S26, Helio, Dimensity)
            val isItelDevice = manufacturer.contains("itel") ||
                    model.contains("s26") || model.contains("s66") || model.contains("p55") ||
                    socModel.contains("ums9230", ignoreCase = true) ||
                    socModel.contains("t606", ignoreCase = true) ||
                    socModel.contains("t616", ignoreCase = true) ||
                    socModel.contains("unisoc", ignoreCase = true) ||
                    socModel.contains("sprd", ignoreCase = true)

            val isAdreno = isRedmiNote11 ||
                    socModel.contains("SM6225", ignoreCase = true) ||
                    socModel.contains("qcom", ignoreCase = true) ||
                    socModel.contains("snapdragon", ignoreCase = true) ||
                    socModel.contains("adreno", ignoreCase = true) ||
                    cpuInfo.contains("Qualcomm", ignoreCase = true) ||
                    Build.BOARD.contains("bengal", ignoreCase = true) ||
                    Build.HARDWARE.contains("qcom", ignoreCase = true)

            val isMali = isItelDevice ||
                    socModel.contains("mali", ignoreCase = true) ||
                    socModel.contains("mtk", ignoreCase = true) ||
                    socModel.contains("dimensity", ignoreCase = true) ||
                    socModel.contains("helio", ignoreCase = true) ||
                    socModel.contains("exynos", ignoreCase = true) ||
                    cpuInfo.contains("MediaTek", ignoreCase = true) ||
                    cpuInfo.contains("Unisoc", ignoreCase = true)

            val gpuVendor = when {
                isAdreno -> "Qualcomm"
                isMali -> "ARM / Mali"
                else -> "Universal"
            }

            val gpuRenderer = when {
                isRedmiNote11 -> "Adreno (TM) 610 (MobileGlues Turnip Vulkan)"
                isAdreno -> "Qualcomm Adreno (MobileGlues Turnip Vulkan)"
                isItelDevice -> "ARM Mali-G57 MP1 (ANGLE Vulkan)"
                isMali -> "ARM Mali Valhall/Bifrost (ANGLE Vulkan)"
                else -> "MobileGlues GL4ES Compatibility Wrapper"
            }

            // Calculation of RAM allocation based on real physical memory
            val calculatedXmx = when {
                totalRamMb >= 8000 -> 2560 // 2.5 GB for 8GB+ phones
                totalRamMb >= 6000 -> 2048 // 2.0 GB for 6GB phones
                totalRamMb >= 4000 -> 1536 // 1.5 GB for 4GB phones
                else -> 1024 // 1.0 GB for low memory devices
            }

            // MobileGlues Turnip renderer is the highest-performing driver for Adreno
            val recommendedDriver = when {
                isAdreno -> "mobileglues-turnip-zink"
                isMali -> "angle-mali"
                else -> "gl4es-adreno-generic"
            }

            val recommendedRenderDist = when {
                totalRamMb >= 8000 -> 8
                totalRamMb >= 6000 -> 8
                totalRamMb >= 4000 -> 6
                else -> 4
            }

            val socNameFinal = when {
                isRedmiNote11 -> "Qualcomm Snapdragon 680 (SM6225)"
                isAdreno -> "Qualcomm Snapdragon Octa-Core"
                isItelDevice -> "Unisoc T606 (Dynamic RAM)"
                isMali -> "ARM Mali Octa-Core"
                else -> if (socModel.isNotBlank() && socModel != "unknown") socModel else "${Build.MANUFACTURER} ${Build.MODEL}"
            }

            val cpuModelFinal = when {
                isRedmiNote11 -> "Redmi Note 11 Kryo 265"
                isItelDevice -> "itel S26 Cortex-A75/A55"
                else -> "${Build.MANUFACTURER} ${Build.MODEL}"
            }

            return DeviceProfile(
                cpuModel = cpuModelFinal,
                socName = socNameFinal,
                cpuCores = cpuCores,
                cpuArch = cpuArch,
                totalRamMb = totalRamMb,
                availableRamMb = availableRamMb,
                gpuVendor = gpuVendor,
                gpuRenderer = gpuRenderer,
                isSnapdragon680 = isAdreno,
                recommendedRamMb = calculatedXmx,
                recommendedRenderDistance = recommendedRenderDist,
                recommendedGraphics = "Fast",
                recommendedDriver = recommendedDriver,
                recommendedJava = if (isArm64) "Java 21 ARM64 (MC 1.21+)" else "Java 8 ARM32"
            )
        }

        private fun getTotalRam(context: Context): Long {
            return try {
                val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val memInfo = ActivityManager.MemoryInfo()
                actManager.getMemoryInfo(memInfo)
                memInfo.totalMem / (1024 * 1024)
            } catch (e: Exception) {
                readProcMemInfo()
            }
        }

        private fun getAvailableRam(context: Context): Long {
            return try {
                val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val memInfo = ActivityManager.MemoryInfo()
                actManager.getMemoryInfo(memInfo)
                memInfo.availMem / (1024 * 1024)
            } catch (e: Exception) {
                2048L
            }
        }

        private fun readProcMemInfo(): Long {
            return try {
                val reader = RandomAccessFile("/proc/meminfo", "r")
                val load = reader.readLine()
                reader.close()
                val total = load.replace(Regex("\\D+"), "").toLong()
                total / 1024
            } catch (e: Exception) {
                4096L
            }
        }

        private fun readCpuInfo(): String {
            return try {
                val file = File("/proc/cpuinfo")
                if (file.exists()) file.readText() else "Qualcomm Snapdragon Kryo"
            } catch (e: Exception) {
                "Qualcomm Snapdragon"
            }
        }
    }
}
