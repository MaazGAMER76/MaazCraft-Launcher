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
import com.example.ui.screens.ZalithLandscapeScreen
import com.example.ui.wizard.OptimizationWizardDialog
import kotlinx.coroutines.launch

enum class NavigationTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.SportsEsports),
    VERSIONS("Versions", Icons.Default.List),
    MODPACKS("Modpacks", Icons.Default.Widgets),
    SERVERS("Servers", Icons.Default.Dns),
    GITHUB("GitHub Hub", Icons.Default.CloudDownload),
    SETTINGS("Settings", Icons.Default.Settings)
}

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
    val launchHelper = remember { LaunchHelper(context) }
    val versionManager = remember { VersionManager(context) }
    val modpackManager = remember { ModpackManager(context) }
    val serverManager = remember { ServerManager(context) }
    val gitHubService = remember { GitHubManagerService(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableStateOf(NavigationTab.HOME) }
    var showOptimizationWizard by remember { mutableStateOf(!prefs.isFirstLaunchDone) }
    var useZalithLandscapeLayout by remember { mutableStateOf(true) }

    if (useZalithLandscapeLayout) {
        Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
            ZalithLandscapeScreen(
                launchHelper = launchHelper,
                versionManager = versionManager,
                modpackManager = modpackManager,
                serverManager = serverManager,
                gitHubService = gitHubService,
                snackbarHostState = snackbarHostState,
                onOpenWizard = { showOptimizationWizard = true }
            )

            // AUTO-OPTIMIZATION WIZARD DIALOG
            if (showOptimizationWizard) {
                OptimizationWizardDialog(
                    onDismiss = { showOptimizationWizard = false },
                    onCompleted = { profile ->
                        showOptimizationWizard = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Snapdragon 680 Profile applied! 2GB RAM • 8 Chunks • Fast Graphics")
                        }
                    }
                )
            }
        }
    } else {
        Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                contentColor = TextPrimary,
                tonalElevation = 8.dp,
                modifier = Modifier.height(72.dp)
            ) {
                NavigationTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PurpleAccent,
                            selectedTextColor = PurpleAccent,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = PurpleContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundDark)
        ) {
            when (selectedTab) {
                NavigationTab.HOME -> {
                    HomeScreen(
                        launchHelper = launchHelper,
                        onNavigateToVersions = { selectedTab = NavigationTab.VERSIONS },
                        onNavigateToSettings = { selectedTab = NavigationTab.SETTINGS },
                        onReOptimize = { showOptimizationWizard = true }
                    )
                }
                NavigationTab.VERSIONS -> {
                    VersionsScreen(
                        versionManager = versionManager,
                        onVersionSelected = { verId ->
                            selectedTab = NavigationTab.HOME
                            scope.launch {
                                snackbarHostState.showSnackbar("Target version set to Minecraft $verId")
                            }
                        }
                    )
                }
                NavigationTab.MODPACKS -> {
                    ModpacksScreen(
                        modpackManager = modpackManager,
                        onModpackSelected = { pack ->
                            selectedTab = NavigationTab.HOME
                            scope.launch {
                                snackbarHostState.showSnackbar("Loaded ${pack.name} (${pack.loader})")
                            }
                        }
                    )
                }
                NavigationTab.SERVERS -> {
                    ServersScreen(
                        serverManager = serverManager,
                        onConnectServer = { s ->
                            selectedTab = NavigationTab.HOME
                            scope.launch {
                                snackbarHostState.showSnackbar("Ready to connect to ${s.name} (${s.host})")
                            }
                        }
                    )
                }
                NavigationTab.GITHUB -> {
                    GitHubScreen(
                        gitHubService = gitHubService
                    )
                }
                NavigationTab.SETTINGS -> {
                    SettingsScreen(
                        onReOptimize = { showOptimizationWizard = true }
                    )
                }
            }

            // AUTO-OPTIMIZATION WIZARD DIALOG
            if (showOptimizationWizard) {
                OptimizationWizardDialog(
                    onDismiss = { showOptimizationWizard = false },
                    onCompleted = { profile ->
                        showOptimizationWizard = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Snapdragon 680 Profile applied! 2GB RAM • 8 Chunks • Fast Graphics")
                        }
                    }
                )
            }
        }
    }
}
}
