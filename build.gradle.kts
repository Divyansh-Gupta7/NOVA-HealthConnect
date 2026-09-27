// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.google.services) apply false
}

allprojects {
    val buildBase = System.getenv("NOVA_BUILD_DIR") 
        ?: "${System.getProperty("user.home")}/.gradle/builds/NovaHealthConnect/${project.name}"
    layout.buildDirectory.set(file(buildBase))
}
