package com.orbital.app

import android.content.pm.PackageManager
import android.util.Log
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object AudioSessionTracker {
    private const val TAG = "AudioSessionTracker"

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Throwable) {
            false
        }
    }

    fun hasPermission(): Boolean {
        return try {
            if (Shizuku.isPreV11()) {
                false
            } else {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }
        } catch (e: Throwable) {
            false
        }
    }

    fun getActiveAudioSessionIds(): List<Int> {
        val sessionIds = mutableListOf<Int>()
        if (!hasPermission()) {
            Log.w(TAG, "Shizuku permission not granted. Cannot run dumpsys.")
            return sessionIds
        }

        try {
            // Explicitly cast nulls to prevent Overload Resolution Ambiguity in Kotlin
            val process = Shizuku.newProcess(
                arrayOf("sh", "-c", "dumpsys media.audio_flinger"),
                null as Array<String>?,
                null as String?
            )

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val sessionRegex = Regex("Session\\s+(\\d+)")

            // Replaced the while() loop with native Kotlin forEachLine
            reader.forEachLine { line ->
                val match = sessionRegex.find(line)
                if (match != null) {
                    val id = match.groupValues[1].toIntOrNull()
                    if (id != null && id > 0 && !sessionIds.contains(id)) {
                        sessionIds.add(id)
                        Log.i(TAG, "Discovered active AudioSession ID: $id")
                    }
                }
            }
            process.waitFor()
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to read AudioFlinger dump via Shizuku", e)
        }

        return sessionIds
    }
}