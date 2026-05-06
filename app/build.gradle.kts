import com.github.megatronking.stringfog.plugin.StringFogExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("stringfog")
    id("com.google.devtools.ksp") version "2.3.5"
    //id("com.google.gms.google-services")
    //id("com.google.firebase.crashlytics")
}

apply(plugin = "stringfog")

configure<StringFogExtension> {
    implementation = "com.github.megatronking.stringfog.xor.StringFogImpl"
    enable = true
    fogPackages = arrayOf("com.word.file.manager.pdf")
    kg = com.github.megatronking.stringfog.plugin.kg.RandomKeyGenerator()
    mode = com.github.megatronking.stringfog.plugin.StringFogMode.base64
}


android {
    namespace = "com.word.file.manager.pdf"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.word.file.manager.pdf"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += setOf("arm64-v8a", "armeabi-v7a")
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    bundle {
        language {
            enableSplit = false
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.browser)
    implementation(libs.xor)
    implementation(libs.gson)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.artifex.mupdf:viewer:1.27.0a")
    implementation("com.github.marain87:AndroidPdfViewer:3.2.8")
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")

    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // https://developers.google.com/ml-kit/vision/doc-scanner/android?hl=zh-cn
    implementation("com.google.android.gms:play-services-mlkit-document-scanner:16.0.0")

    implementation("com.google.android.gms:play-services-ads:25.2.0")
    implementation("com.google.ads.mediation:applovin:13.6.2.0")
    implementation("com.google.ads.mediation:pangle:8.0.0.4.0")
    implementation("com.google.ads.mediation:facebook:6.21.0.2")
    implementation("com.google.ads.mediation:mintegral:17.1.41.0")
    implementation("com.google.ads.mediation:vungle:7.7.2.0")
    implementation("com.unity3d.ads:unity-ads:4.16.5")
    implementation("com.google.ads.mediation:unity:4.17.0.0")

    implementation(platform("com.google.firebase:firebase-bom:34.12.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-crashlytics")

    implementation(libs.installreferrer)
    implementation(libs.user.messaging.platform)
    // review
    implementation(libs.review)
    implementation(libs.review.ktx)
    // https://github.com/facebook/facebook-android-sdk/blob/main/CHANGELOG.md
    implementation(libs.facebook.android.sdk)

}
