// FILE: app/src/main/java/com/example/core/ControlProfileManager.kt
package com.example.core

import android.content.Context
import com.example.model.ControlProfile
import com.example.model.TouchButton
import org.json.JSONObject
import java.io.File

class ControlProfileManager(private val context: Context) {

    private val controlsDir: File
        get() {
            val dir = File(context.getExternalFilesDir(null), "maazcraft/controls")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    fun loadDefaultProfile(): ControlProfile {
        return try {
            val jsonStr = context.assets.open("mobile_controls.json").bufferedReader().use { it.readText() }
            val root = JSONObject(jsonStr)
            val buttonsArray = root.getJSONArray("buttons")
            val btnList = mutableListOf<TouchButton>()

            for (i in 0 until buttonsArray.length()) {
                val b = buttonsArray.getJSONObject(i)
                btnList.add(
                    TouchButton(
                        id = b.getString("id"),
                        label = b.getString("label"),
                        keyCode = b.getInt("keyCode"),
                        x = b.getInt("x"),
                        y = b.getInt("y"),
                        width = b.getInt("width"),
                        height = b.getInt("height"),
                        type = b.getString("type")
                    )
                )
            }

            ControlProfile(
                profileName = root.getString("profileName"),
                version = root.getInt("version"),
                buttonScale = root.getDouble("buttonScale").toFloat(),
                buttonOpacity = root.getDouble("buttonOpacity").toFloat(),
                touchSensitivity = root.getDouble("touchSensitivity").toFloat(),
                vibrationFeedback = root.getBoolean("vibrationFeedback"),
                buttons = btnList
            )
        } catch (e: Exception) {
            fallbackProfile()
        }
    }

    private fun fallbackProfile(): ControlProfile {
        return ControlProfile(
            profileName = "Default Touch Controls",
            version = 3,
            buttonScale = 1.25f,
            buttonOpacity = 0.85f,
            touchSensitivity = 1.15f,
            vibrationFeedback = true,
            buttons = listOf(
                TouchButton("btn_up", "▲", 19, 60, 280, 55, 55, "DPAD"),
                TouchButton("btn_left", "◀", 21, 10, 330, 55, 55, "DPAD"),
                TouchButton("btn_right", "▶", 22, 110, 330, 55, 55, "DPAD"),
                TouchButton("btn_down", "▼", 20, 60, 380, 55, 55, "DPAD"),
                TouchButton("btn_sneak", "◇", 115, 60, 330, 55, 55, "DPAD_CENTER"),
                TouchButton("btn_jump", "JUMP", 62, 680, 330, 70, 70, "ACTION"),
                TouchButton("btn_attack", "PRI", 323, 600, 290, 60, 60, "ACTION"),
                TouchButton("btn_use", "SEC", 324, 600, 360, 60, 60, "ACTION"),
                TouchButton("btn_inv", "INV", 69, 360, 420, 55, 40, "HOTBAR"),
                TouchButton("btn_f3", "F3", 133, 10, 20, 45, 35, "SYSTEM"),
                TouchButton("btn_chat", "CHAT", 84, 60, 20, 50, 35, "SYSTEM"),
                TouchButton("btn_pause", "ESC", 111, 700, 20, 45, 35, "SYSTEM")
            )
        )
    }
}
