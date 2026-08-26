// FILE: app/src/main/java/com/example/ui/screens/ServersScreen.kt
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalCellularAlt
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.core.PreferenceManager
import com.example.core.ServerManager
import com.example.model.ServerInfo
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
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.launch

@Composable
fun ServersScreen(
    serverManager: ServerManager,
    onConnectServer: (ServerInfo) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { PreferenceManager(context) }

    var serversList by remember { mutableStateOf<List<ServerInfo>>(emptyList()) }
    var isPingingAll by remember { mutableStateOf(false) }

    // Add / Edit Dialog state
    var showEditDialog by remember { mutableStateOf(false) }
    var editingServer by remember { mutableStateOf<ServerInfo?>(null) }
    var serverNameInput by remember { mutableStateOf("") }
    var serverHostInput by remember { mutableStateOf("") }
    var serverPortInput by remember { mutableStateOf("25565") }

    val pingsMap = remember { mutableStateMapOf<String, Long>() }

    fun refreshServers() {
        serversList = serverManager.loadServers()
    }

    fun pingAll() {
        scope.launch {
            isPingingAll = true
            serversList.forEach { s ->
                val ms = serverManager.pingServer(s.host, s.port)
                pingsMap[s.id] = ms
            }
            isPingingAll = false
        }
    }

    LaunchedEffect(Unit) {
        refreshServers()
        pingAll()
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
                    text = "Server Manager",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Multiplayer servers & low-latency ping",
                    fontSize = 12.sp,
                    color = PurpleAccent
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { pingAll() },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated)
                ) {
                    if (isPingingAll) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = PurpleAccent, strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Ping All", tint = PurpleAccent)
                    }
                }

                Button(
                    onClick = {
                        editingServer = null
                        serverNameInput = ""
                        serverHostInput = ""
                        serverPortInput = "25565"
                        showEditDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Server", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Servers List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(serversList, key = { it.id }) { server ->
                val currentPing = pingsMap[server.id] ?: server.pingMs

                ServerItemCard(
                    server = server,
                    pingMs = currentPing,
                    onConnect = { onConnectServer(server) },
                    onEdit = {
                        editingServer = server
                        serverNameInput = server.name
                        serverHostInput = server.host
                        serverPortInput = server.port.toString()
                        showEditDialog = true
                    },
                    onDelete = {
                        serverManager.deleteServer(server.id)
                        refreshServers()
                    }
                )
            }
        }
    }

    // ADD / EDIT SERVER DIALOG
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = if (editingServer != null) "Edit Minecraft Server" else "Add Minecraft Server",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = serverNameInput,
                        onValueChange = { serverNameInput = it },
                        label = { Text("Server Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurpleAccent,
                            unfocusedBorderColor = PurpleDarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = serverHostInput,
                        onValueChange = { serverHostInput = it },
                        label = { Text("Server Address / Host (IP)") },
                        placeholder = { Text("e.g. mc.hypixel.net") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurpleAccent,
                            unfocusedBorderColor = PurpleDarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = serverPortInput,
                        onValueChange = { serverPortInput = it },
                        label = { Text("Port") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurpleAccent,
                            unfocusedBorderColor = PurpleDarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (serverHostInput.isNotBlank()) {
                            val newId = editingServer?.id ?: "server_${System.currentTimeMillis()}"
                            val port = serverPortInput.toIntOrNull() ?: 25565
                            val s = ServerInfo(
                                id = newId,
                                name = serverNameInput.ifBlank { serverHostInput },
                                host = serverHostInput.trim(),
                                port = port,
                                version = "1.8 - 1.21.x",
                                motd = "§aMaazCraft Custom Multiplayer Server",
                                onlinePlayers = (50..500).random(),
                                maxPlayers = 1000,
                                pingMs = (25..65).random().toLong(),
                                featured = false
                            )
                            serverManager.saveServer(s)
                            showEditDialog = false
                            refreshServers()
                            pingAll()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Save Server")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
private fun ServerItemCard(
    server: ServerInfo,
    pingMs: Long,
    onConnect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val pingColor = when {
        pingMs < 50 -> SuccessGreen
        pingMs < 100 -> WarningAmber
        else -> ErrorRed
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                if (server.featured) listOf(PurpleAccent.copy(alpha = 0.5f), PurpleDarkBorder)
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
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PurpleContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = PurpleAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = server.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (server.featured) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(PurpleAccent.copy(alpha = 0.2f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "FEATURED",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PurpleAccent
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${server.host}:${server.port}",
                            fontSize = 11.sp,
                            color = PurpleLight,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Ping Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(pingColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${pingMs}ms",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = pingColor,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // MOTD
            Text(
                text = cleanMinecraftMotd(server.motd),
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Actions & Player Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🟢 ${server.onlinePlayers} / ${server.maxPlayers} Online",
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }

                    if (!server.featured) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                        }
                    }

                    Button(
                        onClick = onConnect,
                        modifier = Modifier.height(34.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Connect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun cleanMinecraftMotd(motd: String): String {
    // Strip Minecraft formatting codes §[0-9a-fk-or]
    return motd.replace(Regex("§[0-9a-fk-or]"), "")
}
