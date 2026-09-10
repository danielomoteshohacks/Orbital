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
            // Shizuku v13+ made newProcess private. We use reflection to force access.
            val clazz = Class.forName("rikka.shizuku.Shizuku")
            val method = clazz.getDeclaredMethod(
                "newProcess", 
                Array<String>::class.java, 
                Array<String>::class.java, 
                String::class.java
            )
            method.isAccessible = true
            
            // Execute the shell command as the Shizuku super-user
            val process = method.invoke(
                null, 
                arrayOf("sh", "-c", "dumpsys media.audio_flinger"), 
                null, 
                null
            ) as Process

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val sessionRegex = Regex("Session\\s+(\\d+)")

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