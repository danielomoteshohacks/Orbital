package com.orbital.app

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import rikka.shizuku.Shizuku

class MainActivity : Activity() {

    private val TAG = "OrbitalDSP"
    private external fun startDspEngine()

    companion object {
        init {
            System.loadLibrary("orbital")
        }
        private const val SHIZUKU_REQUEST_CODE = 1000
    }

    // Strictly defined object interface to prevent Kotlin SAM conversion crashes
    private val shizukuPermissionListener = object : Shizuku.OnRequestPermissionResultListener {
        override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
            if (requestCode == SHIZUKU_REQUEST_CODE) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    Log.i("OrbitalDSP", "Shizuku Permission Granted! Querying AudioFlinger...")
                    val sessionIds = AudioSessionTracker.getActiveAudioSessionIds()
                    Log.i("OrbitalDSP", "Found Active Media Sessions: $sessionIds")
                } else {
                    Log.e("OrbitalDSP", "Shizuku Permission Denied. Cannot bypass DRM.")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Shizuku.addRequestPermissionResultListener(shizukuPermissionListener)
        
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

        startDspEngine()
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(shizukuPermissionListener)
    }
}