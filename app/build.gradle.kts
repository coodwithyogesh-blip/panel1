plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.momsafe.duallock"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.momsafe.duallock"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions { unitTests.isIncludeAndroidResources = false }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
