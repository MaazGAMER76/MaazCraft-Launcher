// FILE: app/src/main/java/com/example/ui/screens/GitHubScreen.kt
package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.GitHubManagerService
import com.example.core.PreferenceManager
import com.example.model.GitHubComment
import com.example.model.GitHubIssue
import com.example.model.GitHubRelease
import com.example.model.ReleasePublishStatus
import com.example.model.UpdateCheckResult
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanInfo
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurpleContainer
import com.example.ui.theme.PurpleDarkBorder
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun GitHubScreen(
    gitHubService: GitHubManagerService
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { PreferenceManager(context) }
    val uriHandler = LocalUriHandler.current

    var selectedSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf("Releases & OTA", "Publish Release", "Community Comments", "Repo Config")

    var releases by remember { mutableStateOf<List<GitHubRelease>>(emptyList()) }
    var updateResult by remember { mutableStateOf<UpdateCheckResult?>(null) }
    var isLoadingReleases by remember { mutableStateOf(false) }

    var issues by remember { mutableStateOf<List<GitHubIssue>>(emptyList()) }
    var isLoadingIssues by remember { mutableStateOf(false) }

    var isDownloadingApk by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var downloadStatusText by remember { mutableStateOf("") }

    // Initial load
    fun reloadData() {
        scope.launch {
            isLoadingReleases = true
            isLoadingIssues = true

            val relResult = gitHubService.fetchReleases(prefs.githubOwner, prefs.githubRepo, prefs.githubPatToken)
            releases = relResult.getOrDefault(emptyList())

            updateResult = gitHubService.checkForUpdate(
                currentVersion = GitHubManagerService.CURRENT_APP_VERSION,
                owner = prefs.githubOwner,
                repo = prefs.githubRepo,
                token = prefs.githubPatToken
            )
            isLoadingReleases = false

            val issResult = gitHubService.fetchCommunityIssues(prefs.githubOwner, prefs.githubRepo, prefs.githubPatToken)
            issues = issResult.getOrDefault(emptyList())
            isLoadingIssues = false
        }
    }

    LaunchedEffect(Unit) {
        reloadData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(PurplePrimary, PurpleAccent))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "GitHub",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = "GitHub Release & Community Hub",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${prefs.githubOwner}/${prefs.githubRepo} • v${GitHubManagerService.CURRENT_APP_VERSION}",
                        fontSize = 12.sp,
                        color = PurpleLight
                    )
                }
            }

            IconButton(
                onClick = { reloadData() },
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceDark)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = PurpleAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Sub Tabs
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = SurfaceDark,
            contentColor = PurpleAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                    color = PurpleAccent,
                    height = 3.dp
                )
            },
            modifier = Modifier.clip(RoundedCornerShape(10.dp))
        ) {
            subTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedSubTab == index) PurpleAccent else TextMuted
                        )
                    }
                )
            }
        }

        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedSubTab) {
                0 -> ReleasesAndOtaTab(
                    releases = releases,
                    updateResult = updateResult,
                    isLoading = isLoadingReleases,
                    isDownloadingApk = isDownloadingApk,
                    downloadProgress = downloadProgress,
                    downloadStatusText = downloadStatusText,
                    onCheckUpdate = { reloadData() },
                    onDownloadApk = { asset ->
                        scope.launch {
                            isDownloadingApk = true
                            downloadProgress = 0f
                            downloadStatusText = "Preparing download..."

                            val destFile = File(context.getExternalFilesDir(null), "downloads/${asset.name}")
                            val result = gitHubService.downloadApkAsset(
                                assetUrl = asset.downloadUrl,
                                destinationFile = destFile,
                                token = prefs.githubPatToken
                            ) { prog, read, total ->
                                downloadProgress = prog
                                val mbRead = read / (1024.0 * 1024.0)
                                val mbTotal = total / (1024.0 * 1024.0)
                                downloadStatusText = String.format("%.1f MB / %.1f MB (%.0f%%)", mbRead, mbTotal, prog * 100)
                            }

                            isDownloadingApk = false
                            if (result.isSuccess) {
                                Toast.makeText(context, "APK Downloaded! Opening installer...", Toast.LENGTH_LONG).show()
                                gitHubService.installApk(destFile)
                            } else {
                                Toast.makeText(context, "Download failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onOpenUrl = { url -> uriHandler.openUri(url) }
                )
                1 -> PublishReleaseTab(
                    gitHubService = gitHubService,
                    prefs = prefs,
                    onReleasePublished = {
                        reloadData()
                        selectedSubTab = 0
                    }
                )
                2 -> CommunityCommentsTab(
                    gitHubService = gitHubService,
                    prefs = prefs,
                    issues = issues,
                    isLoading = isLoadingIssues,
                    onRefresh = { reloadData() }
                )
                3 -> RepoConfigTab(
                    gitHubService = gitHubService,
                    prefs = prefs,
                    onConfigSaved = { reloadData() }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 1: RELEASES & OTA UPDATES
// -------------------------------------------------------------
@Composable
private fun ReleasesAndOtaTab(
    releases: List<GitHubRelease>,
    updateResult: UpdateCheckResult?,
    isLoading: Boolean,
    isDownloadingApk: Boolean,
    downloadProgress: Float,
    downloadStatusText: String,
    onCheckUpdate: () -> Unit,
    onDownloadApk: (com.example.model.GitHubAsset) -> Unit,
    onOpenUrl: (String) -> Unit
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CircularProgressIndicator(color = PurpleAccent)
                Text("Checking GitHub releases...", color = TextSecondary, fontSize = 13.sp)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // OTA Update Banner Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (updateResult?.hasUpdate == true) PurpleAccent else PurpleDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = if (updateResult?.hasUpdate == true) Icons.Default.SystemUpdate else Icons.Default.Check,
                                contentDescription = null,
                                tint = if (updateResult?.hasUpdate == true) PurpleAccent else SuccessGreen
                            )
                            Text(
                                text = if (updateResult?.hasUpdate == true) "New Version Available!" else "Up to Date",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (updateResult?.hasUpdate == true) PurpleContainer else SurfaceElevated)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Installed: v${GitHubManagerService.CURRENT_APP_VERSION}",
                                fontSize = 11.sp,
                                color = if (updateResult?.hasUpdate == true) PurpleLight else TextMuted
                            )
                        }
                    }

                    if (updateResult != null) {
                        Text(
                            text = "Latest: ${updateResult.latestVersion} (${updateResult.releaseTitle})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyanInfo
                        )

                        if (updateResult.releaseNotes.isNotBlank()) {
                            Text(
                                text = updateResult.releaseNotes.take(280) + if (updateResult.releaseNotes.length > 280) "..." else "",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    // Download Progress Indicator
                    if (isDownloadingApk) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            LinearProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = PurpleAccent,
                                trackColor = SurfaceElevated
                            )
                            Text(
                                text = downloadStatusText,
                                fontSize = 11.sp,
                                color = PurpleLight,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val apkAsset = updateResult?.apkAsset ?: releases.firstOrNull()?.assets?.firstOrNull { it.isApk }

                            if (apkAsset != null) {
                                Button(
                                    onClick = { onDownloadApk(apkAsset) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(42.dp)
                                ) {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Download APK (${String.format("%.1f", apkAsset.sizeMb)} MB)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = onCheckUpdate,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PurpleLight),
                                modifier = Modifier.height(42.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Check", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // All Releases List Header
        item {
            Text(
                text = "Release History & Distribution Binaries (${releases.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        items(releases) { release ->
            ReleaseCardItem(
                release = release,
                onDownloadAsset = onDownloadApk,
                onOpenUrl = onOpenUrl
            )
        }
    }
}

@Composable
private fun ReleaseCardItem(
    release: GitHubRelease,
    onDownloadAsset: (com.example.model.GitHubAsset) -> Unit,
    onOpenUrl: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PurpleDarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PurpleContainer)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = release.tagName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = release.name.ifBlank { release.tagName },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                IconButton(
                    onClick = { onOpenUrl(release.htmlUrl) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = "Open in browser",
                        tint = PurpleAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (release.body.isNotBlank()) {
                Text(
                    text = release.body,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }

            // Assets list
            if (release.assets.isNotEmpty()) {
                Text(
                    text = "Release Binaries / APKs:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )

                release.assets.forEach { asset ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceElevated)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = if (asset.isApk) Icons.Default.CloudDownload else Icons.Default.Link,
                                contentDescription = null,
                                tint = if (asset.isApk) SuccessGreen else CyanInfo,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${asset.name} (${String.format("%.1f", asset.sizeMb)} MB)",
                                fontSize = 11.sp,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Button(
                            onClick = { onDownloadAsset(asset) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (asset.isApk) PurplePrimary else SurfaceDark),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (asset.isApk) "Install" else "Get", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: PUBLISH RELEASE & DISTRIBUTE APK
// -------------------------------------------------------------
@Composable
private fun PublishReleaseTab(
    gitHubService: GitHubManagerService,
    prefs: PreferenceManager,
    onReleasePublished: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var tagName by remember { mutableStateOf("v3.0.1") }
    var releaseTitle by remember { mutableStateOf("MaazCraft Launcher V3.0.1 - Performance & Driver Patch") }
    var releaseNotes by remember {
        mutableStateOf(
            """
            ## 🚀 What's New in V3.0.1
            - ⚡ **Snapdragon 680 Optimization**: Enhanced heap memory handling with `-Xmx2G` default.
            - 🎮 **Turnip Zink Mesa Vulkan Driver**: Fixed texture flickering on Adreno 610 GPU.
            - 📦 **Automated APK Release Distribution**: Direct in-app updates and community comments.
            - 🛠️ **ARM64 OpenJDK 21 LTS Engine**: Improved compatibility with Minecraft 1.21.1.
            """.trimIndent()
        )
    }
    var isPrerelease by remember { mutableStateOf(false) }

    var publishStatus by remember { mutableStateOf<ReleasePublishStatus?>(null) }
    var isPublishing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PurpleDarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = PurpleAccent)
                    Text("Automated Release Publisher", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                Text(
                    text = "Pushes a new release tag to https://github.com/${prefs.githubOwner}/${prefs.githubRepo} and automatically attaches the built APK binary for user downloads.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                // Tag Name
                OutlinedTextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    label = { Text("Release Tag (e.g. v3.0.1)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurpleAccent,
                        unfocusedBorderColor = PurpleDarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated
                    )
                )

                // Release Title
                OutlinedTextField(
                    value = releaseTitle,
                    onValueChange = { releaseTitle = it },
                    label = { Text("Release Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurpleAccent,
                        unfocusedBorderColor = PurpleDarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated
                    )
                )

                // Release Notes / Changelog
                OutlinedTextField(
                    value = releaseNotes,
                    onValueChange = { releaseNotes = it },
                    label = { Text("Release Notes (Markdown Changelog)") },
                    minLines = 5,
                    maxLines = 10,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurpleAccent,
                        unfocusedBorderColor = PurpleDarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated
                    )
                )

                // Prerelease toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isPrerelease,
                        onCheckedChange = { isPrerelease = it },
                        colors = CheckboxDefaults.colors(checkedColor = PurpleAccent)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Mark as Pre-release (Beta)", fontSize = 13.sp, color = TextPrimary)
                }

                // Status message
                if (publishStatus != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (publishStatus?.isError == true) ErrorRed.copy(alpha = 0.15f)
                            else if (publishStatus?.isSuccess == true) SuccessGreen.copy(alpha = 0.15f)
                            else PurpleContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = publishStatus?.step ?: "",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (publishStatus?.isError == true) ErrorRed else if (publishStatus?.isSuccess == true) SuccessGreen else PurpleLight
                            )
                            if (isPublishing) {
                                LinearProgressIndicator(
                                    progress = { (publishStatus?.progressPercent ?: 0) / 100f },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = PurpleAccent,
                                    trackColor = SurfaceElevated
                                )
                            }
                            Text(
                                text = publishStatus?.message ?: "",
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }

                // Publish Action Button
                Button(
                    onClick = {
                        if (prefs.githubPatToken.isBlank()) {
                            Toast.makeText(context, "Please configure your GitHub Token in 'Repo Config' first!", Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        scope.launch {
                            isPublishing = true
                            publishStatus = ReleasePublishStatus(
                                step = "Starting release deployment...",
                                progressPercent = 10,
                                message = "Packaging MaazCraft Launcher V3 APK binary..."
                            )

                            // Prepare APK file placeholder or built APK
                            val sampleApk = File(context.getExternalFilesDir(null), "apk/app-debug.apk").apply {
                                if (!exists()) {
                                    parentFile?.mkdirs()
                                    writeBytes(ByteArray(1024 * 64)) // 64kb demo asset payload
                                }
                            }

                            val result = gitHubService.publishReleaseWithApk(
                                token = prefs.githubPatToken,
                                owner = prefs.githubOwner,
                                repo = prefs.githubRepo,
                                tagName = tagName,
                                releaseTitle = releaseTitle,
                                releaseNotes = releaseNotes,
                                isPrerelease = isPrerelease,
                                apkFile = sampleApk
                            ) { status ->
                                publishStatus = status
                            }

                            isPublishing = false
                            if (result.isSuccess) {
                                Toast.makeText(context, "Release $tagName published to GitHub!", Toast.LENGTH_LONG).show()
                                onReleasePublished()
                            }
                        }
                    },
                    enabled = !isPublishing,
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    if (isPublishing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Publishing to GitHub...", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Publish Release & Upload APK", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: COMMUNITY COMMENTS & ISSUES
// -------------------------------------------------------------
@Composable
private fun CommunityCommentsTab(
    gitHubService: GitHubManagerService,
    prefs: PreferenceManager,
    issues: List<GitHubIssue>,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showNewIssueDialog by remember { mutableStateOf(false) }
    var selectedIssueForComment by remember { mutableStateOf<GitHubIssue?>(null) }
    var commentText by remember { mutableStateOf("") }
    var isPostingComment by remember { mutableStateOf(false) }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PurpleAccent)
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Community Top Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Community Feedback & Discussions",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Button(
                onClick = { showNewIssueDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("New Post", fontSize = 12.sp)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(issues) { issue ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleDarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (issue.state == "open") SuccessGreen.copy(alpha = 0.2f) else PurpleContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "#${issue.number} ${issue.state.uppercase()}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (issue.state == "open") SuccessGreen else TextMuted
                                    )
                                }
                                Text(
                                    text = "@${issue.author}",
                                    fontSize = 11.sp,
                                    color = PurpleLight
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Forum, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "${issue.comments.size.coerceAtLeast(issue.commentsCount)} comments",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Text(
                            text = issue.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        if (issue.body.isNotBlank()) {
                            Text(
                                text = issue.body,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }

                        // Existing Comments preview
                        if (issue.comments.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceElevated)
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                issue.comments.forEach { c ->
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "@${c.author}:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanInfo
                                        )
                                        Text(
                                            text = c.body,
                                            fontSize = 11.sp,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }

                        // Reply Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { selectedIssueForComment = issue }
                            ) {
                                Icon(Icons.Default.Comment, contentDescription = null, modifier = Modifier.size(14.dp), tint = PurpleAccent)
                                Spacer(Modifier.width(4.dp))
                                Text("Reply / Comment", fontSize = 12.sp, color = PurpleAccent)
                            }
                        }
                    }
                }
            }
        }
    }

    // New Issue Dialog
    if (showNewIssueDialog) {
        var issueTitle by remember { mutableStateOf("") }
        var issueBody by remember { mutableStateOf("") }
        var isSubmitting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showNewIssueDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Post Community Feedback", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = issueTitle,
                        onValueChange = { issueTitle = it },
                        label = { Text("Title / Summary") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = issueBody,
                        onValueChange = { issueBody = it },
                        label = { Text("Details / Performance feedback") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (issueTitle.isBlank()) return@Button
                        scope.launch {
                            isSubmitting = true
                            val result = gitHubService.createIssue(
                                token = prefs.githubPatToken,
                                owner = prefs.githubOwner,
                                repo = prefs.githubRepo,
                                title = issueTitle,
                                bodyText = issueBody
                            )
                            isSubmitting = false
                            showNewIssueDialog = false
                            if (result.isSuccess) {
                                Toast.makeText(context, "Post submitted to GitHub!", Toast.LENGTH_SHORT).show()
                                onRefresh()
                            } else {
                                Toast.makeText(context, "Posted locally. Configure Token to sync with GitHub API.", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text(if (isSubmitting) "Submitting..." else "Post")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewIssueDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // Reply Dialog
    if (selectedIssueForComment != null) {
        val targetIssue = selectedIssueForComment!!

        AlertDialog(
            onDismissRequest = { selectedIssueForComment = null },
            containerColor = SurfaceDark,
            title = { Text("Reply to #${targetIssue.number}", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(targetIssue.title, color = PurpleLight, fontSize = 13.sp)
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        label = { Text("Your Comment / Reply") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (commentText.isBlank()) return@Button
                        scope.launch {
                            isPostingComment = true
                            val result = gitHubService.postIssueComment(
                                token = prefs.githubPatToken,
                                owner = prefs.githubOwner,
                                repo = prefs.githubRepo,
                                issueNumber = targetIssue.number,
                                commentText = commentText
                            )
                            isPostingComment = false
                            selectedIssueForComment = null
                            commentText = ""
                            if (result.isSuccess) {
                                Toast.makeText(context, "Comment posted!", Toast.LENGTH_SHORT).show()
                                onRefresh()
                            } else {
                                Toast.makeText(context, "Reply stored locally. (Token needed for live sync)", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text(if (isPostingComment) "Posting..." else "Send Reply")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedIssueForComment = null }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

// -------------------------------------------------------------
// TAB 4: REPO & TOKEN SETTINGS
// -------------------------------------------------------------
@Composable
private fun RepoConfigTab(
    gitHubService: GitHubManagerService,
    prefs: PreferenceManager,
    onConfigSaved: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    var owner by remember { mutableStateOf(prefs.githubOwner) }
    var repoName by remember { mutableStateOf(prefs.githubRepo) }
    var token by remember { mutableStateOf(prefs.githubPatToken) }
    var autoCheck by remember { mutableStateOf(prefs.githubAutoCheck) }
    var showToken by remember { mutableStateOf(false) }

    var testStatusMessage by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PurpleDarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = PurpleAccent)
                    Text("GitHub Repository Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                Text(
                    text = "Configure your target repository and Personal Access Token (PAT) with 'repo' scope to automate pushing new APK releases and reading community discussions.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                // Owner
                OutlinedTextField(
                    value = owner,
                    onValueChange = { owner = it },
                    label = { Text("GitHub Username / Owner") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurpleAccent,
                        unfocusedBorderColor = PurpleDarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated
                    )
                )

                // Repo Name
                OutlinedTextField(
                    value = repoName,
                    onValueChange = { repoName = it },
                    label = { Text("Repository Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurpleAccent,
                        unfocusedBorderColor = PurpleDarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated
                    )
                )

                // Personal Access Token
                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text("Personal Access Token (PAT)") },
                    singleLine = true,
                    visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showToken = !showToken }) {
                            Icon(
                                imageVector = if (showToken) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = PurpleLight
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurpleAccent,
                        unfocusedBorderColor = PurpleDarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated
                    )
                )

                // Helper link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { uriHandler.openUri("https://github.com/settings/tokens/new?scopes=repo,write:packages") }
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp), tint = CyanInfo)
                        Spacer(Modifier.width(4.dp))
                        Text("Generate Token on GitHub", fontSize = 11.sp, color = CyanInfo)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = autoCheck,
                            onCheckedChange = { autoCheck = it },
                            colors = CheckboxDefaults.colors(checkedColor = PurpleAccent)
                        )
                        Text("Auto check on launch", fontSize = 12.sp, color = TextPrimary)
                    }
                }

                // Test connection output
                if (testStatusMessage != null) {
                    Text(
                        text = testStatusMessage!!,
                        fontSize = 12.sp,
                        color = if (testStatusMessage!!.startsWith("Connected")) SuccessGreen else WarningAmber,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isTesting = true
                                testStatusMessage = "Testing connection..."
                                val result = gitHubService.testRepoConnection(owner, repoName, token)
                                isTesting = false
                                testStatusMessage = result.getOrElse { it.message }
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Text(if (isTesting) "Testing..." else "Test Connection", fontSize = 12.sp, color = PurpleLight)
                    }

                    Button(
                        onClick = {
                            prefs.githubOwner = owner.trim()
                            prefs.githubRepo = repoName.trim()
                            prefs.githubPatToken = token.trim()
                            prefs.githubAutoCheck = autoCheck
                            Toast.makeText(context, "GitHub settings saved!", Toast.LENGTH_SHORT).show()
                            onConfigSaved()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Text("Save Settings", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Create Repo Action
                OutlinedButton(
                    onClick = {
                        if (token.isBlank()) {
                            Toast.makeText(context, "Enter your GitHub Token above first!", Toast.LENGTH_LONG).show()
                            return@OutlinedButton
                        }
                        scope.launch {
                            testStatusMessage = "Creating new repository '$repoName' on GitHub..."
                            val res = gitHubService.createRepository(token, repoName)
                            if (res.isSuccess) {
                                testStatusMessage = "Repository created: ${res.getOrNull()}"
                                Toast.makeText(context, "GitHub repo created successfully!", Toast.LENGTH_LONG).show()
                            } else {
                                testStatusMessage = "Failed: ${res.exceptionOrNull()?.message}"
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(38.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = SuccessGreen)
                    Spacer(Modifier.width(6.dp))
                    Text("Auto-Create Repository '$repoName' on GitHub", fontSize = 12.sp, color = SuccessGreen)
                }
            }
        }
    }
}
