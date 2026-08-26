// FILE: app/src/main/java/com/example/core/DeviceProfileDetector.kt
package com.example.core

import android.content.Context
import com.example.model.DeviceProfile

/**
 * Compatibility delegate for DeviceProfileDetector
 */
object DeviceProfileDetector {
    fun detect(context: Context): DeviceProfile {
        return DeviceProfileManager(context).getOrDetect()
    }
}
