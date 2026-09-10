package com.orbital.app

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import rikka.shizuku.Shizuku

class MainActivity : Activity() {

    private val TAG = "OrbitalDSP"

    // 1. Declare the external native function. 
    private external fun startDspEngine()

    companion object {
        init {
            System.loadLibrary("orbital")
        }
        private const val SHIZUKU_REQUEST_CODE = 1000
    }

    // 2. Create the Shizuku Permission Listener
    private val shizukuPermissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == SHIZUKU_REQUEST_CODE) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Shizuku Permission Granted! Querying AudioFlinger...")
                val sessionIds = AudioSessionTracker.getActiveAudioSessionIds()
                Log.i(TAG, "Found Active Media Sessions: $sessionIds")
            } else {
                Log.e(TAG, "Shizuku Permission Denied. Cannot bypass DRM.")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 3. Register the permission listener
        Shizuku.addRequestPermissionResultListener(shizukuPermissionListener)
        
        // 4. Check if Shizuku is alive, then request permission or grab sessions
        if (AudioSessionTracker.isShizukuAvailable()) {
            if (AudioSessionTracker.hasPermission()) {
                val sessionIds = AudioSessionTracker.getActiveAudioSessionIds()
                Log.i(TAG, "Found Active Media Sessions: $sessionIds")
            } else {
                Shizuku.requestPermission(SHIZUKU_REQUEST_CODE)
            }
        } else {
            Log.e(TAG, "Shizuku is not running on this device. Start it via Wireless Debugging.")
        }

        // 5. Boot the zero-latency native audio engine!
        startDspEngine()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 6. Clean up the listener to prevent memory leaks when app closes
        Shizuku.removeRequestPermissionResultListener(shizukuPermissionListener)
    }
}