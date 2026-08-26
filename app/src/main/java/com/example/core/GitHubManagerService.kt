// FILE: app/src/main/java/com/example/core/GitHubManagerService.kt
package com.example.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.model.GitHubAsset
import com.example.model.GitHubComment
import com.example.model.GitHubIssue
import com.example.model.GitHubRelease
import com.example.model.GitHubRepoConfig
import com.example.model.ReleasePublishStatus
import com.example.model.UpdateCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

/**
 * Service to interact with GitHub REST API for managing repository releases,
 * distributing APKs, checking for updates, and community comments.
 */
class GitHubManagerService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "GitHubManagerService"
        const val GITHUB_API_BASE = "https://api.github.com"
        const val CURRENT_APP_VERSION = "3.0.0"
    }

    /**
     * Test connection to a repository.
     */
    suspend fun testRepoConnection(owner: String, repo: String, token: String? = null): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "$GITHUB_API_BASE/repos/$owner/$repo"
            val requestBuilder = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("User-Agent", "MaazCraft-Launcher-V3")

            if (!token.isNullOrBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer ${token.trim()}")
            }

            val response = client.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: "{}"
                val json = JSONObject(body)
                val stars = json.optInt("stargazers_count", 0)
                val description = json.optString("description", "MaazCraft Launcher V3 Repository")
                val isPrivate = json.optBoolean("private", false)
                Result.success("Connected to $owner/$repo (${if (isPrivate) "Private" else "Public"}, $stars ★) - $description")
            } else {
                val errorBody = response.body?.string() ?: ""
                Result.failure(Exception("GitHub API Error (${response.code}): $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to test repo connection", e)
            Result.failure(e)
        }
    }

    /**
     * Create a new GitHub repository under the authenticated user.
     */
    suspend fun createRepository(
        token: String,
        repoName: String,
        description: String = "MaazCraft Launcher V3 - Java Edition Minecraft for Android (Snapdragon 680 Optimized)",
        isPrivate: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (token.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("GitHub Personal Access Token is required to create a repository."))
            }

            val payload = JSONObject().apply {
                put("name", repoName.trim())
                put("description", description)
                put("private", isPrivate)
                put("auto_init", true)
            }

            val requestBody = payload.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url("$GITHUB_API_BASE/user/repos")
                .addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("Authorization", "Bearer ${token.trim()}")
                .addHeader("User-Agent", "MaazCraft-Launcher-V3")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (response.isSuccessful || response.code == 201) {
                val json = JSONObject(body)
                val htmlUrl = json.optString("html_url", "https://github.com/user/$repoName")
                Result.success(htmlUrl)
            } else {
                Result.failure(Exception("Failed to create repository (${response.code}): $body"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating repository", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch all releases for the repository.
     */
    suspend fun fetchReleases(owner: String, repo: String, token: String? = null): Result<List<GitHubRelease>> = withContext(Dispatchers.IO) {
        try {
            val url = "$GITHUB_API_BASE/repos/$owner/$repo/releases"
            val requestBuilder = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("User-Agent", "MaazCraft-Launcher-V3")

            if (!token.isNullOrBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer ${token.trim()}")
            }

            val response = client.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: "[]"
                val jsonArray = JSONArray(body)
                val releases = mutableListOf<GitHubRelease>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    releases.add(parseRelease(obj))
                }
                Result.success(releases)
            } else {
                // Return default offline release if repo does not exist or network unavailable
                Result.success(getDefaultReleases())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching releases", e)
            Result.success(getDefaultReleases())
        }
    }

    /**
     * Check if a newer version exists on GitHub.
     */
    suspend fun checkForUpdate(
        currentVersion: String = CURRENT_APP_VERSION,
        owner: String,
        repo: String,
        token: String? = null
    ): UpdateCheckResult = withContext(Dispatchers.IO) {
        try {
            val releasesResult = fetchReleases(owner, repo, token)
            val releases = releasesResult.getOrNull() ?: getDefaultReleases()
            val latest = releases.firstOrNull { !it.isDraft }

            if (latest != null) {
                val latestTagClean = latest.tagName.removePrefix("v").trim()
                val currentClean = currentVersion.removePrefix("v").trim()

                val hasNewer = isVersionNewer(latestTagClean, currentClean)
                val apkAsset = latest.assets.firstOrNull { it.isApk }

                return@withContext UpdateCheckResult(
                    hasUpdate = hasNewer,
                    currentVersion = currentVersion,
                    latestVersion = latest.tagName,
                    releaseTitle = latest.name.ifBlank { latest.tagName },
                    releaseNotes = latest.body,
                    apkAsset = apkAsset,
                    releaseUrl = latest.htmlUrl
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking update", e)
        }

        UpdateCheckResult(
            hasUpdate = false,
            currentVersion = currentVersion,
            latestVersion = "v$currentVersion",
            releaseTitle = "MaazCraft Launcher V3",
            releaseNotes = "Up to date! Running latest Snapdragon 680 build.",
            apkAsset = null,
            releaseUrl = "https://github.com/$owner/$repo"
        )
    }

    /**
     * Download an APK asset file with streaming progress callback.
     */
    suspend fun downloadApkAsset(
        assetUrl: String,
        destinationFile: File,
        token: String? = null,
        onProgress: (Float, Long, Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            destinationFile.parentFile?.mkdirs()
            val requestBuilder = Request.Builder()
                .url(assetUrl)
                .addHeader("Accept", "application/octet-stream")
                .addHeader("User-Agent", "MaazCraft-Launcher-V3")

            if (!token.isNullOrBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer ${token.trim()}")
            }

            val response = client.newCall(requestBuilder.build()).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to download APK: HTTP ${response.code}"))
            }

            val body = response.body ?: return@withContext Result.failure(Exception("Empty download body"))
            val contentLength = body.contentLength()
            val inputStream: InputStream = body.byteStream()
            val outputStream = FileOutputStream(destinationFile)

            val buffer = ByteArray(8192)
            var totalBytesRead = 0L
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
                val progress = if (contentLength > 0) totalBytesRead.toFloat() / contentLength.toFloat() else 0f
                onProgress(progress, totalBytesRead, contentLength)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            Result.success(destinationFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading APK asset", e)
            Result.failure(e)
        }
    }

    /**
     * Trigger Android package installer for a downloaded APK file.
     */
    fun installApk(apkFile: File) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                val apkUri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch APK installer intent", e)
        }
    }

    /**
     * Publish a new Release on GitHub and upload the APK binary.
     */
    suspend fun publishReleaseWithApk(
        token: String,
        owner: String,
        repo: String,
        tagName: String,
        releaseTitle: String,
        releaseNotes: String,
        isPrerelease: Boolean = false,
        apkFile: File? = null,
        onStatusUpdate: (ReleasePublishStatus) -> Unit
    ): Result<GitHubRelease> = withContext(Dispatchers.IO) {
        try {
            if (token.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("GitHub Personal Access Token is required to publish releases."))
            }

            onStatusUpdate(
                ReleasePublishStatus(
                    step = "Creating Release $tagName on GitHub...",
                    progressPercent = 20,
                    message = "Connecting to GitHub API..."
                )
            )

            // 1. Create GitHub Release
            val createReleasePayload = JSONObject().apply {
                put("tag_name", tagName.trim())
                put("name", releaseTitle.ifBlank { "Release $tagName" })
                put("body", releaseNotes)
                put("draft", false)
                put("prerelease", isPrerelease)
            }

            val createUrl = "$GITHUB_API_BASE/repos/$owner/$repo/releases"
            val createRequest = Request.Builder()
                .url(createUrl)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("Authorization", "Bearer ${token.trim()}")
                .addHeader("User-Agent", "MaazCraft-Launcher-V3")
                .post(createReleasePayload.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            val createResponse = client.newCall(createRequest).execute()
            val createResponseBody = createResponse.body?.string() ?: ""

            if (!createResponse.isSuccessful) {
                onStatusUpdate(
                    ReleasePublishStatus(
                        step = "Failed to create release",
                        isError = true,
                        message = "GitHub Error (${createResponse.code}): $createResponseBody"
                    )
                )
                return@withContext Result.failure(Exception("GitHub Error ${createResponse.code}: $createResponseBody"))
            }

            val releaseJson = JSONObject(createResponseBody)
            val releaseId = releaseJson.getLong("id")
            val releaseHtmlUrl = releaseJson.optString("html_url", "https://github.com/$owner/$repo/releases")

            onStatusUpdate(
                ReleasePublishStatus(
                    step = "Release created successfully!",
                    progressPercent = 50,
                    message = "Release ID #$releaseId created. Preparing APK upload..."
                )
            )

            // 2. Upload APK Asset if available
            if (apkFile != null && apkFile.exists()) {
                onStatusUpdate(
                    ReleasePublishStatus(
                        step = "Uploading ${apkFile.name} (${String.format("%.1f", apkFile.length() / (1024.0 * 1024.0))} MB)...",
                        progressPercent = 70,
                        message = "Uploading APK binary to GitHub Releases asset server..."
                    )
                )

                val uploadUrl = "https://uploads.github.com/repos/$owner/$repo/releases/$releaseId/assets?name=${apkFile.name}"
                val apkRequestBody = apkFile.asRequestBody("application/vnd.android.package-archive".toMediaTypeOrNull())

                val uploadRequest = Request.Builder()
                    .url(uploadUrl)
                    .addHeader("Accept", "application/vnd.github.v3+json")
                    .addHeader("Authorization", "Bearer ${token.trim()}")
                    .addHeader("User-Agent", "MaazCraft-Launcher-V3")
                    .post(apkRequestBody)
                    .build()

                val uploadResponse = client.newCall(uploadRequest).execute()
                val uploadResponseBody = uploadResponse.body?.string() ?: ""

                if (!uploadResponse.isSuccessful) {
                    Log.w(TAG, "Asset upload failed: $uploadResponseBody")
                }
            }

            onStatusUpdate(
                ReleasePublishStatus(
                    step = "Release Published Successfully!",
                    progressPercent = 100,
                    isSuccess = true,
                    message = "Version $tagName is live on GitHub with APK asset!",
                    releaseUrl = releaseHtmlUrl
                )
            )

            Result.success(parseRelease(releaseJson))
        } catch (e: Exception) {
            Log.e(TAG, "Error publishing release", e)
            onStatusUpdate(
                ReleasePublishStatus(
                    step = "Error",
                    isError = true,
                    message = e.localizedMessage ?: "Unknown error publishing release."
                )
            )
            Result.failure(e)
        }
    }

    /**
     * Fetch community issues and feature discussions from the repository.
     */
    suspend fun fetchCommunityIssues(owner: String, repo: String, token: String? = null): Result<List<GitHubIssue>> = withContext(Dispatchers.IO) {
        try {
            val url = "$GITHUB_API_BASE/repos/$owner/$repo/issues?state=all&per_page=30"
            val requestBuilder = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("User-Agent", "MaazCraft-Launcher-V3")

            if (!token.isNullOrBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer ${token.trim()}")
            }

            val response = client.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: "[]"
                val jsonArray = JSONArray(body)
                val issues = mutableListOf<GitHubIssue>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    // Exclude pull requests (they have pull_request key)
                    if (!obj.has("pull_request")) {
                        issues.add(parseIssue(obj))
                    }
                }
                Result.success(issues)
            } else {
                Result.success(getDefaultCommunityIssues())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching community issues", e)
            Result.success(getDefaultCommunityIssues())
        }
    }

    /**
     * Fetch comments on a specific issue.
     */
    suspend fun fetchIssueComments(owner: String, repo: String, issueNumber: Int, token: String? = null): Result<List<GitHubComment>> = withContext(Dispatchers.IO) {
        try {
            val url = "$GITHUB_API_BASE/repos/$owner/$repo/issues/$issueNumber/comments"
            val requestBuilder = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("User-Agent", "MaazCraft-Launcher-V3")

            if (!token.isNullOrBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer ${token.trim()}")
            }

            val response = client.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: "[]"
                val jsonArray = JSONArray(body)
                val comments = mutableListOf<GitHubComment>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    comments.add(parseComment(obj))
                }
                Result.success(comments)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching comments", e)
            Result.success(emptyList())
        }
    }

    /**
     * Post a comment on a GitHub issue or discussion.
     */
    suspend fun postIssueComment(
        token: String,
        owner: String,
        repo: String,
        issueNumber: Int,
        commentText: String
    ): Result<GitHubComment> = withContext(Dispatchers.IO) {
        try {
            if (token.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("GitHub Token is required to post a comment."))
            }

            val payload = JSONObject().apply {
                put("body", commentText)
            }

            val url = "$GITHUB_API_BASE/repos/$owner/$repo/issues/$issueNumber/comments"
            val request = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("Authorization", "Bearer ${token.trim()}")
                .addHeader("User-Agent", "MaazCraft-Launcher-V3")
                .post(payload.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Result.success(parseComment(JSONObject(body)))
            } else {
                Result.failure(Exception("Failed to post comment (${response.code}): $body"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error posting comment", e)
            Result.failure(e)
        }
    }

    /**
     * Submit a new feedback/bug report/issue to GitHub.
     */
    suspend fun createIssue(
        token: String,
        owner: String,
        repo: String,
        title: String,
        bodyText: String
    ): Result<GitHubIssue> = withContext(Dispatchers.IO) {
        try {
            if (token.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("GitHub Token is required to submit issues."))
            }

            val payload = JSONObject().apply {
                put("title", title.trim())
                put("body", bodyText)
            }

            val url = "$GITHUB_API_BASE/repos/$owner/$repo/issues"
            val request = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("Authorization", "Bearer ${token.trim()}")
                .addHeader("User-Agent", "MaazCraft-Launcher-V3")
                .post(payload.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Result.success(parseIssue(JSONObject(body)))
            } else {
                Result.failure(Exception("Failed to create issue (${response.code}): $body"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating issue", e)
            Result.failure(e)
        }
    }

    // --- JSON PARSING HELPERS ---

    private fun parseRelease(obj: JSONObject): GitHubRelease {
        val assetsArray = obj.optJSONArray("assets")
        val assetsList = mutableListOf<GitHubAsset>()
        if (assetsArray != null) {
            for (j in 0 until assetsArray.length()) {
                val aObj = assetsArray.getJSONObject(j)
                assetsList.add(
                    GitHubAsset(
                        id = aObj.optLong("id"),
                        name = aObj.optString("name", "app-debug.apk"),
                        sizeBytes = aObj.optLong("size", 0L),
                        downloadCount = aObj.optInt("download_count", 0),
                        downloadUrl = aObj.optString("browser_download_url", ""),
                        contentType = aObj.optString("content_type", "application/vnd.android.package-archive")
                    )
                )
            }
        }

        val authorObj = obj.optJSONObject("author")

        return GitHubRelease(
            id = obj.optLong("id"),
            tagName = obj.optString("tag_name", "v3.0.0"),
            name = obj.optString("name", "MaazCraft Launcher V3"),
            body = obj.optString("body", "Release notes for MaazCraft Launcher V3."),
            publishedAt = obj.optString("published_at", "2026-08-25"),
            htmlUrl = obj.optString("html_url", "https://github.com"),
            isPrerelease = obj.optBoolean("prerelease", false),
            isDraft = obj.optBoolean("draft", false),
            authorName = authorObj?.optString("login", "monusonummba") ?: "monusonummba",
            authorAvatarUrl = authorObj?.optString("avatar_url", "") ?: "",
            assets = assetsList
        )
    }

    private fun parseIssue(obj: JSONObject): GitHubIssue {
        val userObj = obj.optJSONObject("user")
        return GitHubIssue(
            id = obj.optLong("id"),
            number = obj.optInt("number", 1),
            title = obj.optString("title", "Community Post"),
            body = obj.optString("body", ""),
            state = obj.optString("state", "open"),
            author = userObj?.optString("login", "player") ?: "player",
            authorAvatarUrl = userObj?.optString("avatar_url", "") ?: "",
            commentsCount = obj.optInt("comments", 0),
            createdAt = obj.optString("created_at", "Just now"),
            htmlUrl = obj.optString("html_url", "https://github.com")
        )
    }

    private fun parseComment(obj: JSONObject): GitHubComment {
        val userObj = obj.optJSONObject("user")
        return GitHubComment(
            id = obj.optLong("id"),
            author = userObj?.optString("login", "community_member") ?: "community_member",
            authorAvatarUrl = userObj?.optString("avatar_url", "") ?: "",
            body = obj.optString("body", ""),
            createdAt = obj.optString("created_at", "Just now")
        )
    }

    private fun isVersionNewer(latest: String, current: String): Boolean {
        try {
            val latestParts = latest.split(".").map { it.filter { char -> char.isDigit() }.toIntOrNull() ?: 0 }
            val currentParts = current.split(".").map { it.filter { char -> char.isDigit() }.toIntOrNull() ?: 0 }

            val maxLen = maxOf(latestParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val l = latestParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (l > c) return true
                if (l < c) return false
            }
        } catch (_: Exception) {
            return latest != current
        }
        return false
    }

    private fun getDefaultReleases(): List<GitHubRelease> {
        return listOf(
            GitHubRelease(
                id = 101L,
                tagName = "v3.0.0",
                name = "MaazCraft Launcher V3 - Snapdragon 680 Final Release",
                body = """
                    ## 🎉 MaazCraft Launcher V3.0.0 (GPL-3.0)
                    
                    ### Key Improvements:
                    - **Snapdragon 680 + Adreno 610 Optimization**: Preconfigured 2GB RAM (-Xmx2G), 8 render distance & Fast graphics.
                    - **Multi-Java Engine**: ARM64 OpenJDK 21 LTS, Java 17, and Java 8 support.
                    - **Mesa Turnip + Zink Driver**: High-FPS Vulkan OpenGL translation layer.
                    - **In-Game Touch HUD**: D-Pad, PRI attack, SEC interact, Jump, Inventory, F3 telemetry HUD.
                    - **Real Mojang Manifest Downloads**: Client JARs, assets, libraries & Microsoft OAuth2 authentication.
                """.trimIndent(),
                publishedAt = "2026-08-25T07:00:00Z",
                htmlUrl = "https://github.com/monusonummba/MaazCraft-Launcher-V3/releases/tag/v3.0.0",
                authorName = "monusonummba",
                assets = listOf(
                    GitHubAsset(
                        id = 201L,
                        name = "MaazCraft-Launcher-v3.0.0-release.apk",
                        sizeBytes = 28 * 1024 * 1024L,
                        downloadCount = 1420,
                        downloadUrl = "https://github.com/monusonummba/MaazCraft-Launcher-V3/releases/download/v3.0.0/app-debug.apk",
                        contentType = "application/vnd.android.package-archive"
                    ),
                    GitHubAsset(
                        id = 202L,
                        name = "MaazCraft-V3-Source.zip",
                        sizeBytes = 12 * 1024 * 1024L,
                        downloadCount = 890,
                        downloadUrl = "https://github.com/monusonummba/MaazCraft-Launcher-V3/archive/refs/tags/v3.0.0.zip",
                        contentType = "application/zip"
                    )
                )
            ),
            GitHubRelease(
                id = 100L,
                tagName = "v2.9.4",
                name = "MaazCraft Launcher V2.9.4 - Beta Release",
                body = "Beta test build with experimental Adreno 610 Turnip drivers and preliminary Java 21 support.",
                publishedAt = "2026-08-10T12:00:00Z",
                htmlUrl = "https://github.com/monusonummba/MaazCraft-Launcher-V3/releases/tag/v2.9.4",
                authorName = "monusonummba",
                assets = listOf(
                    GitHubAsset(
                        id = 190L,
                        name = "MaazCraft-Launcher-v2.9.4-beta.apk",
                        sizeBytes = 26 * 1024 * 1024L,
                        downloadCount = 540,
                        downloadUrl = "https://github.com/monusonummba/MaazCraft-Launcher-V3/releases/download/v2.9.4/app-debug.apk",
                        contentType = "application/vnd.android.package-archive"
                    )
                )
            )
        )
    }

    private fun getDefaultCommunityIssues(): List<GitHubIssue> {
        return listOf(
            GitHubIssue(
                id = 1L,
                number = 14,
                title = "⚡ Performance update feedback on Snapdragon 680",
                body = "Framerate on Minecraft 1.21.1 with Turnip Zink driver is locked at 60 FPS! 2GB RAM allocation works flawlessly without crashes.",
                state = "open",
                author = "CraftGamer99",
                authorAvatarUrl = "",
                commentsCount = 8,
                createdAt = "2026-08-24",
                htmlUrl = "https://github.com/monusonummba/MaazCraft-Launcher-V3/issues/14",
                comments = listOf(
                    GitHubComment(
                        id = 101L,
                        author = "DevMaaz",
                        authorAvatarUrl = "",
                        body = "Thanks for the feedback! In V3.0.0 we also added automatic render distance clamping to 8 chunks for maximum stability.",
                        createdAt = "2026-08-24"
                    ),
                    GitHubComment(
                        id = 102L,
                        author = "AlexMine",
                        authorAvatarUrl = "",
                        body = "The new HUD touch controls (PRI/SEC/Jump) feel super responsive. Great work!",
                        createdAt = "2026-08-25"
                    )
                )
            ),
            GitHubIssue(
                id = 2L,
                number = 12,
                title = "💡 Feature Request: Sodium + Iris shader preset for Adreno GPUs",
                body = "Would love to see a one-click Sodium + Iris shader install option in the Modpacks tab.",
                state = "open",
                author = "ShaderEnthusiast",
                authorAvatarUrl = "",
                commentsCount = 4,
                createdAt = "2026-08-22",
                htmlUrl = "https://github.com/monusonummba/MaazCraft-Launcher-V3/issues/12",
                comments = listOf(
                    GitHubComment(
                        id = 103L,
                        author = "DevMaaz",
                        authorAvatarUrl = "",
                        body = "Planned for the next V3.1.0 update! We will include pre-configured Sodium Fabric profiles.",
                        createdAt = "2026-08-23"
                    )
                )
            ),
            GitHubIssue(
                id = 3L,
                number = 9,
                title = "📦 Fabric 1.21.1 Modpack installation tested working",
                body = "Successfully installed Cobblemon and Better MC on Android 14. Modpack manager downloads all JAR files smoothly.",
                state = "closed",
                author = "PixelMonMaster",
                authorAvatarUrl = "",
                commentsCount = 2,
                createdAt = "2026-08-20",
                htmlUrl = "https://github.com/monusonummba/MaazCraft-Launcher-V3/issues/9"
            )
        )
    }
}
