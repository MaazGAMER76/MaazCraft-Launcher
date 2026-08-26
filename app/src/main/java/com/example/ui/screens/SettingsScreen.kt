// FILE: app/src/main/java/com/example/ui/screens/SettingsScreen.kt
package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.DriverInstaller
import com.example.core.JavaManager
import com.example.core.PreferenceManager
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanInfo
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
fun SettingsScreen(
    onReOptimize: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }
    val javaManager = remember { JavaManager(context) }
    val driverInstaller = remember { DriverInstaller(context) }

    var ramMb by remember { mutableIntStateOf(prefs.allocatedRamMb) }
    var selectedJava by remember { mutableStateOf(prefs.selectedJavaVersion) }
    var selectedDriverId by remember { mutableStateOf(prefs.selectedDriverId) }
    var renderDistance by remember { mutableIntStateOf(prefs.renderDistance) }
    var graphicsMode by remember { mutableStateOf(prefs.graphicsMode) }
    var resolutionScale by remember { mutableIntStateOf(prefs.resolutionScale) }
    var controlScale by remember { mutableFloatStateOf(prefs.controlScale) }
    var controlOpacity by remember { mutableFloatStateOf(prefs.controlOpacity) }
    var hapticFeedback by remember { mutableStateOf(prefs.hapticFeedback) }
    var jvmArgs by remember { mutableStateOf(prefs.customJvmArgs) }

    var showJavaMenu by remember { mutableStateOf(false) }
    var showDriverMenu by remember { mutableStateOf(false) }
    var showLicenseDialog by remember { mutableStateOf(false) }

    val javaOptions = listOf(
        "Auto (Recommended)",
        "Java 21 ARM64 (MC 1.21+)",
        "Java 17 ARM64 (MC 1.17-1.20)",
        "Java 8 ARM64 (MC 1.8-1.16)"
    )

    val driversList = remember { driverInstaller.loadDriverDatabase() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Launcher Settings",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "JVM, Render engine, memory & controls",
                    fontSize = 12.sp,
                    color = PurpleAccent
                )
            }

            Button(
                onClick = onReOptimize,
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Re-Optimize", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 1. RAM ALLOCATION SECTION
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PurpleDarkBorder, PurpleDarkBorder)))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Memory, contentDescription = null, tint = CyanInfo, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("RAM Allocation", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                    }
                    Text(
                        text = "$ramMb MB (${String.format("%.1f", ramMb / 1024f)} GB)",
                        fontWeight = FontWeight.Bold,
                        color = PurpleAccent,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Snapdragon 680 with 8GB RAM recommended: 2048 MB (2 GB)",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = ramMb.toFloat(),
                    onValueChange = {
                        ramMb = ((it / 256).toInt() * 256).coerceIn(512, 8192)
                        prefs.allocatedRamMb = ramMb
                    },
                    valueRange = 512f..8192f,
                    colors = SliderDefaults.colors(
                        thumbColor = PurpleAccent,
                        activeTrackColor = PurplePrimary,
                        inactiveTrackColor = PurpleDarkBorder
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("512 MB", fontSize = 10.sp, color = TextMuted)
                    Text("2048 MB (Optimal)", fontSize = 10.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                    Text("8192 MB", fontSize = 10.sp, color = TextMuted)
                }
            }
        }

        // 2. JAVA RUNTIME PICKER
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PurpleDarkBorder, PurpleDarkBorder)))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Java Runtime Environment", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceElevated)
                        .clickable { showJavaMenu = true }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = selectedJava, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 13.sp)
                            Text(text = "Auto-selects Java 8, 17, or 21 based on target MC version", fontSize = 10.sp, color = TextMuted)
                        }
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = PurpleAccent)
                    }

                    DropdownMenu(
                        expanded = showJavaMenu,
                        onDismissRequest = { showJavaMenu = false },
                        modifier = Modifier.background(SurfaceDark)
                    ) {
                        javaOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt, color = TextPrimary) },
                                onClick = {
                                    selectedJava = opt
                                    prefs.selectedJavaVersion = opt
                                    showJavaMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // 3. GRAPHICS DRIVER & RENDER SETTINGS
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PurpleDarkBorder, PurpleDarkBorder)))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.VideogameAsset, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Graphics Driver & Renderer", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Driver Selector
                val currentDriver = driversList.firstOrNull { it.id == selectedDriverId } ?: driversList[0]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceElevated)
                        .clickable { showDriverMenu = true }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = currentDriver.name, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 13.sp)
                            Text(text = "Target: ${currentDriver.targetGpu} • ${currentDriver.driverType}", fontSize = 10.sp, color = PurpleLight)
                        }
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = PurpleAccent)
                    }

                    DropdownMenu(
                        expanded = showDriverMenu,
                        onDismissRequest = { showDriverMenu = false },
                        modifier = Modifier.background(SurfaceDark)
                    ) {
                        driversList.forEach { drv ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(drv.name, color = TextPrimary, fontWeight = FontWeight.Bold)
                                        Text(drv.targetGpu, fontSize = 11.sp, color = PurpleLight)
                                    }
                                },
                                onClick = {
                                    selectedDriverId = drv.id
                                    prefs.selectedDriverId = drv.id
                                    showDriverMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Render Distance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Render Distance", fontSize = 13.sp, color = TextPrimary)
                    Text("$renderDistance Chunks", fontWeight = FontWeight.Bold, color = PurpleAccent, fontFamily = FontFamily.Monospace)
                }
                Slider(
                    value = renderDistance.toFloat(),
                    onValueChange = {
                        renderDistance = it.toInt()
                        prefs.renderDistance = renderDistance
                    },
                    valueRange = 2f..24f,
                    steps = 21,
                    colors = SliderDefaults.colors(thumbColor = PurpleAccent, activeTrackColor = PurplePrimary)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Graphics Mode (Fast vs Fancy)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Fast Graphics Mode", fontSize = 13.sp, color = TextPrimary)
                        Text("Reduces leaf and shadow geometry for higher FPS", fontSize = 11.sp, color = TextMuted)
                    }
                    Switch(
                        checked = graphicsMode == "Fast",
                        onCheckedChange = {
                            graphicsMode = if (it) "Fast" else "Fancy"
                            prefs.graphicsMode = graphicsMode
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = PurpleAccent, checkedTrackColor = PurplePrimary)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Resolution Scale
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Resolution Scale", fontSize = 13.sp, color = TextPrimary)
                    Text("$resolutionScale%", fontWeight = FontWeight.Bold, color = PurpleAccent, fontFamily = FontFamily.Monospace)
                }
                Slider(
                    value = resolutionScale.toFloat(),
                    onValueChange = {
                        resolutionScale = ((it / 5).toInt() * 5).coerceIn(50, 125)
                        prefs.resolutionScale = resolutionScale
                    },
                    valueRange = 50f..125f,
                    colors = SliderDefaults.colors(thumbColor = PurpleAccent, activeTrackColor = PurplePrimary)
                )
            }
        }

        // 4. TOUCH CONTROLS SETTINGS
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PurpleDarkBorder, PurpleDarkBorder)))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.TouchApp, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Touch Controls Customization", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Button Scale
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Button Scale", fontSize = 13.sp, color = TextPrimary)
                    Text(String.format("%.2fx", controlScale), fontWeight = FontWeight.Bold, color = PurpleAccent)
                }
                Slider(
                    value = controlScale,
                    onValueChange = {
                        controlScale = it
                        prefs.controlScale = it
                    },
                    valueRange = 0.75f..1.75f,
                    colors = SliderDefaults.colors(thumbColor = PurpleAccent, activeTrackColor = PurplePrimary)
                )

                // Button Opacity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Button Opacity", fontSize = 13.sp, color = TextPrimary)
                    Text("${(controlOpacity * 100).toInt()}%", fontWeight = FontWeight.Bold, color = PurpleAccent)
                }
                Slider(
                    value = controlOpacity,
                    onValueChange = {
                        controlOpacity = it
                        prefs.controlOpacity = it
                    },
                    valueRange = 0.2f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = PurpleAccent, activeTrackColor = PurplePrimary)
                )

                // Haptic Feedback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Haptic Touch Feedback", fontSize = 13.sp, color = TextPrimary)
                        Text("Vibrate on button tap and hotbar change", fontSize = 11.sp, color = TextMuted)
                    }
                    Switch(
                        checked = hapticFeedback,
                        onCheckedChange = {
                            hapticFeedback = it
                            prefs.hapticFeedback = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = PurpleAccent, checkedTrackColor = PurplePrimary)
                    )
                }
            }
        }

        // 5. CUSTOM JVM ARGUMENTS
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PurpleDarkBorder, PurpleDarkBorder)))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Custom JVM Arguments", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = jvmArgs,
                    onValueChange = {
                        jvmArgs = it
                        prefs.customJvmArgs = it
                    },
                    placeholder = { Text("-XX:+UseG1GC -XX:+UnlockExperimentalVMOptions...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurpleAccent,
                        unfocusedBorderColor = PurpleDarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        // 6. GITHUB REPOSITORY & OTA UPDATES
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PurpleDarkBorder, PurpleDarkBorder)))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, tint = PurpleAccent)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("GitHub Releases & Repository", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                        Text("${prefs.githubOwner}/${prefs.githubRepo} • v3.0.0", fontSize = 11.sp, color = TextSecondary)
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PurpleContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Configured", fontSize = 10.sp, color = PurpleLight, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 7. LICENSE & ABOUT POJAVLAUNCHER FORK
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showLicenseDialog = true },
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PurpleDarkBorder, PurpleDarkBorder)))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Open Source License (GNU GPL-3.0)", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                        Text("PojavLauncher Fork • User must own Minecraft", fontSize = 11.sp, color = TextSecondary)
                    }
                }
                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = PurpleAccent)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }

    // LICENSE MODAL
    if (showLicenseDialog) {
        AlertDialog(
            onDismissRequest = { showLicenseDialog = false },
            title = {
                Text("MaazCraft Launcher V3 (GPL-3.0)", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "This application is an enhanced fork of the open-source PojavLauncher project, licensed under the GNU General Public License v3.0.",
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "• Minecraft is a trademark of Mojang AB / Microsoft. Users must own an official Minecraft license to play online.\n" +
                                "• Built with Eclipse Temurin OpenJDK ARM64, Mesa Turnip Vulkan driver, Zink OpenGL 4.6, and LWJGL.\n" +
                                "• Full source code and LICENSE file included in repository root.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showLicenseDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Close")
                }
            },
            containerColor = SurfaceDark
        )
    }
}
