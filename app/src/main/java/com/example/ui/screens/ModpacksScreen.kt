// FILE: app/src/main/java/com/example/ui/screens/ModpacksScreen.kt
package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ModpackManager
import com.example.core.PreferenceManager
import com.example.model.Modpack
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
import kotlinx.coroutines.launch

@Composable
fun ModpacksScreen(
    modpackManager: ModpackManager,
    onModpackSelected: (Modpack) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { PreferenceManager(context) }

    var modpacksList by remember { mutableStateOf<List<Modpack>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val downloadingMap = remember { mutableStateMapOf<String, Float>() }
    val downloadStatusMap = remember { mutableStateMapOf<String, String>() }

    fun refreshModpacks() {
        modpacksList = modpackManager.loadModpacks()
    }

    LaunchedEffect(Unit) {
        refreshModpacks()
    }

    // Zip file import launcher
    val zipPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val pack = modpackManager.importFromZipUri(uri, "custom_modpack.zip") { p, msg ->
                    downloadingMap["imported_temp"] = p
                    downloadStatusMap["imported_temp"] = msg
                }
                downloadingMap.remove("imported_temp")
                downloadStatusMap.remove("imported_temp")
                if (pack != null) {
                    refreshModpacks()
                }
            }
        }
    }

    val filteredList = remember(modpacksList, searchQuery, selectedCategory) {
        modpacksList.filter { m ->
            val matchesQuery = m.name.contains(searchQuery, ignoreCase = true) ||
                    m.description.contains(searchQuery, ignoreCase = true) ||
                    m.tags.any { it.contains(searchQuery, ignoreCase = true) }
            val matchesCat = when (selectedCategory) {
                "Performance" -> m.category.contains("Performance", ignoreCase = true)
                "RPG / Adventure" -> m.category.contains("RPG", ignoreCase = true) || m.category.contains("Adventure", ignoreCase = true)
                "Tech & Magic" -> m.category.contains("Tech", ignoreCase = true) || m.category.contains("Pokemon", ignoreCase = true)
                else -> true
            }
            matchesQuery && matchesCat
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Modpack Manager",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "CurseForge & Modrinth auto-installer",
                    fontSize = 12.sp,
                    color = PurpleAccent
                )
            }

            // Import .zip button
            Button(
                onClick = { zipPickerLauncher.launch("application/zip") },
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Import .ZIP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search modpacks (Fabulously Optimized, RLCraft, ATM9)...") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = PurpleAccent)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PurpleAccent,
                unfocusedBorderColor = PurpleDarkBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Categories
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Performance", "RPG / Adventure", "Tech & Magic").forEach { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurplePrimary,
                        selectedLabelColor = Color.White,
                        containerColor = SurfaceElevated,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        selectedBorderColor = PurpleAccent,
                        borderColor = PurpleDarkBorder
                    )
                )
            }
        }

        // Modpack Cards List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredList, key = { it.id }) { modpack ->
                val isDownloading = downloadingMap.containsKey(modpack.id)
                val progress = downloadingMap[modpack.id] ?: 0f
                val statusMsg = downloadStatusMap[modpack.id] ?: ""

                ModpackItemCard(
                    modpack = modpack,
                    isDownloading = isDownloading,
                    downloadProgress = progress,
                    statusMessage = statusMsg,
                    onInstall = {
                        scope.launch {
                            downloadingMap[modpack.id] = 0.05f
                            downloadStatusMap[modpack.id] = "Starting download..."
                            val success = modpackManager.downloadAndInstall(modpack) { p, msg ->
                                downloadingMap[modpack.id] = p
                                downloadStatusMap[modpack.id] = msg
                            }
                            downloadingMap.remove(modpack.id)
                            downloadStatusMap.remove(modpack.id)
                            if (success) {
                                refreshModpacks()
                            }
                        }
                    },
                    onPlay = {
                        prefs.selectedVersionId = modpack.version
                        prefs.selectedModLoader = modpack.loader
                        onModpackSelected(modpack)
                    }
                )
            }
        }
    }
}

@Composable
private fun ModpackItemCard(
    modpack: Modpack,
    isDownloading: Boolean,
    downloadProgress: Float,
    statusMessage: String,
    onInstall: () -> Unit,
    onPlay: () -> Unit
) {
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
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (modpack.loader == "Fabric") Color(0xFF2E7D32) else Color(0xFFC2185B)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (modpack.loader == "Fabric") "FAB" else "FRG",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = modpack.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "MC ${modpack.version} • ${modpack.loader} • by ${modpack.author}",
                            fontSize = 11.sp,
                            color = PurpleLight
                        )
                    }
                }

                // Size Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${modpack.modsCount} mods • ${modpack.sizeMb.toInt()} MB",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = modpack.description,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tags Pill Row
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                modpack.tags.take(3).forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PurpleContainer.copy(alpha = 0.5f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = tag, fontSize = 9.sp, color = PurpleAccent)
                    }
                }
            }

            // Progress Indicator
            if (isDownloading) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { downloadProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = PurpleAccent,
                    trackColor = PurpleDarkBorder
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = statusMessage,
                    fontSize = 11.sp,
                    color = CyanInfo,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${modpack.downloads} downloads",
                    fontSize = 11.sp,
                    color = TextMuted
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!modpack.isInstalled && !isDownloading) {
                        Button(
                            onClick = onInstall,
                            modifier = Modifier.height(36.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Install", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (modpack.isInstalled) {
                        Button(
                            onClick = onPlay,
                            modifier = Modifier.height(36.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Launch Modpack",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
