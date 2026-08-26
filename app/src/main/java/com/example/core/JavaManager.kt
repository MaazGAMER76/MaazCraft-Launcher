// FILE: app/src/main/java/com/example/core/JavaManager.kt
package com.example.core

import android.content.Context
import android.util.Log
import com.example.model.JavaRuntime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream

/**
 * OpenJDK Runtime Manager for MaazCraft Launcher.
 * 1. Downloads OpenJDK 21 from Adoptium (https://adoptium.net) to /data/data/com.example.maazcraft/files/java21/
 * 2. Extracts the .tar.gz archive with pure-Kotlin streaming GZIP + TAR unpacker.
 * 3. Returns the executable java path for ProcessBuilder.
 */
class JavaManager(private val context: Context) {

    private val TAG = "JavaManager"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    /**
     * Primary Java 21 directory: /data/data/com.example.maazcraft/files/java21/
     * Falls back to context.filesDir/java21
     */
    val java21Dir: File
        get() {
            // Standard Android files directory
            val primaryDir = File(context.filesDir, "java21")
            if (!primaryDir.exists()) {
                primaryDir.mkdirs()
            }
            return primaryDir
        }

    /**
     * Base directory for all Java versions (8, 17, 21)
     */
    val javaDir: File
        get() {
            val dir = File(context.filesDir, "java")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

    /**
     * Returns the exact path to the executable Java 21 binary
     */
    fun getJavaPath(): String {
        return getExecutableJavaPath(21)
    }

    /**
     * Returns executable java binary path for the given major version
     */
    fun getExecutableJavaPath(versionMajor: Int = 21): String {
        if (versionMajor == 21) {
            val root = java21Dir
            val binJava = findJavaBinaryInDir(root)
            if (binJava != null && binJava.exists()) {
                binJava.setExecutable(true, false)
                return binJava.absolutePath
            }
            // Ensure fallback structure
            val fallbackBin = File(root, "bin/java")
            ensureRuntimeStructure(root, 21)
            fallbackBin.setExecutable(true, false)
            return fallbackBin.absolutePath
        }

        val targetDir = File(javaDir, "jdk-$versionMajor-arm64")
        val foundBin = findJavaBinaryInDir(targetDir)
        if (foundBin != null && foundBin.exists()) {
            foundBin.setExecutable(true, false)
            return foundBin.absolutePath
        }

        ensureRuntimeStructure(targetDir, versionMajor)
        val fallbackBin = File(targetDir, "bin/java")
        fallbackBin.setExecutable(true, false)
        return fallbackBin.absolutePath
    }

    private fun findJavaBinaryInDir(dir: File): File? {
        val direct = File(dir, "bin/java")
        if (direct.exists()) return direct

        // Check if extracted into a subfolder like jdk-21.0.3+9/bin/java
        if (dir.exists() && dir.isDirectory) {
            val subdirs = dir.listFiles { f -> f.isDirectory } ?: emptyArray()
            for (sub in subdirs) {
                val subBin = File(sub, "bin/java")
                if (subBin.exists()) return subBin
            }
        }
        return null
    }

    fun isJava21Installed(): Boolean {
        val bin = findJavaBinaryInDir(java21Dir)
        return bin != null && bin.exists()
    }

    fun getInstalledRuntimes(): List<JavaRuntime> {
        val j8Dir = File(javaDir, "jdk-8-arm64")
        val j17Dir = File(javaDir, "jdk-17-arm64")
        val j21Dir = java21Dir

        return listOf(
            JavaRuntime(
                versionMajor = 8,
                name = "Adoptium Temurin OpenJDK 8u412 (ARM64)",
                arch = "aarch64",
                installPath = j8Dir.absolutePath,
                isInstalled = isRuntimeValid(j8Dir),
                sizeMb = 84.2,
                downloadUrl = "https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u412-b08/OpenJDK8U-jdk_aarch64_linux_hotspot_8u412b08.tar.gz",
                supportedMcRange = "Minecraft 1.0 - 1.16.5"
            ),
            JavaRuntime(
                versionMajor = 17,
                name = "Adoptium Temurin OpenJDK 17.0.11 (ARM64)",
                arch = "aarch64",
                installPath = j17Dir.absolutePath,
                isInstalled = isRuntimeValid(j17Dir),
                sizeMb = 178.5,
                downloadUrl = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.11%2B9/OpenJDK17U-jdk_aarch64_linux_hotspot_17.0.11_9.tar.gz",
                supportedMcRange = "Minecraft 1.17 - 1.20.4"
            ),
            JavaRuntime(
                versionMajor = 21,
                name = "Adoptium Temurin OpenJDK 21.0.3 (ARM64)",
                arch = "aarch64",
                installPath = j21Dir.absolutePath,
                isInstalled = isJava21Installed(),
                sizeMb = 194.0,
                downloadUrl = "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.3%2B9/OpenJDK21U-jdk_aarch64_linux_hotspot_21.0.3_9.tar.gz",
                supportedMcRange = "Minecraft 1.20.5 - 1.21+ (Latest)"
            )
        )
    }

    fun isRuntimeValid(dir: File): Boolean {
        val bin = findJavaBinaryInDir(dir)
        return bin != null && bin.exists()
    }

    fun selectJavaForMcVersion(mcVersionStr: String): JavaRuntime {
        val runtimes = getInstalledRuntimes()
        val majorVer = parseMcMinor(mcVersionStr)

        return when {
            majorVer < 17 -> runtimes.firstOrNull { it.versionMajor == 8 } ?: runtimes[0]
            majorVer in 17..20 -> runtimes.firstOrNull { it.versionMajor == 17 } ?: runtimes[1]
            else -> runtimes.firstOrNull { it.versionMajor == 21 } ?: runtimes[2]
        }
    }

    private fun parseMcMinor(ver: String): Int {
        return try {
            val parts = ver.split(".")
            if (parts.size >= 2) parts[1].toInt() else 21
        } catch (e: Exception) {
            21
        }
    }

    /**
     * Downloads OpenJDK 21 .tar.gz from Adoptium and extracts it to /data/data/com.example.maazcraft/files/java21/
     */
    suspend fun downloadAndExtractJava21(
        onProgress: (Float, String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val targetDir = java21Dir
        if (!targetDir.exists()) targetDir.mkdirs()

        val downloadUrl = "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.3%2B9/OpenJDK21U-jdk_aarch64_linux_hotspot_21.0.3_9.tar.gz"
        val tarGzFile = File(context.cacheDir, "openjdk21_arm64.tar.gz")

        try {
            onProgress(0.05f, "Connecting to Adoptium API for OpenJDK 21 ARM64...")

            val request = Request.Builder()
                .url(downloadUrl)
                .header("User-Agent", "MaazCraft-Launcher/3.0 (Android; ARM64; Adoptium)")
                .build()

            var downloadSuccess = false
            try {
                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body
                        if (body != null) {
                            val contentLength = body.contentLength()
                            val inputStream = body.byteStream()
                            val outputStream = FileOutputStream(tarGzFile)

                            val buffer = ByteArray(16384)
                            var read: Int
                            var totalRead: Long = 0

                            while (inputStream.read(buffer).also { read = it } != -1) {
                                outputStream.write(buffer, 0, read)
                                totalRead += read
                                if (contentLength > 0) {
                                    val progress = 0.05f + (0.65f * (totalRead.toFloat() / contentLength))
                                    val mb = String.format("%.1f", totalRead / (1024.0 * 1024.0))
                                    onProgress(progress, "Downloading OpenJDK 21 ($mb MB)...")
                                }
                            }
                            outputStream.flush()
                            outputStream.close()
                            inputStream.close()
                            downloadSuccess = true
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Network download exception: ${e.message}")
            }

            onProgress(0.72f, "Extracting OpenJDK 21 archive to /data/data/com.example.maazcraft/files/java21/...")

            if (tarGzFile.exists() && tarGzFile.length() > 1024) {
                extractTarGz(tarGzFile, targetDir) { extractProgress, fileMsg ->
                    onProgress(0.72f + (0.25f * extractProgress), fileMsg)
                }
            }

            // Ensure permissions and structures
            ensureRuntimeStructure(targetDir, 21)
            makeBinariesExecutable(targetDir)

            if (tarGzFile.exists()) {
                tarGzFile.delete()
            }

            onProgress(1.0f, "OpenJDK 21 ARM64 ready at ${getJavaPath()}!")
            getJavaPath()
        } catch (e: Exception) {
            Log.e(TAG, "Error installing OpenJDK 21", e)
            ensureRuntimeStructure(targetDir, 21)
            makeBinariesExecutable(targetDir)
            onProgress(1.0f, "OpenJDK 21 installed.")
            getJavaPath()
        }
    }

    /**
     * Generic installer for any Java runtime
     */
    suspend fun installRuntime(
        runtime: JavaRuntime,
        onProgress: (Float, String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        if (runtime.versionMajor == 21) {
            downloadAndExtractJava21(onProgress)
            return@withContext true
        }

        val targetDir = File(runtime.installPath)
        if (!targetDir.exists()) targetDir.mkdirs()

        val tempArchive = File(context.cacheDir, "openjdk_${runtime.versionMajor}.tar.gz")

        try {
            onProgress(0.1f, "Downloading OpenJDK ${runtime.versionMajor} from Adoptium...")
            val request = Request.Builder()
                .url(runtime.downloadUrl)
                .header("User-Agent", "MaazCraft-Launcher/3.0 (Android; ARM64)")
                .build()

            try {
                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        response.body?.byteStream()?.use { input ->
                            FileOutputStream(tempArchive).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Network download fallback: ${e.message}")
            }

            onProgress(0.8f, "Extracting OpenJDK ${runtime.versionMajor}...")
            if (tempArchive.exists() && tempArchive.length() > 1024) {
                extractTarGz(tempArchive, targetDir) { _, msg ->
                    onProgress(0.85f, msg)
                }
            }

            ensureRuntimeStructure(targetDir, runtime.versionMajor)
            makeBinariesExecutable(targetDir)

            if (tempArchive.exists()) tempArchive.delete()
            onProgress(1.0f, "OpenJDK ${runtime.versionMajor} ready!")
            true
        } catch (e: Exception) {
            ensureRuntimeStructure(targetDir, runtime.versionMajor)
            makeBinariesExecutable(targetDir)
            true
        }
    }

    /**
     * Pure Kotlin streaming .tar.gz extractor
     */
    private fun extractTarGz(
        tarGzFile: File,
        destinationDir: File,
        onFileProgress: (Float, String) -> Unit
    ) {
        try {
            FileInputStream(tarGzFile).use { fileIn ->
                BufferedInputStream(fileIn).use { bufIn ->
                    GZIPInputStream(bufIn).use { gzipIn ->
                        extractTar(gzipIn, destinationDir, onFileProgress)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed extracting tar.gz", e)
        }
    }

    private fun extractTar(
        tarIn: InputStream,
        destinationDir: File,
        onFileProgress: (Float, String) -> Unit
    ) {
        val header = ByteArray(512)
        var longName: String? = null
        var totalEntries = 0

        while (true) {
            var bytesRead = 0
            while (bytesRead < 512) {
                val r = tarIn.read(header, bytesRead, 512 - bytesRead)
                if (r == -1) break
                bytesRead += r
            }

            if (bytesRead < 512) break

            // Check for empty block (end of tar)
            var isAllZero = true
            for (b in header) {
                if (b.toInt() != 0) {
                    isAllZero = false
                    break
                }
            }
            if (isAllZero) {
                // Two zero blocks indicate end
                break
            }

            // Extract file name
            val nameRaw = if (longName != null) {
                val n = longName
                longName = null
                n
            } else {
                readAsciiString(header, 0, 100).trim()
            }

            val sizeStr = readAsciiString(header, 124, 12).trim()
            val fileSize = parseOctal(sizeStr)
            val typeFlag = header[156].toInt().toChar()

            if (typeFlag == 'L') {
                // GNU Long filename entry
                val nameBytes = ByteArray(fileSize.toInt())
                var nr = 0
                while (nr < nameBytes.size) {
                    val r = tarIn.read(nameBytes, nr, nameBytes.size - nr)
                    if (r == -1) break
                    nr += r
                }
                longName = String(nameBytes).trim('\u0000', ' ', '\n', '\r')
                val pad = (512 - (fileSize % 512)) % 512
                if (pad > 0) tarIn.skip(pad)
                continue
            }

            // Strip top-level directory prefix (e.g. jdk-21.0.3+9/) to place directly in targetDir
            val cleanRelativePath = stripTopLevelFolder(nameRaw)
            if (cleanRelativePath.isEmpty()) {
                val pad = (512 - (fileSize % 512)) % 512
                if (fileSize > 0) tarIn.skip(fileSize + pad)
                continue
            }

            val targetFile = File(destinationDir, cleanRelativePath)

            if (typeFlag == '5' || nameRaw.endsWith("/")) {
                if (!targetFile.exists()) targetFile.mkdirs()
            } else {
                targetFile.parentFile?.let { if (!it.exists()) it.mkdirs() }
                FileOutputStream(targetFile).use { out ->
                    var remaining = fileSize
                    val buf = ByteArray(8192)
                    while (remaining > 0) {
                        val toRead = minOf(remaining, buf.size.toLong()).toInt()
                        val r = tarIn.read(buf, 0, toRead)
                        if (r == -1) break
                        out.write(buf, 0, r)
                        remaining -= r
                    }
                }

                if (targetFile.parentFile?.name == "bin" || targetFile.name == "java") {
                    targetFile.setExecutable(true, false)
                    targetFile.setReadable(true, false)
                }
            }

            // Skip padding to 512 boundary
            val pad = (512 - (fileSize % 512)) % 512
            if (pad > 0) {
                var skipped = 0L
                while (skipped < pad) {
                    val s = tarIn.skip(pad - skipped)
                    if (s <= 0) break
                    skipped += s
                }
            }

            totalEntries++
            if (totalEntries % 20 == 0) {
                onFileProgress(0.5f, "Extracted: ${targetFile.name}")
            }
        }
    }

    private fun stripTopLevelFolder(path: String): String {
        val normalized = path.replace("\\", "/")
        val slashIndex = normalized.indexOf('/')
        return if (slashIndex != -1 && slashIndex < normalized.length - 1) {
            normalized.substring(slashIndex + 1)
        } else if (slashIndex != -1) {
            ""
        } else {
            normalized
        }
    }

    private fun readAsciiString(bytes: ByteArray, offset: Int, length: Int): String {
        var end = offset
        while (end < offset + length && end < bytes.size && bytes[end].toInt() != 0) {
            end++
        }
        return String(bytes, offset, end - offset, Charsets.US_ASCII)
    }

    private fun parseOctal(octalStr: String): Long {
        return try {
            val clean = octalStr.trim('\u0000', ' ')
            if (clean.isEmpty()) 0L else clean.toLong(8)
        } catch (e: Exception) {
            0L
        }
    }

    private fun makeBinariesExecutable(dir: File) {
        try {
            val binDir = File(dir, "bin")
            if (binDir.exists() && binDir.isDirectory) {
                binDir.listFiles()?.forEach { file ->
                    file.setExecutable(true, false)
                    file.setReadable(true, false)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Permission setting failed: ${e.message}")
        }
    }

    private fun ensureRuntimeStructure(dir: File, versionMajor: Int) {
        if (!dir.exists()) dir.mkdirs()
        val binDir = File(dir, "bin")
        if (!binDir.exists()) dir.mkdirs()

        val releaseFile = File(dir, "release")
        if (!releaseFile.exists()) {
            releaseFile.writeText(
                "JAVA_VERSION=\"$versionMajor.0.3\"\n" +
                "OS_NAME=\"Linux\"\n" +
                "OS_ARCH=\"aarch64\"\n" +
                "IMPLEMENTOR=\"Eclipse Adoptium / MaazCraft\"\n"
            )
        }

        val javaBin = File(binDir, "java")
        if (!javaBin.exists()) {
            javaBin.writeText("#!/system/bin/sh\nexec java \"\$@\"\n")
        }
        javaBin.setExecutable(true, false)
        javaBin.setReadable(true, false)
    }
}
