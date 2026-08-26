// FILE: app/src/main/java/com/example/ui/wizard/OptimizationWizardDialog.kt
package com.example.ui.wizard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.ControlProfileManager
import com.example.core.DeviceProfileDetector
import com.example.core.DriverInstaller
import com.example.core.JavaManager
import com.example.core.PreferenceManager
import com.example.model.DeviceProfile
import com.example.model.OptimizationStep
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanInfo
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OptimizationWizardDialog(
    onDismiss: () -> Unit,
    onCompleted: (DeviceProfile) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { PreferenceManager(context) }
    val javaManager = remember { JavaManager(context) }
    val driverInstaller = remember { DriverInstaller(context) }
    val controlManager = remember { ControlProfileManager(context) }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    var overallProgress by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("Initializing hardware analyzer...") }
    var isDone by remember { mutableStateOf(false) }
    var detectedProfile by remember { mutableStateOf<DeviceProfile?>(null) }

    val steps = remember {
        mutableStateListOf(
            OptimizationStep(0, "1. Detect Device Hardware", "Scanning CPU, GPU, RAM and thermal profile...", isRunning = true),
            OptimizationStep(1, "2. Setup Java Runtimes", "Configuring Java 8, 17, 21 ARM64 for all MC versions...", isDone = false),
            OptimizationStep(2, "3. Install Graphics Driver", "Selecting Mesa Turnip + Zink for Adreno 610 GPU...", isDone = false),
            OptimizationStep(3, "4. Configure Render Settings", "Applying 2GB RAM, 8 chunk render distance & Fast graphics...", isDone = false),
            OptimizationStep(4, "5. Mobile Touch Controls", "Setting large touch buttons & medium sensitivity...", isDone = false)
        )
    }

    LaunchedEffect(Unit) {
        scope.launch {
            // STEP 1: Detect Device
            currentStepIndex = 0
            steps[0] = steps[0].copy(isRunning = true, progress = 0.3f)
            statusText = "Analyzing CPU architecture and system memory..."
            delay(400)

            val profile = DeviceProfileDetector.detect(context)
            detectedProfile = profile
            steps[0] = steps[0].copy(
                isDone = true,
                isRunning = false,
                progress = 1f,
                detail = "Detected: ${profile.socName} • ${profile.totalRamMb} MB RAM • ${profile.gpuRenderer}"
            )
            overallProgress = 0.2f
            delay(300)

            // STEP 2: Setup Java Runtimes
            currentStepIndex = 1
            steps[1] = steps[1].copy(isRunning = true)
            statusText = "Staging Java 8, 17, and 21 ARM64 JDKs..."
            val runtimes = javaManager.getInstalledRuntimes()
            for (r in runtimes) {
                javaManager.installRuntime(r) { p, msg ->
                    statusText = msg
                }
                delay(150)
            }
            steps[1] = steps[1].copy(
                isDone = true,
                isRunning = false,
                progress = 1f,
                detail = "Java 8, 17, 21 ARM64 ready in /maazcraft/java/"
            )
            overallProgress = 0.45f
            delay(300)

            // STEP 3: Install Graphics Driver
            currentStepIndex = 2
            steps[2] = steps[2].copy(isRunning = true)
            statusText = "Reading DriverDB.json and installing Turnip Zink..."
            val bestDriver = driverInstaller.getDriverForId("turnip-zink-adreno-610")
            driverInstaller.installDriver(bestDriver) { p, msg ->
                statusText = msg
            }
            prefs.selectedDriverId = bestDriver.id
            steps[2] = steps[2].copy(
                isDone = true,
                isRunning = false,
                progress = 1f,
                detail = "Installed: ${bestDriver.name} (Vulkan 1.3 + Zink OpenGL 4.6)"
            )
            overallProgress = 0.7f
            delay(300)

            // STEP 4: Configure Render Settings
            currentStepIndex = 3
            steps[3] = steps[3].copy(isRunning = true)
            statusText = "Optimizing for Snapdragon 680 (RAM=2GB, Chunks=8, Fast Graphics)..."
            prefs.allocatedRamMb = profile.recommendedRamMb
            prefs.renderDistance = profile.recommendedRenderDistance
            prefs.graphicsMode = profile.recommendedGraphics
            prefs.resolutionScale = 100
            delay(400)
            steps[3] = steps[3].copy(
                isDone = true,
                isRunning = false,
                progress = 1f,
                detail = "Applied RAM: ${profile.recommendedRamMb} MB • Render: ${profile.recommendedRenderDistance} chunks • Graphics: ${profile.recommendedGraphics}"
            )
            overallProgress = 0.88f
            delay(300)

            // STEP 5: Set Mobile Touch Controls
            currentStepIndex = 4
            steps[4] = steps[4].copy(isRunning = true)
            statusText = "Loading mobile_controls.json with large touch layout..."
            val defaultControls = controlManager.loadDefaultProfile()
            prefs.controlScale = defaultControls.buttonScale
            prefs.controlOpacity = defaultControls.buttonOpacity
            prefs.touchSensitivity = defaultControls.touchSensitivity
            prefs.hapticFeedback = defaultControls.vibrationFeedback
            delay(400)
            steps[4] = steps[4].copy(
                isDone = true,
                isRunning = false,
                progress = 1f,
                detail = "Touch Controls: Scale ${defaultControls.buttonScale}x • Opacity ${(defaultControls.buttonOpacity * 100).toInt()}% • Vibration ON"
            )
            overallProgress = 1.0f

            // Mark completed in preferences
            prefs.isFirstLaunchDone = true
            statusText = "Optimization complete! Snapdragon 680 preset configured."
            isDone = true
            delay(200)
        }
    }

    Dialog(
        onDismissRequest = { if (isDone) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark),
            color = BackgroundDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(PurplePrimary, PurpleAccent))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "MaazCraft Auto-Optimizer",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = "Optimizing for Snapdragon 680 + 8GB RAM",
                        fontSize = 14.sp,
                        color = PurpleAccent,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Progress Bar Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PurpleDarkBorder, PurplePrimary)))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Optimization Status",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                            Text(
                                text = "${(overallProgress * 100).toInt()}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PurpleAccent,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val animatedProgress by animateFloatAsState(targetValue = overallProgress, label = "p")
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = PurpleAccent,
                            trackColor = PurpleDarkBorder
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = statusText,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Step Cards
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    steps.forEach { step ->
                        StepItemCard(step = step)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Action
                AnimatedVisibility(visible = isDone) {
                    Button(
                        onClick = {
                            detectedProfile?.let { onCompleted(it) } ?: onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PurplePrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "START PLAYING MAAZCRAFT",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                if (!isDone) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = PurpleAccent,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Applying hardware tweaks...",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepItemCard(step: OptimizationStep) {
    val icon: ImageVector = when (step.stepIndex) {
        0 -> Icons.Default.Memory
        1 -> Icons.Default.Code
        2 -> Icons.Default.Speed
        3 -> Icons.Default.VideogameAsset
        else -> Icons.Default.TouchApp
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (step.isDone) SurfaceElevated else if (step.isRunning) PurpleContainer else SurfaceDark
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (step.isRunning) androidx.compose.foundation.BorderStroke(1.dp, PurpleAccent) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        if (step.isDone) SuccessGreen.copy(alpha = 0.2f)
                        else if (step.isRunning) PurplePrimary
                        else Color.DarkGray.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (step.isDone) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(20.dp)
                    )
                } else if (step.isRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = step.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (step.isDone || step.isRunning) TextPrimary else TextMuted
                )
                Text(
                    text = step.detail,
                    fontSize = 12.sp,
                    color = if (step.isDone) SuccessGreen else if (step.isRunning) TextSecondary else TextMuted,
                    maxLines = 2
                )
            }
        }
    }
}
