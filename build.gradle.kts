buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        //noinspection UseTomlInstead
        classpath("com.github.megatronking.stringfog:gradle-plugin:5.2.0")
        classpath("com.github.megatronking.stringfog:xor:5.0.0")
    }
}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    //id("com.google.gms.google-services") version "4.4.4" apply false
    //id("com.google.firebase.crashlytics") version "3.0.7" apply false
}