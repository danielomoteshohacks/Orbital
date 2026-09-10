plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.orbital.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.orbital.app"
        minSdk = 31 // Android 12 minimum for advanced audio APIs
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        externalNativeBuild {
            cmake {
                arguments += "-DANDROID_STL=c++_shared"
            }
        }
    }

    buildFeatures {
        prefab = true // Allows pulling pre-compiled C++ libraries like Oboe
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("com.google.oboe:oboe:1.9.0") // The C++ audio pipeline
}