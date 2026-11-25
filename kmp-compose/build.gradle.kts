plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "dz.cabinet.design"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }
}

kotlin {
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("androidx.compose:compose-runtime:1.7.2")
                implementation("androidx.compose.ui:ui:1.7.2")
                implementation("androidx.compose.material3:material3:1.3.1")
                implementation("androidx.compose.ui:ui-tooling-preview:1.7.2")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("androidx.activity:activity-compose:1.9.0")
                implementation("androidx.appcompat:appcompat:1.7.0")
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
    }
}