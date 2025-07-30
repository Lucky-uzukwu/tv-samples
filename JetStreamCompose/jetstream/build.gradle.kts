/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.baselineprofile)
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "com.google.wiltv"
    // Needed for latest androidx snapshot build
    compileSdk = 35

    defaultConfig {
        applicationId = "com.google.wiltv"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "SERVER_URL", "\"https://api.nortv.xyz\"")
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui.tooling.preview)

    // extra material icons
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.tv.foundation)

    // Material components optimized for TV apps
    implementation(libs.androidx.tv.material)

    // ViewModel in Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Compose Navigation
    implementation(libs.androidx.navigation.compose)

    // Coil
    implementation(libs.coil.compose)

    // JSON parser
    implementation(libs.kotlinx.serialization)

    // Media3
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.ui)

    // SplashScreen
    implementation(libs.androidx.core.splashscreen)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.material3.android)
    ksp(libs.hilt.compiler)

    // Baseline profile installer
    implementation(libs.androidx.profileinstaller)

    // Compose Previews
    debugImplementation(libs.androidx.compose.ui.tooling)

    // For baseline profile generation
    baselineProfile(project(":benchmark"))

    implementation(libs.retrofit) // Retrofit core
    implementation(libs.converter.gson) // JSON converter (or use Moshi if preferred)
    implementation("co.touchlab:kermit:2.0.4")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.datastore:datastore-preferences:1.1.6")

    val paging_version = "3.3.6"

    //noinspection UseTomlInstead
    implementation("androidx.paging:paging-runtime:$paging_version")
    // optional - Jetpack Compose integration
    implementation("androidx.paging:paging-compose:$paging_version")

    implementation("androidx.compose.ui:ui-text-google-fonts:1.8.1")

    implementation(libs.tv.foundation)
    implementation(libs.coil.compose)

    // SplashScreen
    implementation(libs.androidx.core.splashscreen)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // ViewModel in Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    //lifecycle
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Compose Navigation
    implementation(libs.androidx.navigation.compose)

    // Coil
    implementation(libs.coil.compose)

    // JSON parser
    implementation(libs.kotlinx.serialization)

    // Media3
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)

    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    implementation(libs.room.runtime)
    ksp(libs.androidx.room.compiler)

    // Baseline profile installer
    implementation(libs.androidx.profileinstaller)
    
    // QR Code generation
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
}
