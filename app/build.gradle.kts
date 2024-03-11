plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.lizongying.mytv0"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lizongying.mytv0"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"


    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    val media3Version = "1.3.0-rc01"
    implementation("androidx.media3:media3-ui:$media3Version")

    // For media playback using ExoPlayer
    implementation("androidx.media3:media3-exoplayer:$media3Version")

    // For HLS playback support with ExoPlayer
    implementation("androidx.media3:media3-exoplayer-hls:$media3Version")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.leanback:leanback:1.0.0")
    implementation("com.github.bumptech.glide:glide:4.11.0")

    // 21:2.9.0 17:2.6.4
    val retrofit2Version = "2.6.4"
    implementation("com.squareup.retrofit2:converter-gson:$retrofit2Version")
    implementation ("com.squareup.retrofit2:converter-protobuf:$retrofit2Version")
    implementation ("com.squareup.retrofit2:retrofit:$retrofit2Version")

    // For yunos
    val exoplayerVersion = "2.13.3"
    implementation("com.google.android.exoplayer:exoplayer-ui:$exoplayerVersion")
    implementation("com.google.android.exoplayer:exoplayer-core:$exoplayerVersion")
    implementation("com.google.android.exoplayer:exoplayer-hls:$exoplayerVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0-RC")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}