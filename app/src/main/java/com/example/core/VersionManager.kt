// FILE: app/src/main/java/com/example/core/VersionManager.kt
package com.example.core

import android.content.Context
import com.example.model.MinecraftVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class VersionManager(private val context: Context) {

    private val downloader = VersionDownloader(context)

    suspend fun fetchVersionsList(): List<MinecraftVersion> = withContext(Dispatchers.IO) {
        val list = mutableListOf<MinecraftVersion>()
        list.addAll(downloader.fetchManifest())

        // If list is empty (offline mode), load local presets
        if (list.isEmpty()) {
            list.addAll(loadLocalVersionsList())
        }

        // Always ensure key popular versions exist
        val popularDefaults = loadLocalVersionsList()
        for (item in popularDefaults) {
            if (list.none { it.id == item.id }) {
                list.add(item)
            }
        }

        // Update installation status
        list.map { it.copy(isInstalled = isVersionInstalled(it.id)) }
    }

    fun loadLocalVersionsList(): List<MinecraftVersion> {
        return listOf(
            MinecraftVersion(
                id = "1.21.1",
                type = "release",
                url = "https://piston-meta.mojang.com/v1/packages/1.21.1/version.json",
                releaseTime = "2024-08-08",
                javaVersion = 21,
                recommendedJava = "Java 21 ARM64",
                description = "Tricky Trials Update (Mace, Crafter, Breeze)",
                isInstalled = isVersionInstalled("1.21.1")
            ),
            MinecraftVersion(
                id = "1.20.1",
                type = "release",
                url = "https://piston-meta.mojang.com/v1/packages/1.20.1/version.json",
                releaseTime = "2023-06-12",
                javaVersion = 17,
                recommendedJava = "Java 17 ARM64",
                description = "Trails & Tales Update (Sniffer, Cherry Groves)",
                isInstalled = isVersionInstalled("1.20.1")
            ),
            MinecraftVersion(
                id = "1.19.4",
                type = "release",
                url = "https://piston-meta.mojang.com/v1/packages/1.19.4/version.json",
                releaseTime = "2023-03-14",
                javaVersion = 17,
                recommendedJava = "Java 17 ARM64",
                description = "The Wild Update Polish",
                isInstalled = isVersionInstalled("1.19.4")
            ),
            MinecraftVersion(
                id = "1.16.5",
                type = "release",
                url = "https://piston-meta.mojang.com/v1/packages/1.16.5/version.json",
                releaseTime = "2021-01-15",
                javaVersion = 8,
                recommendedJava = "Java 8 ARM64",
                description = "Nether Update (Netherite, Piglins)",
                isInstalled = isVersionInstalled("1.16.5")
            ),
            MinecraftVersion(
                id = "1.12.2",
                type = "release",
                url = "https://piston-meta.mojang.com/v1/packages/1.12.2/version.json",
                releaseTime = "2017-09-18",
                javaVersion = 8,
                recommendedJava = "Java 8 ARM64",
                description = "World of Color (Classic Modding Forge / RLCraft)",
                isInstalled = isVersionInstalled("1.12.2")
            ),
            MinecraftVersion(
                id = "1.8.9",
                type = "release",
                url = "https://piston-meta.mojang.com/v1/packages/1.8.9/version.json",
                releaseTime = "2015-12-09",
                javaVersion = 8,
                recommendedJava = "Java 8 ARM64",
                description = "Bountiful Update (Classic 1.8 PvP Standard)",
                isInstalled = isVersionInstalled("1.8.9")
            )
        )
    }

    fun isVersionInstalled(versionId: String): Boolean {
        return downloader.isVersionDownloaded(versionId)
    }

    suspend fun downloadVersion(
        version: MinecraftVersion,
        onProgress: (Float, String) -> Unit
    ): Boolean {
        return downloader.downloadVersion(version, onProgress)
    }

    fun getVersionJarPath(versionId: String): String {
        return downloader.getClientJarPath(versionId)
    }
}
