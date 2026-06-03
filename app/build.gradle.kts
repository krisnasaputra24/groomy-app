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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        mlModelBinding = true
    }

    androidResources {
        noCompress += "tflite"
    }
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
        resources {
            pickFirsts += "lib/x86/libtensorflowlite_jni.so"
            pickFirsts += "lib/x86_64/libtensorflowlite_jni.so"
            pickFirsts += "lib/armeabi-v7a/libtensorflowlite_jni.so"
            pickFirsts += "lib/arm64-v8a/libtensorflowlite_jni.so"
            pickFirsts += "lib/x86/libtensorflowlite_flex_jni.so"
            pickFirsts += "lib/x86_64/libtensorflowlite_flex_jni.so"
            pickFirsts += "lib/armeabi-v7a/libtensorflowlite_flex_jni.so"
            pickFirsts += "lib/arm64-v8a/libtensorflowlite_flex_jni.so"
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
    implementation("androidx.compose.material:material-icons-extended")
    // Retrofit untuk koneksi API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
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
    implementation(libs.tensorflow.lite.metadata)
}
