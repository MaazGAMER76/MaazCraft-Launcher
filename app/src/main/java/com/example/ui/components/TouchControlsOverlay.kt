// FILE: app/src/main/java/com/example/ui/components/TouchControlsOverlay.kt
package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanInfo
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@Composable
fun TouchControlsOverlay(
    scale: Float = 1.0f,
    opacity: Float = 0.85f,
    haptic: Boolean = true,
    fps: Int = 60,
    onButtonPress: (String) -> Unit = {},
    onChatOpen: () -> Unit = {},
    onInventoryOpen: () -> Unit = {},
    onPauseOpen: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var selectedHotbarSlot by remember { mutableIntStateOf(1) }

    fun triggerHaptic(actionName: String) {
        if (haptic) {
            try {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            } catch (e: Exception) {}
        }
        onButtonPress(actionName)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(opacity)
    ) {
        // TOP SYSTEM BAR: [F3] [CHAT] [KEY] ... [DEBUG INFO] ... [ESC]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TouchControlButton(
                    label = "F3",
                    width = 46.dp,
                    height = 32.dp,
                    onClick = { triggerHaptic("F3_TOGGLE") }
                )
                TouchControlButton(
                    label = "CHAT",
                    width = 54.dp,
                    height = 32.dp,
                    onClick = {
                        triggerHaptic("CHAT")
                        onChatOpen()
                    }
                )
                TouchControlButton(
                    label = "KEY",
                    width = 46.dp,
                    height = 32.dp,
                    onClick = { triggerHaptic("KEYBOARD") }
                )
                TouchControlButton(
                    label = "TAB",
                    width = 46.dp,
                    height = 32.dp,
                    onClick = { triggerHaptic("PLAYER_LIST") }
                )
            }

            // Central Telemetry
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "FPS: $fps | XYZ: 124.5 / 68.0 / -342.1",
                    color = CyanInfo,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // Right ESC
            TouchControlButton(
                label = "ESC",
                width = 48.dp,
                height = 32.dp,
                accent = true,
                onClick = {
                    triggerHaptic("ESCAPE")
                    onPauseOpen()
                }
            )
        }

        // BOTTOM LEFT: D-PAD CONTROLS
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 48.dp)
                .size((140 * scale).dp)
        ) {
            // UP
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size((48 * scale).dp)
            ) {
                DpadButton(label = "▲") { triggerHaptic("MOVE_FORWARD") }
            }
            // LEFT
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size((48 * scale).dp)
            ) {
                DpadButton(label = "◀") { triggerHaptic("MOVE_LEFT") }
            }
            // CENTER SNEAK
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size((42 * scale).dp)
            ) {
                DpadButton(label = "◇", isCenter = true) { triggerHaptic("TOGGLE_SNEAK") }
            }
            // RIGHT
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size((48 * scale).dp)
            ) {
                DpadButton(label = "▶") { triggerHaptic("MOVE_RIGHT") }
            }
            // DOWN
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size((48 * scale).dp)
            ) {
                DpadButton(label = "▼") { triggerHaptic("MOVE_BACKWARD") }
            }
        }

        // BOTTOM CENTER: 9-SLOT HOTBAR & INVENTORY BUTTON
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Inventory pill
            TouchControlButton(
                label = "INVENTORY (E)",
                width = 110.dp,
                height = 28.dp,
                onClick = {
                    triggerHaptic("INVENTORY")
                    onInventoryOpen()
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 1-9 Hotbar bar
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .border(1.dp, PurpleAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                val hotbarItems = listOf("🗡️", "⛏️", "🪓", "🏹", "🍞", "🥩", "🧱", "🔥", "💧")
                for (i in 1..9) {
                    val isSelected = selectedHotbarSlot == i
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) PurplePrimary.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.1f))
                            .border(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) PurpleAccent else Color.Gray.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable {
                                selectedHotbarSlot = i
                                triggerHaptic("HOTBAR_SLOT_$i")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = hotbarItems[i - 1],
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // BOTTOM RIGHT: ACTION BUTTONS (PRI, SEC, JUMP)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 48.dp)
                .size((150 * scale).dp)
        ) {
            // PRI (Attack / Break)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size((56 * scale).dp)
            ) {
                ActionButton(
                    label = "PRI",
                    sub = "Attack",
                    color = Color(0xFFE53935)
                ) { triggerHaptic("ATTACK_PRIMARY") }
            }

            // SEC (Place / Use)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size((56 * scale).dp)
            ) {
                ActionButton(
                    label = "SEC",
                    sub = "Use",
                    color = Color(0xFF1E88E5)
                ) { triggerHaptic("USE_SECONDARY") }
            }

            // JUMP
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size((64 * scale).dp)
            ) {
                ActionButton(
                    label = "JUMP",
                    sub = "Space",
                    color = PurplePrimary
                ) { triggerHaptic("JUMP") }
            }
        }
    }
}

@Composable
private fun TouchControlButton(
    label: String,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    accent: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width, height)
            .clip(RoundedCornerShape(6.dp))
            .background(if (accent) PurplePrimary else Color.Black.copy(alpha = 0.65f))
            .border(1.dp, if (accent) PurpleAccent else Color.White.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun DpadButton(
    label: String,
    isCenter: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isPressed) PurpleAccent.copy(alpha = 0.8f)
                else if (isCenter) Color.DarkGray.copy(alpha = 0.7f)
                else Color.Black.copy(alpha = 0.65f)
            )
            .border(
                1.dp,
                if (isPressed) PurpleAccent else Color.White.copy(alpha = 0.35f),
                RoundedCornerShape(8.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = if (isCenter) 13.sp else 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ActionButton(
    label: String,
    sub: String,
    color: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(if (isPressed) Color.White.copy(alpha = 0.9f) else color.copy(alpha = 0.75f))
            .border(
                1.5.dp,
                if (isPressed) Color.White else color,
                CircleShape
            )
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                color = if (isPressed) Color.Black else Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = sub,
                color = if (isPressed) Color.Black.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.7f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
