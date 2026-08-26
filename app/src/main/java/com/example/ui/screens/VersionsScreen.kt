// FILE: app/src/main/java/com/example/ui/screens/VersionsScreen.kt
package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.core.PreferenceManager
import com.example.core.VersionManager
import com.example.model.MinecraftVersion
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
import kotlinx.coroutines.launch

@Composable
fun VersionsScreen(
    versionManager: VersionManager,
    onVersionSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { PreferenceManager(context) }

    var versionsList by remember { mutableStateOf<List<MinecraftVersion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Releases", "Installed", "Popular"

    // Map of download states
    val downloadingMap = remember { mutableStateMapOf<String, Float>() }
    val downloadStatusMap = remember { mutableStateMapOf<String, String>() }

    fun loadVersions() {
        scope.launch {
            isLoading = true
            versionsList = versionManager.fetchVersionsList()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadVersions()
    }

    val filteredList = remember(versionsList, searchQuery, selectedFilter) {
        val popularIds = setOf("1.21.1", "1.20.1", "1.19.4", "1.16.5", "1.12.2", "1.8.9")
        versionsList.filter { v ->
            val matchesQuery = v.id.contains(searchQuery, ignoreCase = true) ||
                    v.description.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "Releases" -> v.type == "release"
                "Installed" -> v.isInstalled || downloadingMap.containsKey(v.id)
                "Popular" -> popularIds.contains(v.id)
                else -> true
            }
            matchesQuery && matchesFilter
        }
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
            Column {
                Text(
                    text = "Version Manager",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Official Mojang Manifest & Local Storage",
                    fontSize = 12.sp,
                    color = PurpleAccent
                )
            }

            IconButton(
                onClick = { loadVersions() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SurfaceElevated)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Manifest",
                    tint = PurpleAccent
                )
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search versions (e.g. 1.21.1, 1.16.5, 1.8.9)...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = PurpleAccent
                )
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

        // Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Popular", "Releases", "Installed").forEach { filter ->
                val isSelected = selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontSize = 12.sp) },
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

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = PurpleAccent)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Fetching Mojang version manifest...",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { version ->
                    val isSelected = prefs.selectedVersionId == version.id
                    val isDownloading = downloadingMap.containsKey(version.id)
                    val progress = downloadingMap[version.id] ?: 0f
                    val statusText = downloadStatusMap[version.id] ?: ""

                    VersionCard(
                        version = version,
                        isSelected = isSelected,
                        isDownloading = isDownloading,
                        downloadProgress = progress,
                        statusText = statusText,
                        onSelect = {
                            prefs.selectedVersionId = version.id
                            onVersionSelected(version.id)
                        },
                        onDownload = {
                            scope.launch {
                                downloadingMap[version.id] = 0.05f
                                downloadStatusMap[version.id] = "Starting download..."
                                val success = versionManager.downloadVersion(version) { p, msg ->
                                    downloadingMap[version.id] = p
                                    downloadStatusMap[version.id] = msg
                                }
                                if (success) {
                                    downloadingMap.remove(version.id)
                                    downloadStatusMap.remove(version.id)
                                    loadVersions()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun VersionCard(
    version: MinecraftVersion,
    isSelected: Boolean,
    isDownloading: Boolean,
    downloadProgress: Float,
    statusText: String,
    onSelect: () -> Unit,
    onDownload: () -> Unit
) {
    val isPopular = listOf("1.21.1", "1.20.1", "1.19.4", "1.16.5", "1.12.2", "1.8.9").contains(version.id)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PurpleContainer else SurfaceDark
        ),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                if (isSelected) listOf(PurpleAccent, PurplePrimary)
                else listOf(PurpleDarkBorder, PurpleDarkBorder)
            )
        )
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
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isPopular) PurplePrimary else SurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = version.id.take(4),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Minecraft ${version.id}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (isPopular) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(PurpleAccent.copy(alpha = 0.2f))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "POPULAR",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PurpleAccent
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${version.type.uppercase()} • Java ${version.javaVersion} required",
                            fontSize = 11.sp,
                            color = PurpleLight
                        )
                    }
                }

                // Active badge or Install button
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PurpleAccent)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SELECTED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                } else if (version.isInstalled) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SuccessGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "INSTALLED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = version.description,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Download Progress Bar
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
                    text = statusText,
                    fontSize = 11.sp,
                    color = CyanInfo,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Release: ${version.releaseTime.take(10)}",
                    fontSize = 10.sp,
                    color = TextMuted
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!version.isInstalled && !isDownloading) {
                        Button(
                            onClick = onDownload,
                            modifier = Modifier.height(34.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = PurpleAccent
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Download", fontSize = 11.sp, color = PurpleAccent)
                        }
                    }

                    Button(
                        onClick = onSelect,
                        modifier = Modifier.height(34.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) PurpleAccent else PurplePrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isSelected) "Active" else "Select",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else Color.White
                        )
                    }
                }
            }
        }
    }
}
