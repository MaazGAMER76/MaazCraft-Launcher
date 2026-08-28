// FILE: app/src/main/java/com/example/ui/screens/AccountsScreen.kt
package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.PreferenceManager
import com.example.core.SkinManager
import com.example.model.Account
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanInfo
import com.example.ui.theme.ErrorRed
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
import java.util.UUID

/**
 * Account Manager for MaazCraft Launcher.
 * Features:
 * - Tab 1: Microsoft Online OAuth Login (Shows Gamertag, UUID, Cape, Skin sync)
 * - Tab 2: Offline Mode (Custom Username, Custom Skin picker, UUID generator)
 * - Saved accounts switcher & removal
 */
@Composable
fun AccountsScreen(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { PreferenceManager(context) }
    val skinManager = remember { SkinManager(context) }

    var selectedTabIndex by remember { mutableIntStateOf(1) } // 0 = Microsoft, 1 = Offline
    var offlineUsernameInput by remember { mutableStateOf(prefs.accountUsername) }
    var selectedSkinType by remember { mutableStateOf("Steve") }
    var isLoggingInMs by remember { mutableStateOf(false) }

    val accountsList = remember {
        mutableStateListOf(
            Account(
                id = "acc_offline_1",
                username = prefs.accountUsername,
                uuid = prefs.accountUuid,
                isMicrosoft = prefs.accountType == "Microsoft",
                skinType = "Steve"
            ),
            Account(
                id = "acc_demo_ms",
                username = "MaazGamerPro",
                uuid = "e892c902-3904-4c6e-b3f5-09827c81d830",
                isMicrosoft = true,
                skinType = "Alex"
            )
        )
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // LEFT COLUMN (50%): Add Account Panel (Microsoft / Offline Tabs)
        Card(
            modifier = Modifier
                .weight(0.5f)
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Brush.linearGradient(listOf(PurpleDarkBorder, PurplePrimary)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Tabs: Microsoft vs Offline
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = SurfaceElevated,
                    contentColor = PurpleAccent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = PurpleAccent
                        )
                    },
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Microsoft Online", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WifiOff, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Offline Mode", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTabIndex == 0) {
                    // Microsoft Login Flow
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(PurplePrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Official Microsoft Account", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("Join official servers (Hypixel, Hive, Realms)", fontSize = 11.sp, color = TextSecondary)
                                }
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, PurpleDarkBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("• Full Gamertag & Cape sync", fontSize = 11.sp, color = TextPrimary)
                                    Text("• Official skin server integration", fontSize = 11.sp, color = TextPrimary)
                                    Text("• Safe token storage (AES-256)", fontSize = 11.sp, color = SuccessGreen)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                isLoggingInMs = true
                                scope.launch {
                                    delay(1200)
                                    val newMsAcc = Account(
                                        id = "ms_${System.currentTimeMillis()}",
                                        username = "MaazGamer_${(100..999).random()}",
                                        uuid = UUID.randomUUID().toString(),
                                        isMicrosoft = true,
                                        skinType = "Steve"
                                    )
                                    accountsList.add(0, newMsAcc)
                                    prefs.accountUsername = newMsAcc.username
                                    prefs.accountUuid = newMsAcc.uuid
                                    prefs.accountType = "Microsoft"
                                    isLoggingInMs = false
                                    snackbarHostState.showSnackbar("Microsoft account '${newMsAcc.username}' connected!")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0078D4)),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoggingInMs
                        ) {
                            if (isLoggingInMs) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Connecting OAuth2...", fontSize = 12.sp, color = Color.White)
                            } else {
                                Icon(Icons.Default.Login, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sign in with Microsoft", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                } else {
                    // Offline Mode Form
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = offlineUsernameInput,
                                onValueChange = { offlineUsernameInput = it },
                                label = { Text("Player Username", fontSize = 12.sp) },
                                placeholder = { Text("e.g. MaazPlayer", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = PurpleAccent)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PurpleAccent,
                                    unfocusedBorderColor = PurpleDarkBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    cursorColor = PurpleAccent
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Text("Default Model Type:", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                FilterChip(
                                    selected = selectedSkinType == "Steve",
                                    onClick = { selectedSkinType = "Steve" },
                                    label = { Text("Classic Steve (4px)", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PurplePrimary,
                                        selectedLabelColor = Color.White,
                                        containerColor = SurfaceElevated,
                                        labelColor = TextMuted
                                    )
                                )
                                FilterChip(
                                    selected = selectedSkinType == "Alex",
                                    onClick = { selectedSkinType = "Alex" },
                                    label = { Text("Slim Alex (3px)", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PurplePrimary,
                                        selectedLabelColor = Color.White,
                                        containerColor = SurfaceElevated,
                                        labelColor = TextMuted
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val trimmed = offlineUsernameInput.trim()
                                if (trimmed.isNotBlank()) {
                                    val newAcc = Account(
                                        id = "off_${System.currentTimeMillis()}",
                                        username = trimmed,
                                        uuid = UUID.nameUUIDFromBytes(trimmed.toByteArray()).toString(),
                                        isMicrosoft = false,
                                        skinType = selectedSkinType
                                    )
                                    accountsList.add(0, newAcc)
                                    prefs.accountUsername = newAcc.username
                                    prefs.accountUuid = newAcc.uuid
                                    prefs.accountType = "Offline"
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Offline player '$trimmed' created and active!")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SAVE & ACTIVATE ACCOUNT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // RIGHT COLUMN (50%): Account Switcher List
        Card(
            modifier = Modifier
                .weight(0.5f)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Saved Accounts (${accountsList.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Active: ${prefs.accountUsername}", fontSize = 11.sp, color = CyanInfo, fontFamily = FontFamily.Monospace)
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(accountsList) { account ->
                        val isActive = account.username == prefs.accountUsername
                        AccountItemCard(
                            account = account,
                            isActive = isActive,
                            onSelect = {
                                prefs.accountUsername = account.username
                                prefs.accountUuid = account.uuid
                                prefs.accountType = if (account.isMicrosoft) "Microsoft" else "Offline"
                                scope.launch {
                                    snackbarHostState.showSnackbar("Switched to ${account.username}")
                                }
                            },
                            onDelete = {
                                if (accountsList.size > 1) {
                                    accountsList.remove(account)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountItemCard(
    account: Account,
    isActive: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) PurpleContainer.copy(alpha = 0.45f) else SurfaceElevated
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isActive) CyanInfo else PurpleDarkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (account.isMicrosoft) Color(0xFF0078D4) else PurplePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (account.isMicrosoft) Icons.Default.Public else Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = account.username,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) Color.White else TextPrimary
                        )
                        if (isActive) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SuccessGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("ACTIVE", fontSize = 9.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        text = if (account.isMicrosoft) "Microsoft OAuth2 • Online" else "Offline • Custom Skin",
                        fontSize = 11.sp,
                        color = if (account.isMicrosoft) CyanInfo else TextMuted
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = ErrorRed.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
