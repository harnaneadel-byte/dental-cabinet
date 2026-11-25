plugins {
    id("com.android.library")
}

android {
    namespace = "dz.cabinet.kmpcore"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("net.zetetic:sqlcipher-android:4.19.1")
    implementation("io.insert-koin:koin-android:3.5.1")
    testImplementation(kotlin("test"))
}