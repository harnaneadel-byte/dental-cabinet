pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "dental-cabinet"
include(":core:data", ":core:security", ":core:domain", ":design-system", ":kmp-core", ":kmp-compose", ":app")