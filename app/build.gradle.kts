plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.bahri.lovelypos"

    compileSdk = 35

    defaultConfig {
        applicationId = "com.bahri.lovelypos"

        minSdk = 26
        targetSdk = 35

        versionCode = 1
        versionName = "1.1.3"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility =
            JavaVersion.VERSION_17

        targetCompatibility =
            JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    applicationVariants.all {
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.ApkVariantOutputImpl
            val appName = try {
                val stringsFile = file("src/main/res/values/strings.xml")
                val xmlContent = stringsFile.readText()
                val pattern = Regex("<string name=\"app_name\">(.*?)</string>")
                pattern.find(xmlContent)?.groupValues?.get(1) ?: "LovelyPOS"
            } catch (_: Exception) {
                "LovelyPOS"
            }
            output.outputFileName = "$appName-${versionName}.apk"
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {

    // Compose BOM
    implementation(
        platform(libs.androidx.compose.bom)
    )

    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)

    // Compose
    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.compose.ui)

    implementation(libs.androidx.compose.ui.graphics)

    implementation(
        libs.androidx.compose.ui.tooling.preview
    )

    implementation(
        libs.androidx.compose.material3
    )

    // Lifecycle
    implementation(
        libs.androidx.lifecycle.runtime.ktx
    )

    implementation(
        libs.androidx.lifecycle.runtime.compose
    )

    implementation(
        libs.androidx.lifecycle.viewmodel.compose
    )

    // Navigation
    implementation(
        libs.androidx.navigation.compose
    )

    // Room
    implementation(libs.androidx.room.runtime)

    implementation(libs.androidx.room.ktx)

    ksp(libs.androidx.room.compiler)

    // Coroutines
    implementation(
        libs.kotlinx.coroutines.android
    )

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)


    // Unit Test
    testImplementation(libs.junit)

    // Android Test
    androidTestImplementation(
        platform(libs.androidx.compose.bom)
    )

    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )

    androidTestImplementation(
        libs.androidx.espresso.core
    )

    androidTestImplementation(
        libs.androidx.junit
    )

    // Debug
    debugImplementation(
        libs.androidx.compose.ui.tooling
    )

    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )
}
