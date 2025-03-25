import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.dagger.hilt.android)
    alias(libs.plugins.jetbrains.kotlin.kapt)
}

android {
    namespace = "com.developeek.circleon"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.developeek.circleon"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "TOKEN_PREFERENCE_KEY", getLocalValue("token.preference.key"))
        buildConfigField("String", "USER_PREFERENCE_KEY", getLocalValue("user.preference.key"))
        buildConfigField("String", "SERVICE_API_URL", getLocalValue("service.api.url"))
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

fun getLocalValue(key: String): String {
    return gradleLocalProperties(rootDir, providers).getProperty(key) ?: ""
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.okhttp3.interceptor)
    implementation(libs.google.dagger.hilt.android)
    implementation(libs.google.code.gson)
    implementation(libs.squareup.retrofit2)
    implementation(libs.squareup.retrofit2.converter)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.fragment)
    implementation(libs.bumptech.glide)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.google.android.flexbox)
    implementation(libs.facebook.shimmer)
    kapt(libs.google.dagger.hilt.android.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
