plugins {
    id("com.android.library")
}

android {
    namespace = "dz.cabinet.core.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation("io.insert-koin:koin-android:3.5.6")
}
