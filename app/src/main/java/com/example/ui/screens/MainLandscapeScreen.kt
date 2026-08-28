// FILE: app/src/main/java/com/example/ui/screens/MainLandscapeScreen.kt
package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.CrashAnalyzer
import com.example.core.DeviceProfileDetector
import com.example.core.GitHubManagerService
import com.example.core.LaunchHelper
import com.example.core.LaunchState
import com.example.core.ModpackManager
import com.example.core.PreferenceManager
import com.example.core.SkinManager
import com.example.core.VersionManager
import com.example.model.Account
import com.example.model.CrashDiagnostic
import com.example.model.DeviceProfile
import com.example.model.MinecraftVersion
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanInfo
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurpleContainer
import com.example.ui.theme.PurpleDarkBorder
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

/**
 * 3-Column Landscape UI for MaazCraft Launcher:
 * - Left Sidebar [20%]: Navigation Rail with Logo & Main Sections
 * - Center Panel [55%]: Interactive Dashboard / Content View
 * - Right Panel [25%]: Real-Time Hardware Info, Active Account + Skin, 300FPS Tuner & Crash Card
 */
@Composable
fun MainLandscapeScreen(
    snackbarHostState: SnackbarHostState,
    onOpenWizard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { PreferenceManager(context) }
    val launchHelper = remember { LaunchHelper(context) }
    val versionManager = remember { VersionManager(context) }
    val modpackManager = remember { ModpackManager(context) }
    val gitHubService = remember { GitHubManagerService(context) }
    val crashAnalyzer = remember { CrashAnalyzer(context) }

    var activeNavTab by remember { mutableStateOf("play") } // play, versions, mods, accounts, skins, settings, crashes, github
    val launchState by launchHelper.launchState.collectAsState()
    var isInGameScreen by remember { mutableStateOf(false) }

    var deviceProfile by remember { mutableStateOf<DeviceProfile?>(null) }
    var detectedCrash by remember { mutableStateOf<CrashDiagnostic?>(null) }
    var isCheckingCrash by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        deviceProfile = DeviceProfileDetector.detect(context)
        detectedCrash = crashAnalyzer.analyzeLatestCrash()
    }

    val activeVersion = MinecraftVersion(
        id = prefs.selectedVersionId,
        type = "release",
        url = "https://piston-meta.mojang.com/v1/packages/${prefs.selectedVersionId}.json",
        releaseTime = "2024",
        javaVersion = if (prefs.selectedVersionId.startsWith("1.21") || prefs.selectedVersionId.startsWith("1.20.5")) 21 else 17,
        recommendedJava = if (prefs.selectedVersionId.startsWith("1.21") || prefs.selectedVersionId.startsWith("1.20.5")) "Java 21" else "Java 17",
        description = "Minecraft Official Release",
        modLoader = prefs.selectedModLoader
    )
    val activeAccount = Account(
        id = "acc_active",
        username = prefs.accountUsername,
        uuid = prefs.accountUuid,
        isMicrosoft = prefs.accountType == "Microsoft"
    )

    val onLaunchGameAction = {
        isInGameScreen = true
        launchHelper.launchGame(activeVersion, activeAccount, scope) {
            scope.launch {
                snackbarHostState.showSnackbar("Minecraft ${activeVersion.id} started with 300 FPS profile!")
            }
        }
    }

    if (isInGameScreen) {
        MinecraftGameScreen(
            version = activeVersion,
            account = activeAccount,
            launchHelper = launchHelper,
            onExitGame = {
                launchHelper.stopGame()
                isInGameScreen = false
            }
        )
    } else {
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundDark)
        ) {
        // =========================================================================
        // 1. LEFT SIDEBAR [~19%]: Navigation Rail
        // =========================================================================
        LeftSidebar(
            activeTab = activeNavTab,
            onTabSelected = { activeNavTab = it },
            onOpenWizard = onOpenWizard,
            modifier = Modifier
                .weight(0.19f)
                .fillMaxHeight()
        )

        // =========================================================================
        // 2. CENTER PANEL [~56%]: Dynamic Active Screen Content
        // =========================================================================
        Box(
            modifier = Modifier
                .weight(0.56f)
                .fillMaxHeight()
                .padding(top = 10.dp, bottom = 10.dp, end = 8.dp)
        ) {
            when (activeNavTab) {
                "play" -> DashboardCenterContent(
                    launchState = launchState,
                    prefs = prefs,
                    onLaunch = { onLaunchGameAction() },
                    onStop = { launchHelper.stopGame() },
                    onNavigateTo = { activeNavTab = it }
                )
                "versions" -> VersionsScreen(
                    versionManager = versionManager,
                    onVersionSelected = { verId ->
                        prefs.selectedVersionId = verId
                        activeNavTab = "play"
                        scope.launch {
                            snackbarHostState.showSnackbar("Active version set to Minecraft $verId")
                        }
                    }
                )
                "mods" -> ModpacksScreen(
                    modpackManager = modpackManager,
                    onModpackSelected = { pack ->
                        prefs.selectedVersionId = pack.version
                        prefs.selectedModLoader = pack.loader
                        activeNavTab = "play"
                        scope.launch {
                            snackbarHostState.showSnackbar("Loaded modpack ${pack.name} (${pack.loader})")
                        }
                    }
                )
                "accounts" -> AccountsScreen(snackbarHostState = snackbarHostState)
                "skins" -> SkinsScreen(snackbarHostState = snackbarHostState)
                "settings" -> SettingsScreen(onReOptimize = onOpenWizard)
                "crashes" -> CrashAnalyzerContent(
                    crash = detectedCrash,
                    onAutoFix = { diag ->
                        scope.launch {
                            val res = crashAnalyzer.applyAutoFix(diag)
                            snackbarHostState.showSnackbar(res.getOrDefault("Auto-Fix applied!"))
                            detectedCrash = null
                        }
                    },
                    onRefresh = {
                        scope.launch {
                            isCheckingCrash = true
                            detectedCrash = crashAnalyzer.analyzeLatestCrash()
                            isCheckingCrash = false
                            snackbarHostState.showSnackbar("Crash log analysis complete.")
                        }
                    }
                )
                "github" -> GitHubScreen(gitHubService = gitHubService)
                else -> DashboardCenterContent(
                    launchState = launchState,
                    prefs = prefs,
                    onLaunch = { onLaunchGameAction() },
                    onStop = { launchHelper.stopGame() },
                    onNavigateTo = { activeNavTab = it }
                )
            }
        }

        // =========================================================================
        // 3. RIGHT PANEL [~25%]: Hardware Info, Active Account + Skin, Performance HUD
        // =========================================================================
        RightInfoPanel(
            profile = deviceProfile,
            prefs = prefs,
            crash = detectedCrash,
            onSwitchAccount = { activeNavTab = "accounts" },
            onSwitchSkin = { activeNavTab = "skins" },
            onOpenCrashFix = { activeNavTab = "crashes" },
            modifier = Modifier
                .weight(0.25f)
                .fillMaxHeight()
                .padding(top = 10.dp, bottom = 10.dp, end = 10.dp)
        )
    }
    }
}

@Composable
private fun LeftSidebar(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    onOpenWizard: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = SurfaceDark,
        border = BorderStroke(1.dp, PurpleDarkBorder.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Brand Obsidian Logo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(PurplePrimary, PurpleAccent))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "MAAZCRAFT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = PurpleAccent,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "V3.3 PRO",
                    fontSize = 9.sp,
                    color = CyanInfo,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Navigation Items List
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                NavTabItem("play", "Play", Icons.Default.PlayArrow, activeTab == "play") { onTabSelected("play") }
                NavTabItem("versions", "Versions", Icons.Default.Widgets, activeTab == "versions") { onTabSelected("versions") }
                NavTabItem("mods", "Modpacks", Icons.Default.FolderZip, activeTab == "mods") { onTabSelected("mods") }
                NavTabItem("accounts", "Accounts", Icons.Default.Person, activeTab == "accounts") { onTabSelected("accounts") }
                NavTabItem("skins", "3D Skins", Icons.Default.Face, activeTab == "skins") { onTabSelected("skins") }
                NavTabItem("settings", "300 FPS Settings", Icons.Default.Tune, activeTab == "settings") { onTabSelected("settings") }
                NavTabItem("crashes", "Crash Auto-Fix", Icons.Default.BugReport, activeTab == "crashes") { onTabSelected("crashes") }
                NavTabItem("github", "Releases", Icons.Default.CloudDownload, activeTab == "github") { onTabSelected("github") }
            }

            // Bottom Auto-Optimizer shortcut
            Button(
                onClick = onOpenWizard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurpleContainer),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanInfo, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Optimizer", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }
    }
}

@Composable
private fun NavTabItem(
    id: String,
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) PurplePrimary else Color.Transparent,
        label = "navBg"
    )
    val contentColor = if (isSelected) Color.White else TextMuted

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(17.dp)
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DashboardCenterContent(
    launchState: LaunchState,
    prefs: PreferenceManager,
    onLaunch: () -> Unit,
    onStop: () -> Unit,
    onNavigateTo: (String) -> Unit
) {
    val isRunning = launchState == LaunchState.RUNNING
    val isPreparing = launchState == LaunchState.PREPARING || launchState == LaunchState.LAUNCHING

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Brush.linearGradient(listOf(PurpleDarkBorder, PurplePrimary)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Hero Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, PurpleDarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Active Profile:", fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PurpleContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(prefs.selectedModLoader, fontSize = 11.sp, color = CyanInfo, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            text = "Minecraft ${prefs.selectedVersionId}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }

                    Button(
                        onClick = { onNavigateTo("versions") },
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleContainer),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Change Version", fontSize = 11.sp, color = TextPrimary)
                    }
                }
            }

            // Quick Shortcut Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickTile("Skins 3D", Icons.Default.Face, "Change Look", Modifier.weight(1f)) { onNavigateTo("skins") }
                QuickTile("Modpacks", Icons.Default.FolderZip, "Sodium / Iris", Modifier.weight(1f)) { onNavigateTo("mods") }
                QuickTile("300 FPS", Icons.Default.Speed, "Auto-Tuned", Modifier.weight(1f)) { onNavigateTo("settings") }
                QuickTile("Accounts", Icons.Default.Person, prefs.accountUsername, Modifier.weight(1f)) { onNavigateTo("accounts") }
            }

            // Big Glowing PLAY Button
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isPreparing) {
                    Row(
                        modifier = Modifier.padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CyanInfo, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Preparing Java 21 environment & native assets...", fontSize = 11.sp, color = CyanInfo)
                    }
                }

                Button(
                    onClick = { if (isRunning) onStop() else onLaunch() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(10.dp, RoundedCornerShape(14.dp), spotColor = if (isRunning) ErrorRed else PurpleAccent),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) ErrorRed else PurplePrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isPreparing
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isRunning) "TERMINATE GAME" else "PLAY MINECRAFT JAVA",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickTile(
    title: String,
    icon: ImageVector,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, PurpleDarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = subtitle, fontSize = 9.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun RightInfoPanel(
    profile: DeviceProfile?,
    prefs: PreferenceManager,
    crash: CrashDiagnostic?,
    onSwitchAccount: () -> Unit,
    onSwitchSkin: () -> Unit,
    onOpenCrashFix: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = SurfaceDark,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, PurpleDarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Active Account & Skin
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, PurpleDarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(PurplePrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = prefs.accountUsername,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${prefs.accountType} • 3D Skin Active",
                            fontSize = 10.sp,
                            color = CyanInfo
                        )
                    }
                    IconButton(
                        onClick = onSwitchAccount,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(15.dp))
                    }
                }
            }

            // Card 2: Hardware & 300FPS Performance
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, PurpleDarkBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Memory, contentDescription = null, tint = CyanInfo, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mobile Hardware", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SuccessGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("300 FPS TUNED", fontSize = 8.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                        }
                    }

                    profile?.let { p ->
                        Text("SoC: ${p.socName}", fontSize = 10.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("RAM: ${prefs.allocatedRamMb} MB / ${p.totalRamMb} MB", fontSize = 10.sp, color = TextSecondary)
                        Text("Renderer: ${prefs.selectedDriverId}", fontSize = 10.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            // Card 3: Crash Status Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenCrashFix() },
                colors = CardDefaults.cardColors(
                    containerColor = if (crash != null) ErrorRed.copy(alpha = 0.15f) else SuccessGreen.copy(alpha = 0.12f)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (crash != null) ErrorRed else SuccessGreen.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (crash != null) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (crash != null) ErrorRed else SuccessGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (crash != null) "Crash Detected!" else "System Stable",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (crash != null) ErrorRed else SuccessGreen
                        )
                        Text(
                            text = if (crash != null) "Tap to auto-fix with 1 click" else "0 JVM or GPU faults",
                            fontSize = 9.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CrashAnalyzerContent(
    crash: CrashDiagnostic?,
    onAutoFix: (CrashDiagnostic) -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, PurpleDarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BugReport, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Crash Detector & Auto-Fix", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Scan", tint = PurpleAccent)
                    }
                }

                if (crash == null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Crashes Detected", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Latest.log is clean. Minecraft is running without errors.", fontSize = 12.sp, color = TextMuted)
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ErrorRed)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(crash.errorType, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                            Text(crash.summary, fontSize = 12.sp, color = TextPrimary)
                            Text("Root Cause: ${crash.rootCause}", fontSize = 11.sp, color = TextSecondary)
                            Text("Solution: ${crash.suggestedFix}", fontSize = 11.sp, color = CyanInfo)
                        }
                    }
                }
            }

            if (crash != null) {
                Button(
                    onClick = { onAutoFix(crash) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("APPLY 1-CLICK AUTO FIX", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.Black)
                }
            }
        }
    }
}
