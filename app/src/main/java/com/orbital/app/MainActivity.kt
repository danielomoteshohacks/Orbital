package com.orbital.app

import android.app.Activity
import android.os.Bundle

class MainActivity : Activity() {

    // 1. Declare the external native function. 
    // The Kotlin compiler knows to look for this inside the loaded C++ library.
    private external fun startDspEngine()

    companion object {
        // 2. Load the 'liborbital.so' C++ binary built by our CMake script
        init {
            System.loadLibrary("orbital")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 3. Boot the zero-latency native audio engine!
        startDspEngine()
    }
}