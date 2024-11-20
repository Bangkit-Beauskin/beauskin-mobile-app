plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.dicoding.bangkitcapstone"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dicoding.bangkitcapstone"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // UI dan layout
    implementation(libs.androidx.core.ktx)              // Kotlin extensions untuk API Android standar
    implementation(libs.androidx.appcompat)             // Kompatibilitas dengan fitur UI terbaru
    implementation(libs.material)                       // Material Design components
    implementation(libs.androidx.activity)              // API terkait Activity
    implementation(libs.androidx.constraintlayout)     // Layout fleksibel dan responsif

    // LiveData, ViewModel dan Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)  // ViewModel dengan ekstensi Kotlin
    implementation(libs.androidx.lifecycle.livedata.ktx)   // LiveData dengan ekstensi Kotlin
    implementation(libs.androidx.fragment.ktx)             // Fragment dengan ekstensi Kotlin

    implementation(libs.androidx.activity.ktx)

    // Testing
    testImplementation(libs.junit)                        // Unit testing dengan JUnit
    androidTestImplementation(libs.androidx.junit)       // UI testing dengan JUnit
    androidTestImplementation(libs.androidx.espresso.core) // UI testing dengan Espresso

    // Lain-lain
    implementation(libs.lottie)                          // Animasi berbasis Lottie
    implementation(libs.android.gif.drawable)            // Menampilkan GIF di UI
    implementation(libs.hilt.android)                     // Dependency Injection menggunakan Hilt
    kapt(libs.hilt.android.compiler)                     // Kompilasi Hilt menggunakan KAPT

    implementation(libs.glide)
}

// Konfigurasi untuk KAPT
kapt {
    correctErrorTypes = true  // Memperbaiki jenis kesalahan tipe selama pemrosesan anotasi
}
