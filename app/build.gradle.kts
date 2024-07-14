plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.zs.gallery"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.zs.gallery"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0-dev01"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    buildTypes {
        // Make sure release is version is optimised.
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }

        // Add necessary changes to debug apk.
        debug {
            // makes it possible to install both release and debug versions in same device.
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "Debug")
            versionNameSuffix = "-debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xcontext-receivers",
            "-Xopt-in=com.primex.core.ExperimentalToolkitApi"
        )
    }
    composeCompiler { enableStrongSkippingMode = false }
    buildFeatures { compose = true }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.androidx.koin)
    implementation(project(":api"))
    implementation(project(":compose-ktx"))
    implementation(libs.toolkit.preferences)
    implementation (libs.androidx.startup.runtime)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.accompanist.permissions)
    implementation(libs.bundles.compose.icons)
    implementation(libs.coil.compose)
}