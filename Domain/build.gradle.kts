plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)

    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.domain"
    compileSdk = 36

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    kotlinOptions {
        jvmTarget = "11"
    }
}
dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    //shiii
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.navigation:navigation-compose:2.8.3")

    //hilt shiii
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")



    // video recoreder shiii (ПРОКЛЯТО)
//
//    implementation("androidx.camera:camera-core:1.3.4")
//    implementation("androidx.camera:camera-camera2:1.3.4")
//    implementation("androidx.camera:camera-lifecycle:1.3.4")
//    implementation("androidx.camera:camera-video:1.3.4")

// просто камера (почти проклято)
//    implementation("androidx.camera:camera-camera2:1.4.0")
//    implementation("androidx.camera:camera-lifecycle:1.4.0")
//    implementation("androidx.camera:camera-video:1.4.0")
//    implementation("androidx.camera:camera-view:1.4.0")
//    implementation("androidx.camera:camera-core:1.4.0")

    // CameraX core и extensions (стабильная 1.5.0)
    implementation("androidx.camera:camera-core:1.5.0")
    implementation("androidx.camera:camera-camera2:1.5.0")
    implementation("androidx.camera:camera-lifecycle:1.5.0")
    implementation("androidx.camera:camera-video:1.5.0")  // Для VideoCapture и QualitySelector
    implementation("androidx.camera:camera-view:1.5.0")   // Для PreviewView

    // Coroutines для await() на ListenableFuture (из Guava, используемого CameraX)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.8.1")  // Или новее, если обновили Kotlin

    // Базовые coroutines (если ещё нет)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")



    // Тесты
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")


    //спич ту текст
    //implementation("com.alphacephei:vosk-android:0.3.50")  // Актуальная версия на 2025; проверьте на Maven Central
    //implementation("com.alphacephei:vosk-android:0.3.32")


    implementation("com.alphacephei:vosk-android:0.3.47") {
        exclude(group = "net.java.dev.jna", module = "jna")
    }

    implementation("net.java.dev.jna:jna:5.13.0@aar")

    implementation(project(":Data"))

}

kapt {
    correctErrorTypes = true
}