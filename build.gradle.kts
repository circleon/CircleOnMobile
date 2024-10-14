// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.jlleitschuh.ktlint) apply false
    alias(libs.plugins.google.dagger.hilt.android) apply false
    alias(libs.plugins.jetbrains.kotlin.kapt) apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        setVersion("0.48.0")
        debug.set(true)
    }
}
