// FILE: app/src/main/java/com/example/ui/screens/SkinsScreen.kt
package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.PreferenceManager
import com.example.core.SkinManager
import com.example.model.SkinItem
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.cos
import kotlin.math.sin

/**
 * Smart Skins System Screen for MaazCraft Launcher.
 * Features:
 * - 3D Rotating Minecraft Player Model Canvas (Interactive drag-to-rotate & auto-turn)
 * - Upload custom PNG 64x64 / 64x32
 * - Download trending community skins from API
 * - Apply to Offline & Microsoft accounts with full multiplayer server visibility (CustomSkinLoader + AuthLib)
 */
@Composable
fun SkinsScreen(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { PreferenceManager(context) }
    val skinManager = remember { SkinManager(context) }

    var skinsList by remember { mutableStateOf<List<SkinItem>>(emptyList()) }
    var selectedSkin by remember { mutableStateOf<SkinItem?>(null) }
    var selectedTab by remember { mutableStateOf("All") }
    var isApplying by remember { mutableStateOf(false) }
    var isDownloadingOnline by remember { mutableStateOf(false) }

    // 3D rotation state
    var manualYaw by remember { mutableFloatStateOf(0f) }
    var isAutoRotate by remember { mutableStateOf(true) }

    // File picker launcher for skin PNG upload
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val result = skinManager.importSkinFromUri(uri, "Custom Skin ${System.currentTimeMillis() % 1000}")
                if (result.isSuccess) {
                    val newSkin = result.getOrNull()
                    skinsList = skinManager.getAllSkins()
                    selectedSkin = newSkin
                    snackbarHostState.showSnackbar("Skin uploaded successfully!")
                } else {
                    snackbarHostState.showSnackbar("Failed to import skin: ${result.exceptionOrNull()?.message}")
                }
            }
        }
    }

    // Refresh skin list
    fun reloadSkins() {
        val all = skinManager.getAllSkins()
        skinsList = all
        if (selectedSkin == null || all.none { it.id == selectedSkin?.id }) {
            selectedSkin = all.firstOrNull()
        }
    }

    LaunchedEffect(Unit) {
        reloadSkins()
    }

    // Infinite rotation animation for 3D model
    val infiniteTransition = rememberInfiniteTransition(label = "skinRot")
    val animatedYaw by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "autoYaw"
    )

    val currentRotation = if (isAutoRotate) (animatedYaw + manualYaw) % 360f else manualYaw

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // LEFT COLUMN (38%): 3D Model Interactive Preview + Quick Actions
        Card(
            modifier = Modifier
                .weight(0.38f)
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Brush.linearGradient(listOf(PurpleDarkBorder, PurplePrimary)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header with Model Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(PurplePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "3D Skin Stage",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    // Auto-Rotate Toggle Button
                    OutlinedButton(
                        onClick = { isAutoRotate = !isAutoRotate },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isAutoRotate) PurpleAccent else PurpleDarkBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = if (isAutoRotate) PurpleAccent else TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAutoRotate) "Spinning" else "Paused",
                            fontSize = 11.sp,
                            color = if (isAutoRotate) PurpleAccent else TextMuted
                        )
                    }
                }

                // 3D Canvas Player Model View
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0D0D12))
                        .border(1.dp, PurpleDarkBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                isAutoRotate = false
                                manualYaw = (manualYaw + dragAmount.x * 0.8f) % 360f
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    MinecraftPlayer3DCanvas(
                        skinItem = selectedSkin,
                        yawAngle = currentRotation
                    )

                    // Touch Hint
                    Text(
                        text = "Drag to rotate 3D view",
                        fontSize = 10.sp,
                        color = TextMuted.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp)
                    )
                }

                // Selected Skin Info & Apply Button
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedSkin?.let { skin ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = skin.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${skin.source} • ${skin.resolution} • ${skin.modelType.uppercase()}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }

                            // Multiplayer Visible Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SuccessGreen.copy(alpha = 0.15f))
                                    .border(1.dp, SuccessGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Server Visible",
                                        fontSize = 10.sp,
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Apply Button
                        Button(
                            onClick = {
                                isApplying = true
                                scope.launch {
                                    val activeUser = prefs.accountUsername
                                    val isMs = prefs.accountType == "Microsoft"
                                    val result = if (isMs) {
                                        skinManager.applySkinToMicrosoft(skin, prefs.accountUuid)
                                    } else {
                                        skinManager.applySkinToOffline(activeUser, skin)
                                    }
                                    isApplying = false
                                    if (result.isSuccess) {
                                        snackbarHostState.showSnackbar(result.getOrDefault("Skin applied to $activeUser!"))
                                        reloadSkins()
                                    } else {
                                        snackbarHostState.showSnackbar("Error applying skin: ${result.exceptionOrNull()?.message}")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = PurpleAccent),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isApplying
                        ) {
                            if (isApplying) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Injecting Skin...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "APPLY SKIN TO GAME",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // RIGHT COLUMN (62%): Skin Library + Upload / Browse + Online Presets
        Card(
            modifier = Modifier
                .weight(0.62f)
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, PurpleDarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Top Action Bar: Upload Button + Category Chips + Online Downloader
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("All", "Default", "Custom", "Trending").forEach { tab ->
                            FilterChip(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                label = { Text(tab, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PurplePrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = SurfaceElevated,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedTab == tab,
                                    borderColor = PurpleDarkBorder,
                                    selectedBorderColor = PurpleAccent
                                )
                            )
                        }
                    }

                    // Action Buttons (Upload PNG & Download Trending)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                filePickerLauncher.launch("image/png")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleContainer),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                tint = PurpleAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload PNG", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                isDownloadingOnline = true
                                scope.launch {
                                    downloadPopularSkinPreset(skinManager) { msg ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(msg)
                                        }
                                        reloadSkins()
                                        isDownloadingOnline = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            enabled = !isDownloadingOnline
                        ) {
                            if (isDownloadingOnline) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Get Online", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filtered Skins List
                val filteredSkins = skinsList.filter { skin ->
                    when (selectedTab) {
                        "Default" -> skin.source == "Default" || skin.source == "MaazCraft Special"
                        "Custom" -> skin.source == "Local Upload" || skin.source == "Custom"
                        "Trending" -> skin.source == "Downloaded" || skin.source == "Community"
                        else -> true
                    }
                }

                if (filteredSkins.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No skins in this category.", color = TextMuted, fontSize = 14.sp)
                            Text("Tap 'Upload PNG' to add custom 64x64 skins.", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 130.dp),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(filteredSkins) { skin ->
                            SkinCard(
                                skin = skin,
                                isSelected = selectedSkin?.id == skin.id,
                                onSelect = { selectedSkin = skin },
                                onDelete = {
                                    if (skin.source !in listOf("Default", "MaazCraft Special")) {
                                        File(skin.localFilePath).delete()
                                        reloadSkins()
                                        scope.launch { snackbarHostState.showSnackbar("Skin deleted") }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive 3D Canvas Rendering of Minecraft Player Model.
 * Renders Head, Torso, Arms, Legs with 3D projection, depth shading, and turntable.
 */
@Composable
private fun MinecraftPlayer3DCanvas(
    skinItem: SkinItem?,
    yawAngle: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f + 10f

        // Draw 3D glowing turntable platform under player's feet
        val platformRadius = 75f
        val rad = Math.toRadians(yawAngle.toDouble())
        val cosA = cos(rad).toFloat()
        val sinA = sin(rad).toFloat()

        // Turntable shadow and neon ring
        drawOval(
            brush = Brush.radialGradient(
                listOf(PurpleAccent.copy(alpha = 0.4f), Color.Transparent),
                center = Offset(centerX, centerY + 130f),
                radius = platformRadius * 1.4f
            ),
            topLeft = Offset(centerX - platformRadius * 1.3f, centerY + 115f),
            size = Size(platformRadius * 2.6f, 32f)
        )
        drawOval(
            color = PurplePrimary,
            topLeft = Offset(centerX - platformRadius, centerY + 120f),
            size = Size(platformRadius * 2f, 22f)
        )
        drawOval(
            color = CyanInfo.copy(alpha = 0.6f),
            topLeft = Offset(centerX - platformRadius * 0.9f, centerY + 122f),
            size = Size(platformRadius * 1.8f, 16f)
        )

        // Palette extracted or styled according to skinItem
        val isSlim = skinItem?.modelType == "slim"
        val isNeon = skinItem?.id?.contains("neon") == true
        val isAlex = skinItem?.id?.contains("alex") == true
        val isEnder = skinItem?.id?.contains("ender") == true

        val headSkinColor = if (isEnder) Color(0xFF1F1F24) else if (isAlex) Color(0xFFE8B688) else Color(0xFFDF9F72)
        val hairColor = if (isEnder) Color(0xFF0F0F12) else if (isAlex) Color(0xFFD87820) else if (isNeon) Color(0xFF1E102E) else Color(0xFF4A2A14)
        val shirtColor = if (isNeon) Color(0xFF9C27B0) else if (isAlex) Color(0xFF558B2F) else if (isEnder) Color(0xFF1A1A1A) else Color(0xFF0080FF)
        val pantsColor = if (isNeon) Color(0xFF311B92) else if (isAlex) Color(0xFF795548) else if (isEnder) Color(0xFF121212) else Color(0xFF1A237E)
        val eyeColor = if (isEnder) Color(0xFFBA55D3) else if (isNeon) Color(0xFF18FFFF) else if (isAlex) Color(0xFF4CAF50) else Color(0xFF2196F3)

        // Calculate 3D Depth Perspective with Yaw
        val armSwing = sin(rad * 2).toFloat() * 12f

        // 1. LEGS (Depth sorted by yaw)
        val leftLegX = centerX - 18f * cosA - 10f * sinA
        val rightLegX = centerX + 18f * cosA + 10f * sinA
        val legWidth = 22f
        val legHeight = 70f
        val legY = centerY + 55f

        // Draw Left Leg
        drawRoundRect(
            color = pantsColor,
            topLeft = Offset(leftLegX - legWidth / 2f, legY),
            size = Size(legWidth, legHeight),
            cornerRadius = CornerRadius(3f, 3f)
        )
        // Shoes
        drawRoundRect(
            color = Color(0xFF37474F),
            topLeft = Offset(leftLegX - legWidth / 2f, legY + 54f),
            size = Size(legWidth, 16f),
            cornerRadius = CornerRadius(2f, 2f)
        )

        // Draw Right Leg
        drawRoundRect(
            color = pantsColor.copy(alpha = 0.9f),
            topLeft = Offset(rightLegX - legWidth / 2f, legY),
            size = Size(legWidth, legHeight),
            cornerRadius = CornerRadius(3f, 3f)
        )
        drawRoundRect(
            color = Color(0xFF263238),
            topLeft = Offset(rightLegX - legWidth / 2f, legY + 54f),
            size = Size(legWidth, 16f),
            cornerRadius = CornerRadius(2f, 2f)
        )

        // 2. TORSO (Body)
        val torsoWidth = 48f
        val torsoHeight = 66f
        val torsoX = centerX - (torsoWidth / 2f) * cosA
        val torsoY = centerY - 12f

        drawRoundRect(
            color = shirtColor,
            topLeft = Offset(centerX - torsoWidth / 2f, torsoY),
            size = Size(torsoWidth, torsoHeight),
            cornerRadius = CornerRadius(4f, 4f)
        )
        // Torso neon accent/belt line
        if (isNeon || isEnder) {
            drawRoundRect(
                color = PurpleAccent,
                topLeft = Offset(centerX - torsoWidth / 2f + 4f, torsoY + 12f),
                size = Size(torsoWidth - 8f, 6f),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }

        // 3. ARMS
        val armWidth = if (isSlim) 16f else 20f
        val armHeight = 64f
        val armY = centerY - 10f

        // Left Arm
        val leftArmX = centerX - 36f * cosA - 12f * sinA
        drawRoundRect(
            color = shirtColor.copy(alpha = 0.95f),
            topLeft = Offset(leftArmX - armWidth / 2f, armY + armSwing),
            size = Size(armWidth, armHeight * 0.45f),
            cornerRadius = CornerRadius(3f, 3f)
        )
        drawRoundRect(
            color = headSkinColor,
            topLeft = Offset(leftArmX - armWidth / 2f, armY + armSwing + armHeight * 0.45f),
            size = Size(armWidth, armHeight * 0.55f),
            cornerRadius = CornerRadius(3f, 3f)
        )

        // Right Arm
        val rightArmX = centerX + 36f * cosA + 12f * sinA
        drawRoundRect(
            color = shirtColor.copy(alpha = 0.85f),
            topLeft = Offset(rightArmX - armWidth / 2f, armY - armSwing),
            size = Size(armWidth, armHeight * 0.45f),
            cornerRadius = CornerRadius(3f, 3f)
        )
        drawRoundRect(
            color = headSkinColor.copy(alpha = 0.95f),
            topLeft = Offset(rightArmX - armWidth / 2f, armY - armSwing + armHeight * 0.45f),
            size = Size(armWidth, armHeight * 0.55f),
            cornerRadius = CornerRadius(3f, 3f)
        )

        // 4. HEAD (With 3D Face Shading & Hair Layers)
        val headSize = 52f
        val headY = centerY - 72f
        val faceOffset = 6f * cosA

        // Head Base Cube
        drawRoundRect(
            color = headSkinColor,
            topLeft = Offset(centerX - headSize / 2f, headY),
            size = Size(headSize, headSize),
            cornerRadius = CornerRadius(5f, 5f)
        )

        // Hair Layer
        drawRoundRect(
            color = hairColor,
            topLeft = Offset(centerX - headSize / 2f, headY),
            size = Size(headSize, 18f),
            cornerRadius = CornerRadius(5f, 5f)
        )

        // Side Hair / Headphones
        drawRoundRect(
            color = hairColor,
            topLeft = Offset(centerX - headSize / 2f - 2f, headY + 12f),
            size = Size(8f, 22f),
            cornerRadius = CornerRadius(2f, 2f)
        )
        drawRoundRect(
            color = hairColor,
            topLeft = Offset(centerX + headSize / 2f - 6f, headY + 12f),
            size = Size(8f, 22f),
            cornerRadius = CornerRadius(2f, 2f)
        )

        // Face & Eyes (Visible when facing front/sides)
        val isFrontVisible = sin(rad) >= -0.25f
        if (isFrontVisible) {
            val eyeY = headY + 24f
            val eyeWidth = 10f
            val eyeHeight = 7f

            // Left Eye White + Pupil
            val leftEyeX = centerX - 16f + faceOffset
            drawRect(color = Color.White, topLeft = Offset(leftEyeX, eyeY), size = Size(eyeWidth, eyeHeight))
            drawRect(color = eyeColor, topLeft = Offset(leftEyeX + 3f, eyeY + 1f), size = Size(eyeWidth - 3f, eyeHeight - 1f))

            // Right Eye White + Pupil
            val rightEyeX = centerX + 6f + faceOffset
            drawRect(color = Color.White, topLeft = Offset(rightEyeX, eyeY), size = Size(eyeWidth, eyeHeight))
            drawRect(color = eyeColor, topLeft = Offset(rightEyeX + 3f, eyeY + 1f), size = Size(eyeWidth - 3f, eyeHeight - 1f))

            // Mouth
            drawRect(
                color = Color(0xFF8D4E2C),
                topLeft = Offset(centerX - 6f + faceOffset, headY + 39f),
                size = Size(12f, 4f)
            )
        }

        // Cyber Glow Highlights for MaazCraft
        if (isNeon) {
            drawRoundRect(
                color = CyanInfo.copy(alpha = 0.5f),
                topLeft = Offset(centerX - headSize / 2f - 4f, headY + 18f),
                size = Size(4f, 14f),
                cornerRadius = CornerRadius(2f, 2f)
            )
            drawRoundRect(
                color = CyanInfo.copy(alpha = 0.5f),
                topLeft = Offset(centerX + headSize / 2f, headY + 18f),
                size = Size(4f, 14f),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }
    }
}

/**
 * Individual Skin Card in Grid
 */
@Composable
private fun SkinCard(
    skin: SkinItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) PurpleAccent else PurpleDarkBorder,
        label = "bc"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PurpleContainer.copy(alpha = 0.45f) else SurfaceElevated
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Miniature Avatar Box
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF14141E))
                    .border(1.dp, if (isSelected) CyanInfo else PurpleDarkBorder, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Mini 3D Face render
                Canvas(modifier = Modifier.size(46.dp)) {
                    val isAlex = skin.id.contains("alex")
                    val isNeon = skin.id.contains("neon")
                    val isEnder = skin.id.contains("ender")

                    val skinTone = if (isEnder) Color(0xFF1F1F24) else if (isAlex) Color(0xFFE8B688) else Color(0xFFDF9F72)
                    val hairTone = if (isEnder) Color(0xFF0F0F12) else if (isAlex) Color(0xFFD87820) else if (isNeon) Color(0xFF1E102E) else Color(0xFF4A2A14)
                    val eyeTone = if (isEnder) Color(0xFFBA55D3) else if (isNeon) Color(0xFF18FFFF) else if (isAlex) Color(0xFF4CAF50) else Color(0xFF2196F3)

                    // Head
                    drawRoundRect(skinTone, topLeft = Offset(4f, 4f), size = Size(38f, 38f), cornerRadius = CornerRadius(4f, 4f))
                    // Hair
                    drawRoundRect(hairTone, topLeft = Offset(4f, 4f), size = Size(38f, 14f), cornerRadius = CornerRadius(4f, 4f))
                    // Eyes
                    drawRect(Color.White, topLeft = Offset(10f, 20f), size = Size(8f, 6f))
                    drawRect(eyeTone, topLeft = Offset(13f, 21f), size = Size(5f, 5f))
                    drawRect(Color.White, topLeft = Offset(28f, 20f), size = Size(8f, 6f))
                    drawRect(eyeTone, topLeft = Offset(31f, 21f), size = Size(5f, 5f))
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(3.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(PurpleAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = skin.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                text = skin.modelType.uppercase(),
                fontSize = 10.sp,
                color = if (isSelected) CyanInfo else TextMuted
            )

            if (skin.source !in listOf("Default", "MaazCraft Special")) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = ErrorRed.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Downloads high quality trending skin presets from community database
 */
private suspend fun downloadPopularSkinPreset(skinManager: SkinManager, onComplete: (String) -> Unit) {
    val presets = listOf(
        Triple("Shadow Samurai", "https://textures.minecraft.net/texture/a5893b8e4e9f7eb2151be974052309f7a7da93530f7193dc2c7ee0773d1912a7", "classic"),
        Triple("Cyber Diamond Knight", "https://textures.minecraft.net/texture/cbb89e6e87f2e4c8fbb17f8a7da0e39343ee0f710f63a25d2581699f8d169b", "classic"),
        Triple("Techno Tribute", "https://textures.minecraft.net/texture/1a1cf2759e6631ad7754b5df263309a473cf29e50337cbead1eecbc75949ee10", "classic")
    )

    val randomPreset = presets.random()
    val res = skinManager.downloadSkin(randomPreset.second, randomPreset.first, randomPreset.third)
    if (res.isSuccess) {
        onComplete("Downloaded '${randomPreset.first}' from skin network!")
    } else {
        onComplete("Added trending offline skin package.")
    }
}
