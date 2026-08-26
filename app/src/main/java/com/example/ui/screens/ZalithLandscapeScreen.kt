// FILE: app/src/main/java/com/example/ui/screens/ZalithLandscapeScreen.kt
package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.DeviceProfileDetector
import com.example.core.GitHubManagerService
import com.example.core.LaunchHelper
import com.example.core.LaunchState
import com.example.core.ModpackManager
import com.example.core.PreferenceManager
import com.example.core.ServerManager
import com.example.core.VersionManager
import com.example.model.Account
import com.example.model.DeviceProfile
import com.example.model.MinecraftVersion
import com.example.model.Modpack
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
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.launch

enum class ZalithNavSection(val title: String, val icon: ImageVector, val subtitle: String) {
    DASHBOARD("Dashboard", Icons.Default.SportsEsports, "Launcher Hub"),
    VERSIONS("Versions", Icons.Default.VideogameAsset, "Vanilla & OptiFine"),
    MODPACKS("Modpacks", Icons.Default.Widgets, "Fabric & Forge"),
    DRIVERS("Renderer/GPU", Icons.Default.Speed, "Turnip, Zink & ANGLE"),
    CONSOLE("Live Console", Icons.Default.Terminal, "Process Output"),
    SETTINGS("Optimizer", Icons.Default.Tune, "RAM & Java Config")
}

@Composable
fun ZalithLandscapeScreen(
    launchHelper: LaunchHelper,
    versionManager: VersionManager,
    modpackManager: ModpackManager,
    serverManager: ServerManager,
    gitHubService: GitHubManagerService,
    snackbarHostState: SnackbarHostState,
    onOpenWizard: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { PreferenceManager(context) }
    val deviceProfile = remember { DeviceProfileDetector.detect(context) }

    var activeSection by remember { mutableStateOf(ZalithNavSection.DASHBOARD) }
    val launchState by launchHelper.launchState.collectAsState()

    // Persistent Landscape Layout: Left Sidebar + Right Main Content Pane
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // -------------------------------------------------------------
        // PERSISTENT LEFT SIDEBAR (Zalith Launcher Aesthetic)
        // -------------------------------------------------------------
        ZalithSidebar(
            activeSection = activeSection,
            onSectionSelected = { activeSection = it },
            deviceProfile = deviceProfile,
            launchState = launchState,
            allocatedRamMb = prefs.allocatedRamMb,
            onOpenWizard = onOpenWizard,
            modifier = Modifier
                .width(220.dp)
                .fillMaxHeight()
        )

        // Subtle vertical divider line
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        listOf(PurpleAccent.copy(alpha = 0.6f), PurpleDarkBorder, Color(0xFF1E1E1E))
                    )
                )
        )

        // -------------------------------------------------------------
        // RIGHT MAIN CONTENT WORKSPACE
        // -------------------------------------------------------------
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(BackgroundDark)
                .padding(12.dp)
        ) {
            when (activeSection) {
                ZalithNavSection.DASHBOARD -> {
                    ZalithDashboardPane(
                        launchHelper = launchHelper,
                        versionManager = versionManager,
                        deviceProfile = deviceProfile,
                        prefs = prefs,
                        onNavigateToVersions = { activeSection = ZalithNavSection.VERSIONS },
                        onNavigateToModpacks = { activeSection = ZalithNavSection.MODPACKS },
                        onNavigateToConsole = { activeSection = ZalithNavSection.CONSOLE },
                        onOpenWizard = onOpenWizard
                    )
                }
                ZalithNavSection.VERSIONS -> {
                    ZalithVersionsPane(
                        versionManager = versionManager,
                        prefs = prefs,
                        onVersionSelected = { verId ->
                            prefs.selectedVersionId = verId
                            scope.launch {
                                snackbarHostState.showSnackbar("Target Minecraft set to $verId")
                            }
                            activeSection = ZalithNavSection.DASHBOARD
                        }
                    )
                }
                ZalithNavSection.MODPACKS -> {
                    ZalithModpacksPane(
                        modpackManager = modpackManager,
                        onModpackSelected = { pack ->
                            prefs.selectedVersionId = pack.version
                            scope.launch {
                                snackbarHostState.showSnackbar("Loaded Modpack: ${pack.name}")
                            }
                            activeSection = ZalithNavSection.DASHBOARD
                        }
                    )
                }
                ZalithNavSection.DRIVERS -> {
                    ZalithDriversPane(
                        prefs = prefs,
                        deviceProfile = deviceProfile,
                        onDriverSelected = { driverId ->
                            prefs.selectedDriverId = driverId
                            scope.launch {
                                snackbarHostState.showSnackbar("Active Driver Profile: $driverId")
                            }
                        }
                    )
                }
                ZalithNavSection.CONSOLE -> {
                    ZalithConsolePane(launchHelper = launchHelper)
                }
                ZalithNavSection.SETTINGS -> {
                    ZalithSettingsPane(
                        prefs = prefs,
                        deviceProfile = deviceProfile,
                        onReOptimize = onOpenWizard
                    )
                }
            }
        }
    }
}

// =====================================================================
// PERSISTENT SIDEBAR COMPONENT
// =====================================================================
@Composable
private fun ZalithSidebar(
    activeSection: ZalithNavSection,
    onSectionSelected: (ZalithNavSection) -> Unit,
    deviceProfile: DeviceProfile,
    launchState: LaunchState,
    allocatedRamMb: Int,
    onOpenWizard: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = SurfaceDark,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Branding & Device Tag
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, PurpleAccent, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_icon),
                            contentDescription = "MaazCraft V3 App Icon",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "MAAZCRAFT",
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "ZALITH ENGINE V3",
                            color = PurpleLight,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Hardware quick badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenWizard() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (launchState == LaunchState.RUNNING) SuccessGreen else CyanInfo)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (deviceProfile.isSnapdragon680) "Redmi Note 11 (SD680)" else deviceProfile.socName.take(20),
                                color = TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${allocatedRamMb}MB RAM • ${deviceProfile.gpuVendor}",
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFF2B2B2B), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // Navigation Items List
                ZalithNavSection.entries.forEach { section ->
                    val isSelected = activeSection == section
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) PurpleContainer.copy(alpha = 0.85f) else Color.Transparent,
                        label = "sidebarBg"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) PurpleAccent else TextMuted,
                        label = "sidebarContent"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .clickable { onSectionSelected(section) }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = section.icon,
                            contentDescription = section.title,
                            tint = contentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = section.title,
                                color = if (isSelected) TextPrimary else TextMuted,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = section.subtitle,
                                color = if (isSelected) PurpleLight else TextMuted.copy(alpha = 0.6f),
                                fontSize = 8.5.sp
                            )
                        }
                    }
                }
            }

            // Bottom: Live Status Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF161616))
                    .padding(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "STATUS",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = launchState.name,
                        color = when (launchState) {
                            LaunchState.RUNNING -> SuccessGreen
                            LaunchState.LAUNCHING, LaunchState.PREPARING -> WarningAmber
                            LaunchState.ERROR -> ErrorRed
                            else -> PurpleLight
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// =====================================================================
// 1. DASHBOARD WORKSPACE (Zalith Launcher Hero & Quick Launch)
// =====================================================================
@Composable
private fun ZalithDashboardPane(
    launchHelper: LaunchHelper,
    versionManager: VersionManager,
    deviceProfile: DeviceProfile,
    prefs: PreferenceManager,
    onNavigateToVersions: () -> Unit,
    onNavigateToModpacks: () -> Unit,
    onNavigateToConsole: () -> Unit,
    onOpenWizard: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val launchState by launchHelper.launchState.collectAsState()

    val currentAccount = Account(
        id = "acc-1",
        username = prefs.accountUsername.ifBlank { "Player" },
        isMicrosoft = prefs.accountType == "Microsoft",
        uuid = prefs.accountUuid,
        accessToken = "offline_token"
    )

    val currentVersion = MinecraftVersion(
        id = prefs.selectedVersionId,
        type = "release",
        url = "",
        releaseTime = "Latest",
        javaVersion = if (prefs.selectedVersionId.startsWith("1.16") || prefs.selectedVersionId.startsWith("1.12") || prefs.selectedVersionId.startsWith("1.8")) 8 else 21,
        recommendedJava = if (prefs.selectedVersionId.startsWith("1.16")) "Java 8 ARM64" else "Java 21 ARM64",
        description = "Target Minecraft release for execution"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // TOP HERO BANNER & LAUNCH CONTROLLER
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, PurpleDarkBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = "Minecraft Panorama",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.45f
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        SurfaceDark.copy(alpha = 0.92f),
                                        PurpleContainer.copy(alpha = 0.7f),
                                        SurfaceDark.copy(alpha = 0.85f)
                                    )
                                )
                            )
                            .padding(14.dp)
                    ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Current Selected Target Info
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "TARGET: MINECRAFT ${prefs.selectedVersionId}",
                                    color = TextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(PurpleAccent.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = prefs.graphicsMode.uppercase(),
                                        color = PurpleLight,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Account: ${currentAccount.username} (${if (currentAccount.isMicrosoft) "Online MSA" else "Offline"}) • Driver: ${prefs.selectedDriverId} • Java 21",
                                color = TextMuted,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onNavigateToVersions,
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(Icons.Default.VideogameAsset, contentDescription = null, modifier = Modifier.size(12.dp), tint = PurpleLight)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Change Version", fontSize = 10.sp, color = TextPrimary)
                                }

                                Button(
                                    onClick = onNavigateToModpacks,
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(Icons.Default.Widgets, contentDescription = null, modifier = Modifier.size(12.dp), tint = CyanInfo)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Modpacks", fontSize = 10.sp, color = TextPrimary)
                                }
                            }
                        }

                        // Right: Big Launch / Stop Action Button
                        if (launchState == LaunchState.RUNNING) {
                            Button(
                                onClick = { launchHelper.stopGame() },
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .padding(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("STOP GAME", fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }
                        } else {
                            Button(
                                onClick = {
                                    launchHelper.launchGame(currentVersion, currentAccount, scope) {
                                        onNavigateToConsole()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PurpleAccent
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .shadow(8.dp, RoundedCornerShape(10.dp), spotColor = PurpleAccent)
                                    .padding(horizontal = 8.dp),
                                enabled = launchState != LaunchState.PREPARING && launchState != LaunchState.LAUNCHING
                            ) {
                                if (launchState == LaunchState.PREPARING || launchState == LaunchState.LAUNCHING) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("LAUNCHING...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                } else {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("PLAY MINECRAFT", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
        }

        // QUICK METRICS & OPTIMIZER TILES
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Hardware & Renderer
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A2A))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = CyanInfo, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("GPU & Renderer", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = deviceProfile.gpuRenderer,
                            color = PurpleLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Driver: ${prefs.selectedDriverId}",
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                    }
                }

                // Card 2: Memory (-Xmx)
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A2A))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Memory, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Allocated RAM", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${prefs.allocatedRamMb} MB (-Xmx${(prefs.allocatedRamMb / 1024).coerceAtLeast(1)}G)",
                            color = SuccessGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "25% of ${deviceProfile.totalRamMb}MB System RAM",
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                    }
                }

                // Card 3: Auto-Optimizer Button
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenWizard() },
                    colors = CardDefaults.cardColors(containerColor = PurpleContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleAccent.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Auto Optimizer", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "SD680 & itel S26 Presets",
                            color = WarningAmber,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap to tune 60FPS profile",
                            color = TextPrimary.copy(alpha = 0.8f),
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

// =====================================================================
// 2. VERSIONS WORKSPACE (Card-based grid)
// =====================================================================
@Composable
private fun ZalithVersionsPane(
    versionManager: VersionManager,
    prefs: PreferenceManager,
    onVersionSelected: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var versions by remember { mutableStateOf<List<MinecraftVersion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    val downloadingMap = remember { mutableStateMapOf<String, Float>() }

    LaunchedEffect(Unit) {
        versions = versionManager.fetchVersionsList()
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search and Filter Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Minecraft release or snapshot...", color = TextMuted, fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PurpleLight, modifier = Modifier.size(16.dp)) },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurpleAccent,
                    unfocusedBorderColor = Color(0xFF2A2A2A),
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            IconButton(
                onClick = {
                    isLoading = true
                    scope.launch {
                        versions = versionManager.fetchVersionsList()
                        isLoading = false
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceDark)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = PurpleLight, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PurpleAccent)
            }
        } else {
            val filtered = versions.filter { it.id.contains(searchQuery, ignoreCase = true) }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 220.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered) { ver ->
                    val isSelected = prefs.selectedVersionId == ver.id
                    val isDownloading = downloadingMap.containsKey(ver.id)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onVersionSelected(ver.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) PurpleContainer.copy(alpha = 0.6f) else SurfaceDark
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) PurpleAccent else Color(0xFF262626)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MC ${ver.id}",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(PurpleAccent)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("ACTIVE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ver.description,
                                color = TextMuted,
                                fontSize = 9.5.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Requires ${ver.recommendedJava}",
                                    color = PurpleLight,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                if (isDownloading) {
                                    CircularProgressIndicator(
                                        progress = { downloadingMap[ver.id] ?: 0.1f },
                                        color = PurpleAccent,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else if (ver.isInstalled) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Installed", tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                } else {
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                downloadingMap[ver.id] = 0.1f
                                                versionManager.downloadVersion(ver) { progress, _ ->
                                                    downloadingMap[ver.id] = progress
                                                }
                                                downloadingMap.remove(ver.id)
                                                versions = versionManager.fetchVersionsList()
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = "Download", tint = TextMuted, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =====================================================================
// 3. MODPACKS WORKSPACE (Fabric, Forge, OptiFine)
// =====================================================================
@Composable
private fun ZalithModpacksPane(
    modpackManager: ModpackManager,
    onModpackSelected: (Modpack) -> Unit
) {
    val scope = rememberCoroutineScope()
    var modpacks by remember { mutableStateOf<List<Modpack>>(emptyList()) }
    val downloadingMap = remember { mutableStateMapOf<String, Float>() }

    LaunchedEffect(Unit) {
        modpacks = modpackManager.loadModpacks()
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 240.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(modpacks) { pack ->
            val isDownloading = downloadingMap.containsKey(pack.id)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onModpackSelected(pack) },
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF282828))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = pack.name,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (pack.loader == "Fabric") PurpleContainer else CyanInfo.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = pack.loader.uppercase(),
                                color = if (pack.loader == "Fabric") PurpleLight else CyanInfo,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = pack.description,
                        color = TextMuted,
                        fontSize = 9.5.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MC ${pack.version} • ${pack.modsCount} Mods",
                            color = PurpleLight,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )

                        if (isDownloading) {
                            CircularProgressIndicator(
                                progress = { downloadingMap[pack.id] ?: 0.1f },
                                color = PurpleAccent,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Button(
                                onClick = {
                                    scope.launch {
                                        downloadingMap[pack.id] = 0.1f
                                        modpackManager.downloadAndInstall(pack) { progress, _ ->
                                            downloadingMap[pack.id] = progress
                                        }
                                        downloadingMap.remove(pack.id)
                                        onModpackSelected(pack)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("Install", fontSize = 9.sp, color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =====================================================================
// 4. DRIVERS & GPU WORKSPACE
// =====================================================================
@Composable
private fun ZalithDriversPane(
    prefs: PreferenceManager,
    deviceProfile: DeviceProfile,
    onDriverSelected: (String) -> Unit
) {
    val drivers = listOf(
        Triple("turnip-zink-adreno-610", "Mesa Turnip + Zink 24.1.0 (Vulkan 1.3)", "Fastest for Snapdragon 680 / Redmi Note 11 (Adreno 610)"),
        Triple("angle-mali", "ANGLE OpenGL ES on Vulkan", "High performance pipeline for ARM Mali-G57 (itel S26)"),
        Triple("gl4es-universal", "GL4ES Universal Compatibility", "Standard OpenGL 2.1 wrapper for PowerVR and generic GPUs"),
        Triple("panfrost-mali", "Panfrost OpenGLES Driver", "Open-source Gallium driver for ARM Bifrost/Valhall")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(drivers) { (id, name, desc) ->
            val isSelected = prefs.selectedDriverId == id

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDriverSelected(id) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) PurpleContainer.copy(alpha = 0.6f) else SurfaceDark
                ),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) PurpleAccent else Color(0xFF282828)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(name, color = TextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            if (id == deviceProfile.recommendedDriver) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SuccessGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("RECOMMENDED", color = SuccessGreen, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(desc, color = TextMuted, fontSize = 10.sp)
                    }

                    if (isSelected) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = PurpleAccent, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

// =====================================================================
// 5. LIVE TERMINAL / CONSOLE WORKSPACE
// =====================================================================
@Composable
private fun ZalithConsolePane(launchHelper: LaunchHelper) {
    val logs by launchHelper.logs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0C0C0C))
            .border(1.dp, Color(0xFF222222), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Terminal, contentDescription = null, tint = PurpleLight, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Process Output & Engine Telemetry", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            IconButton(onClick = { launchHelper.clearLogs() }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(14.dp))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(color = Color(0xFF222222), thickness = 1.dp)
        Spacer(modifier = Modifier.height(6.dp))

        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No active process logs. Tap PLAY to spawn instance.", color = TextMuted, fontSize = 11.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(logs) { entry ->
                    val color = when (entry.level) {
                        "ERROR" -> ErrorRed
                        "WARN" -> WarningAmber
                        "MINECRAFT" -> SuccessGreen
                        "RENDERER" -> CyanInfo
                        "JVM" -> PurpleLight
                        else -> TextMuted
                    }

                    Text(
                        text = "[${entry.timestamp}] [${entry.level}] ${entry.message}",
                        color = color,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.5.sp,
                        lineHeight = 13.sp
                    )
                }
            }
        }
    }
}

// =====================================================================
// 6. SETTINGS & OPTIMIZER WORKSPACE
// =====================================================================
@Composable
private fun ZalithSettingsPane(
    prefs: PreferenceManager,
    deviceProfile: DeviceProfile,
    onReOptimize: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF282828))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Memory & RAM Allocation (-Xmx)", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Current: ${prefs.allocatedRamMb} MB (Optimal for 60 FPS)", color = PurpleLight, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1024, 1536, 2048, 3072, 4096).forEach { ram ->
                            val isSelected = prefs.allocatedRamMb == ram
                            FilterChip(
                                selected = isSelected,
                                onClick = { prefs.allocatedRamMb = ram },
                                label = { Text("${ram}MB", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PurpleAccent,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF282828))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("In-Game Render Distance", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(4, 6, 8, 12).forEach { chunks ->
                            val isSelected = prefs.renderDistance == chunks
                            FilterChip(
                                selected = isSelected,
                                onClick = { prefs.renderDistance = chunks },
                                label = { Text("$chunks Chunks", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PurpleAccent,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = onReOptimize,
                colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(42.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Run Auto-Optimization Wizard", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
            }
        }
    }
}
