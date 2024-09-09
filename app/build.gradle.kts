import java.io.BufferedReader

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.lizongying.mytv0"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lizongying.mytv0"
        minSdk = 19
        targetSdk = 34
        versionCode = getVersionCode()
        versionName = getVersionName()
        multiDexEnabled = true
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
        // Flag to enable support for the new language APIs

        // For AGP 4.1+
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

fun getVersionCode(): Int {
    return try {
        val process = Runtime.getRuntime().exec("git describe --tags --always")
        process.waitFor()
        val arr = (process.inputStream.bufferedReader().use(BufferedReader::readText).trim()
            .replace("v", "").replace(".", " ").replace("-", " ") + " 0").split(" ")
        val versionCode =
            arr[0].toInt() * 16777216 + arr[1].toInt() * 65536 + arr[2].toInt() * 256 + arr[3].toInt()
        versionCode
    } catch (ignored: Exception) {
        1
    }
}

fun getVersionName(): String {
    return try {
        val process = Runtime.getRuntime().exec("git describe --tags --always")
        process.waitFor()
        val versionName = process.inputStream.bufferedReader().use(BufferedReader::readText).trim()
            .removePrefix("v")
        versionName.ifEmpty {
            "1.0.0"
        }
    } catch (ignored: Exception) {
        "1.0.0"
    }
}

task("modifySource") {
    val net = project.findProperty("net") ?: ""
    println("net: $net")

    val channels = when (net) {
        "ipv6" -> "assets/ipv6.txt"
        "mobile" -> "assets/mobile.txt"
        else -> "assets/common.txt"
    }

    println("channels: $channels")

    inputs.file(channels)
    outputs.file("src/main/res/raw/channels.txt")
    doLast {
        if (channels.isNotEmpty()) {
            val sourceFile = file(channels)
            val targetFile = file("src/main/res/raw/channels.txt")
            targetFile.writeText(sourceFile.readText())
            println(targetFile.readText())
        }

        val url = when (net) {
            "ipv6" -> "DEFAULT_CONFIG_URL = \"https://live.fanmingming.com/tv/m3u/ipv6.m3u\""
            "mobile" -> "DEFAULT_CONFIG_URL = \"https://live.fanmingming.com/tv/m3u/itv.m3u\""
            else -> ""
        }

        if (url.isNotEmpty()) {
            val f = file("src/main/java/com/lizongying/mytv0/SP.kt")
            f.writeText(f.readText().replace("DEFAULT_CONFIG_URL = \"\"", url))
        }
    }
}

tasks.whenTaskAdded {
    if (name == "assembleRelease") {
        dependsOn("modifySource")
        doLast {
            val net = project.findProperty("net") ?: ""
            println("net: $net")

            val url = when (net) {
                "ipv6" -> "DEFAULT_CONFIG_URL = \"https://live.fanmingming.com/tv/m3u/ipv6.m3u\""
                "mobile" -> "DEFAULT_CONFIG_URL = \"https://live.fanmingming.com/tv/m3u/itv.m3u\""
                else -> ""
            }

            if (url.isNotEmpty()) {
                val f = file("src/main/java/com/lizongying/mytv0/SP.kt")
                f.writeText(f.readText().replace(url, "DEFAULT_CONFIG_URL = \"\""))
            }
        }
    }

    if (listOf(
            "packageReleaseResources",
            "mergeReleaseResources",
            "generateReleaseResources",
            "mapReleaseSourceSetPaths",
        ).contains(name)
    ) {
        dependsOn("modifySource")
    }
}

dependencies {
    // For AGP 7.4+
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.media3.ui)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.hls)
    implementation(libs.media3.exoplayer.dash)
    implementation(libs.media3.exoplayer.rtsp)
    //implementation(libs.media3.datasource.okhttp)

    implementation(libs.nanohttpd)
    implementation(libs.gua64)
    implementation(libs.zxing)
    implementation(libs.glide)

    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.converter.gson)
    implementation(libs.retrofit)

    implementation(libs.core.ktx)
    implementation(libs.coroutines)

    implementation(libs.constraintlayout)
    implementation(libs.appcompat)
    implementation(libs.recyclerview)
    implementation(libs.lifecycle.viewmodel)

    implementation(files("libs/lib-decoder-ffmpeg-release.aar"))

    implementation(libs.conscrypt)
}