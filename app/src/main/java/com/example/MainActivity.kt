// FILE: app/src/main/java/com/example/MainActivity.kt
package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.DeviceProfileDetector
import com.example.core.GitHubManagerService
import com.example.core.LaunchHelper
import com.example.core.ModpackManager
import com.example.core.PreferenceManager
import com.example.core.ServerManager
import com.example.core.VersionManager
import com.example.model.DeviceProfile
import com.example.ui.screens.GitHubScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ModpacksScreen
import com.example.ui.screens.ServersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VersionsScreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurpleContainer
import com.example.ui.theme.PurpleDarkBorder
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.screens.MainLandscapeScreen
import com.example.ui.wizard.OptimizationWizardDialog
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { PreferenceManager(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    var showOptimizationWizard by remember { mutableStateOf(!prefs.isFirstLaunchDone) }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        // Main 3-Column Landscape Interface
        MainLandscapeScreen(
            snackbarHostState = snackbarHostState,
            onOpenWizard = { showOptimizationWizard = true }
        )

        // Snackbar Host for real-time notifications
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(16.dp)
        )

        // Auto-Optimization Wizard Dialog
        if (showOptimizationWizard) {
            OptimizationWizardDialog(
                onDismiss = { showOptimizationWizard = false },
                onCompleted = { profile ->
                    showOptimizationWizard = false
                    scope.launch {
                        snackbarHostState.showSnackbar("${profile.socName} Profile applied! ${profile.recommendedRamMb}MB RAM • ${profile.recommendedRenderDistance} Chunks • ${profile.recommendedGraphics} Graphics")
                    }
                }
            )
        }
    }
}
