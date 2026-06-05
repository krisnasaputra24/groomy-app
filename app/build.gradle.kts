plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.krisna.groomy"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.krisna.groomy"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Membatasi arsitektur HP hanya untuk ARM (HP Modern)
        // Ini akan menghapus library x86 (Emulator) yang sangat berat dari Select TF Ops
        ndk {
            abiFilters.add("arm64-v8a")
            abiFilters.add("armeabi-v7a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        mlModelBinding = true
    }

    androidResources {
        noCompress += "tflite"
        localeFilters += "en"
        localeFilters += "id"
    }
    packaging {
        jniLibs {
            useLegacyPackaging = true
            // Jangan exclude library ini karena dibutuhkan oleh TensorFlow Lite Flex
            // excludes += "**/libtensorflowlite_jni.so"
            // excludes += "**/libtensorflowlite_flex_jni.so"
        }
        resources {
            // Hapus duplikasi dan library yang tidak perlu untuk mengecilkan ukuran APK
            excludes += "META-INF/*.kotlin_module"
            excludes += "META-INF/DEPENDENCIES"
            pickFirsts += "lib/**/libtensorflowlite_jni.so"
            pickFirsts += "lib/**/libtensorflowlite_flex_jni.so"
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.tensorflow.lite.metadata)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.material.icons.extended)
    // Retrofit untuk koneksi API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // Logging untuk melihat error di Logcat
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    val nav_version = "2.7.7"

    implementation("androidx.navigation:navigation-compose:$nav_version")
    implementation("androidx.compose.foundation:foundation:1.6.0")
    implementation(libs.coil.compose)
    implementation(libs.cloudinary.android)
    
    implementation("org.tensorflow:tensorflow-lite:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.16.1") // TAMBAHKAN INI
    implementation("org.tensorflow:tensorflow-lite-support-api:0.4.4")
}
