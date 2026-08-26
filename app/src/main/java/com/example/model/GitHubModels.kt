// FILE: app/src/main/java/com/example/model/GitHubModels.kt
package com.example.model

/**
 * GitHub release model.
 */
data class GitHubRelease(
    val id: Long,
    val tagName: String,
    val name: String,
    val body: String,
    val publishedAt: String,
    val htmlUrl: String,
    val isPrerelease: Boolean = false,
    val isDraft: Boolean = false,
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val assets: List<GitHubAsset> = emptyList()
)

/**
 * GitHub release asset binary (e.g., APK, ZIP).
 */
data class GitHubAsset(
    val id: Long,
    val name: String,
    val sizeBytes: Long,
    val downloadCount: Int,
    val downloadUrl: String,
    val contentType: String
) {
    val sizeMb: Double
        get() = sizeBytes / (1024.0 * 1024.0)

    val isApk: Boolean
        get() = name.endsWith(".apk", ignoreCase = true)
}

/**
 * GitHub Issue / Discussion comment model for community engagement.
 */
data class GitHubIssue(
    val id: Long,
    val number: Int,
    val title: String,
    val body: String,
    val state: String, // "open" or "closed"
    val author: String,
    val authorAvatarUrl: String,
    val commentsCount: Int,
    val createdAt: String,
    val htmlUrl: String,
    val comments: List<GitHubComment> = emptyList()
)

/**
 * Single user comment on a GitHub issue or release.
 */
data class GitHubComment(
    val id: Long,
    val author: String,
    val authorAvatarUrl: String,
    val body: String,
    val createdAt: String
)

/**
 * GitHub configuration for the repository manager.
 */
data class GitHubRepoConfig(
    val owner: String = "monusonummba",
    val repo: String = "MaazCraft-Launcher-V3",
    val personalAccessToken: String = "",
    val autoCheckUpdates: Boolean = true,
    val lastCheckedTimestamp: Long = 0L
)

/**
 * Status response for OTA update checking.
 */
data class UpdateCheckResult(
    val hasUpdate: Boolean,
    val currentVersion: String,
    val latestVersion: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val apkAsset: GitHubAsset?,
    val releaseUrl: String
)

/**
 * Progress tracker for publishing releases & uploading APK assets.
 */
data class ReleasePublishStatus(
    val step: String = "",
    val progressPercent: Int = 0,
    val isSuccess: Boolean = false,
    val isError: Boolean = false,
    val message: String = "",
    val releaseUrl: String? = null
)
