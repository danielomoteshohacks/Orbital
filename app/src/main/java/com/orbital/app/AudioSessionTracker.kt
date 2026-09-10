package com.orbital.app

import android.content.pm.PackageManager
import android.util.Log
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object AudioSessionTracker {
    private const val TAG = "AudioSessionTracker"

    // 1. Verify if the Shizuku server is running on the device
    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }

    // 2. Check if the user granted Shizuku permissions to Orbital
    fun hasPermission(): Boolean {
        return if (Shizuku.isPreV11()) {
            false
        } else {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }
    }

    // 3. Query AudioFlinger to find active audio session IDs
    fun getActiveAudioSessionIds(): List<Int> {
        val sessionIds = mutableListOf<Int>()
        if (!hasPermission()) {
            Log.w(TAG, "Shizuku permission not granted. Cannot run dumpsys.")
            return sessionIds
        }

        try {
            // Run dumpsys through Shizuku's elevated shell process
            val process = Shizuku.newProcess(
                arrayOf("sh", "-c", "dumpsys media.audio_flinger"), 
                null, 
                null
            )
            
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            
            // Matches audio session allocations in AudioFlinger track dumps
            val sessionRegex = Regex("""Session\s+(\d+)""")

            while (reader.readLine().also { line = it } != null) {
                line?.let { l ->
                    val match = sessionRegex.find(l)
                    if (match != null) {
                        val id = match.groupValues[1].toIntOrNull()
                        if (id != null && id > 0 && !sessionIds.contains(id)) {
                            sessionIds.add(id)
                            Log.i(TAG, "Discovered active AudioSession ID: $id")
                        }
                    }
                }
            }
            reader.close()
            process.waitFor()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read AudioFlinger dump via Shizuku", e)
        }

        return sessionIds
    }
}