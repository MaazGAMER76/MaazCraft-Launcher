// FILE: app/src/main/java/com/example/ui/screens/HomeScreen.kt
package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.DeviceProfileDetector
import com.example.core.LaunchHelper
import com.example.core.LaunchState
import com.example.core.PreferenceManager
import com.example.model.Account
import com.example.model.MinecraftVersion
import com.example.ui.components.TouchControlsOverlay
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.ConsoleBg
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

@Composable
fun HomeScreen(
    launchHelper: LaunchHelper,
    onNavigateToVersions: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onReOptimize: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { PreferenceManager(context) }
    val clipboardManager = LocalClipboardManager.current

    val launchState by launchHelper.launchState.collectAsState()
    val logs by launchHelper.logs.collectAsState()
    val fps by launchHelper.currentFps.collectAsState()

    var showConsoleDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var showInGameSimulation by remember { mutableStateOf(false) }
    var showPauseMenu by remember { mutableStateOf(false) }
    var showChatDialog by remember { mutableStateOf(false) }
    var showInventoryDialog by remember { mutableStateOf(false) }

    var accountName by remember { mutableStateOf(prefs.accountUsername) }
    var isMicrosoftAccount by remember { mutableStateOf(prefs.accountType == "Microsoft") }

    val detectedProfile = remember { DeviceProfileDetector.detect(context) }

    val activeVersion = remember(prefs.selectedVersionId, prefs.selectedModLoader) {
        MinecraftVersion(
            id = prefs.selectedVersionId,
            type = "release",
            url = "",
            releaseTime = "2024",
            javaVersion = 21,
            recommendedJava = "Java 21 ARM64",
            description = "Selected Game Target",
            isInstalled = true,
            modLoader = prefs.selectedModLoader
        )
    }

    val activeAccount = remember(accountName, isMicrosoftAccount) {
        Account(
            id = "acc_1",
            username = accountName,
            uuid = prefs.accountUuid,
            isMicrosoft = isMicrosoftAccount,
            skinType = if (accountName.lowercase().contains("alex")) "Alex" else "Steve"
        )
    }

    // When game launches, automatically open interactive session
    if (launchState == LaunchState.RUNNING && !showInGameSimulation) {
        showInGameSimulation = true
    }

    // MAIN DASHBOARD CONTENT
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Brand Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(listOf(PurplePrimary, PurpleAccent))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "M3",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "MaazCraft Launcher",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "v3.0.0 (Pojav GPL-3.0 Engine)",
                        fontSize = 11.sp,
                        color = PurpleAccent,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Console and Re-Optimize buttons
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = { showConsoleDialog = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated)
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Console Logs",
                        tint = if (logs.isNotEmpty()) PurpleAccent else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onReOptimize,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Re-Optimize",
                        tint = PurpleAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // HERO BANNER CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(18.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(PurplePrimary, PurpleAccent.copy(alpha = 0.4f)))
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                PurpleContainer.copy(alpha = 0.7f),
                                SurfaceDark
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PurplePrimary)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "MINECRAFT JAVA EDITION",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }

                        // Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when (launchState) {
                                        LaunchState.RUNNING -> SuccessGreen.copy(alpha = 0.2f)
                                        LaunchState.PREPARING, LaunchState.LAUNCHING -> WarningAmber.copy(alpha = 0.2f)
                                        LaunchState.ERROR -> ErrorRed.copy(alpha = 0.2f)
                                        else -> Color.DarkGray.copy(alpha = 0.4f)
                                    }
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = when (launchState) {
                                    LaunchState.RUNNING -> "● RUNNING (${fps} FPS)"
                                    LaunchState.PREPARING -> "● PREPARING JVM..."
                                    LaunchState.LAUNCHING -> "● LAUNCHING..."
                                    LaunchState.ERROR -> "● LAUNCH FAILED"
                                    else -> "● READY TO PLAY"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (launchState) {
                                    LaunchState.RUNNING -> SuccessGreen
                                    LaunchState.PREPARING, LaunchState.LAUNCHING -> WarningAmber
                                    LaunchState.ERROR -> ErrorRed
                                    else -> TextSecondary
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Version ${activeVersion.id}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )

                    Text(
                        text = "${activeVersion.modLoader} Loader • ${detectedProfile.socName} Optimized",
                        fontSize = 13.sp,
                        color = PurpleLight,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Version Selector Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceElevated)
                            .clickable { onNavigateToVersions() }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VideogameAsset,
                                contentDescription = null,
                                tint = PurpleAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Target Version: ${activeVersion.id} (${activeVersion.modLoader})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Tap to switch version or install Forge/Fabric",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                }
            }
        }

        // ACCOUNT & PROFILE ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Player Account Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showAccountDialog = true },
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PurpleDarkBorder, PurpleDarkBorder)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PurpleContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (activeAccount.skinType == "Alex") "👩" else "🧑",
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = activeAccount.username,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (activeAccount.isMicrosoft) "Microsoft Acc" else "Offline Acc",
                            fontSize = 11.sp,
                            color = if (activeAccount.isMicrosoft) SuccessGreen else TextMuted
                        )
                    }
                }
            }

            // Quick Mod Loader Switcher Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        val nextLoader = when (prefs.selectedModLoader) {
                            "Vanilla" -> "Fabric"
                            "Fabric" -> "Forge"
                            else -> "Vanilla"
                        }
                        prefs.selectedModLoader = nextLoader
                    },
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PurpleDarkBorder, PurpleDarkBorder)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PurpleAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = PurpleAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Loader: ${prefs.selectedModLoader}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Tap to switch",
                            fontSize = 11.sp,
                            color = PurpleAccent
                        )
                    }
                }
            }
        }

        // HARDWARE & PERFORMANCE PRESET CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PurpleDarkBorder, PurpleDarkBorder)))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = CyanInfo,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${detectedProfile.socName.take(24)} Preset Active",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    TextButton(onClick = onNavigateToSettings) {
                        Text(text = "Customize", fontSize = 12.sp, color = PurpleAccent)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetBadge(label = "RAM", value = "${prefs.allocatedRamMb} MB", icon = Icons.Default.Memory)
                    PresetBadge(label = "Java", value = prefs.selectedJavaVersion.take(7), icon = Icons.Default.Code)
                    PresetBadge(label = "Chunks", value = "${prefs.renderDistance}", icon = Icons.Default.Speed)
                    PresetBadge(label = "Driver", value = if (prefs.selectedDriverId.contains("mobileglues")) "MobileGlues" else "Turnip", icon = Icons.Default.VideogameAsset)
                }
            }
        }

        // LAUNCH ACTION BUTTON
        Spacer(modifier = Modifier.height(4.dp))

        if (launchState == LaunchState.RUNNING) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showInGameSimulation = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VideogameAsset,
                        contentDescription = null,
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RESUME GAME",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 15.sp
                    )
                }

                Button(
                    onClick = { launchHelper.stopGame() },
                    modifier = Modifier
                        .width(110.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "STOP", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        } else {
            Button(
                onClick = {
                    launchHelper.launchGame(activeVersion, activeAccount, scope) {
                        showInGameSimulation = true
                    }
                },
                enabled = launchState != LaunchState.PREPARING && launchState != LaunchState.LAUNCHING,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurplePrimary,
                    contentColor = Color.White,
                    disabledContainerColor = PurpleContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (launchState == LaunchState.PREPARING || launchState == LaunchState.LAUNCHING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "STARTING MINECRAFT...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "PLAY MINECRAFT ${activeVersion.id}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Live Log Preview Strip (Last 2 lines)
        if (logs.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showConsoleDialog = true },
                colors = CardDefaults.cardColors(containerColor = ConsoleBg),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PurpleDarkBorder, PurpleDarkBorder)))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Console Output (${logs.size} lines)",
                            fontSize = 11.sp,
                            color = PurpleAccent,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Tap to expand",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val recent = logs.takeLast(2)
                    recent.forEach { l ->
                        Text(
                            text = "[${l.timestamp}] [${l.level}] ${l.message}",
                            fontSize = 10.sp,
                            color = if (l.level == "ERROR") ErrorRed else if (l.level == "WARN") WarningAmber else TextPrimary,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    // ACCOUNT SELECTOR DIALOG
    if (showAccountDialog) {
        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            title = {
                Text(text = "Manage Account", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Choose your player nickname and authentication mode.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = accountName,
                        onValueChange = { accountName = it },
                        label = { Text("Player Nickname") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurpleAccent,
                            unfocusedBorderColor = PurpleDarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                isMicrosoftAccount = false
                                prefs.accountUsername = accountName
                                prefs.accountType = "Offline"
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isMicrosoftAccount) PurplePrimary else SurfaceElevated
                            )
                        ) {
                            Text("Offline Mode")
                        }

                        Button(
                            onClick = {
                                isMicrosoftAccount = true
                                prefs.accountUsername = accountName
                                prefs.accountType = "Microsoft"
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMicrosoftAccount) PurplePrimary else SurfaceElevated
                            )
                        ) {
                            Text("Microsoft")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        prefs.accountUsername = accountName.ifBlank { "MaazPlayer" }
                        prefs.accountType = if (isMicrosoftAccount) "Microsoft" else "Offline"
                        showAccountDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Save Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccountDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // CONSOLE LOGS MODAL DIALOG
    if (showConsoleDialog) {
        Dialog(
            onDismissRequest = { showConsoleDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                shape = RoundedCornerShape(16.dp),
                color = ConsoleBg
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = PurpleAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Minecraft Console Log",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Row {
                            IconButton(onClick = {
                                val allText = logs.joinToString("\n") { "[${it.timestamp}] [${it.level}] ${it.message}" }
                                clipboardManager.setText(AnnotatedString(allText))
                            }) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextSecondary)
                            }
                            IconButton(onClick = { launchHelper.clearLogs() }) {
                                Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear", tint = TextSecondary)
                            }
                            IconButton(onClick = { showConsoleDialog = false }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Logs List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                            .padding(8.dp)
                    ) {
                        items(logs) { log ->
                            val color = when (log.level) {
                                "ERROR" -> ErrorRed
                                "WARN" -> WarningAmber
                                "JVM" -> PurpleAccent
                                "RENDERER" -> CyanInfo
                                else -> SuccessGreen
                            }
                            Text(
                                text = "[${log.timestamp}] [${log.level}] ${log.message}",
                                color = color,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "State: ${launchState.name} • Process ID: ${android.os.Process.myPid()}",
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                        if (launchState == LaunchState.RUNNING) {
                            Button(
                                onClick = { launchHelper.stopGame() },
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Kill Process")
                            }
                        }
                    }
                }
            }
        }
    }

    // FULL-SCREEN IN-GAME SIMULATOR SESSION
    if (showInGameSimulation && launchState == LaunchState.RUNNING) {
        Dialog(
            onDismissRequest = { /* Require using in-game ESC menu */ },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF1B263B)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Minecraft Game World Viewport Canvas Simulator
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF4A90E2), // Day Sky
                                        Color(0xFF5C9E66), // Mountains
                                        Color(0xFF437548), // Grass
                                        Color(0xFF654321)  // Dirt
                                    )
                                )
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "⛏️ MAAZCRAFT WORLD",
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Minecraft ${activeVersion.id} • ${detectedProfile.gpuRenderer.take(30)} • ${fps} FPS",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Touch Controls On-Screen Overlay
                    TouchControlsOverlay(
                        scale = prefs.controlScale,
                        opacity = prefs.controlOpacity,
                        haptic = prefs.hapticFeedback,
                        fps = fps,
                        onPauseOpen = { showPauseMenu = true },
                        onChatOpen = { showChatDialog = true },
                        onInventoryOpen = { showInventoryDialog = true }
                    )

                    // PAUSE MENU MODAL
                    if (showPauseMenu) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.75f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier
                                    .width(300.dp)
                                    .padding(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PurplePrimary)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Game Menu",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = TextPrimary
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Button(
                                        onClick = { showPauseMenu = false },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                                    ) {
                                        Text("Back to Game")
                                    }

                                    Button(
                                        onClick = {
                                            showPauseMenu = false
                                            showConsoleDialog = true
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated)
                                    ) {
                                        Text("Console Logs")
                                    }

                                    Button(
                                        onClick = {
                                            showPauseMenu = false
                                            showInGameSimulation = false
                                            launchHelper.stopGame()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                                    ) {
                                        Text("Save & Quit to Title")
                                    }
                                }
                            }
                        }
                    }

                    // CHAT MODAL
                    if (showChatDialog) {
                        var chatInput by remember { mutableStateOf("") }
                        Dialog(onDismissRequest = { showChatDialog = false }) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Minecraft Chat & Commands",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = chatInput,
                                        onValueChange = { chatInput = it },
                                        placeholder = { Text("Type /gamemode, /time set day, message...") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = PurpleAccent,
                                            unfocusedBorderColor = PurpleDarkBorder,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = { showChatDialog = false }) {
                                            Text("Cancel", color = TextMuted)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = { showChatDialog = false },
                                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                                        ) {
                                            Text("Send")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // INVENTORY MODAL
                    if (showInventoryDialog) {
                        Dialog(onDismissRequest = { showInventoryDialog = false }) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Crafting & Survival Inventory",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    // 3x9 Grid items
                                    val invIcons = listOf("🛡️", "🏹", "🍖", "🍎", "💎", "🪙", "🪵", "🪨", "🪚")
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        for (row in 0..2) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                for (col in 0..8) {
                                                    val icon = invIcons[(row * 9 + col) % invIcons.size]
                                                    Box(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .background(
                                                                SurfaceElevated,
                                                                RoundedCornerShape(4.dp)
                                                            )
                                                            .border(
                                                                1.dp,
                                                                PurpleDarkBorder,
                                                                RoundedCornerShape(4.dp)
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(text = icon, fontSize = 13.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = { showInventoryDialog = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                                    ) {
                                        Text("Close Inventory")
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

@Composable
private fun PresetBadge(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceElevated)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, fontSize = 9.sp, color = TextMuted)
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PurpleLight,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
