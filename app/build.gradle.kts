import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    kotlin("android")

    // Firebase
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

val APP_VERSION_NAME: String by project
val APP_VERSION_CODE: String by project
val APP_ID: String by project

android {

    namespace = APP_ID
    compileSdk = libs.versions.compile.sdk.version.get().toInt()

    defaultConfig {

        applicationId = APP_ID
        minSdk = libs.versions.min.sdk.version.get().toInt()

        versionCode = APP_VERSION_CODE.toInt()
        versionName = APP_VERSION_NAME

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    /**
     * Release signing
     */
    signingConfigs {

        create("release") {

            /**
             * Caminho do keystore
             * Será criado pela pipeline GitHub Actions
             */
            storeFile = file("../keystore/release.jks")

            /**
             * Secrets vindos do GitHub Actions
             */
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")

            /**
             * Compatibilidade Android antigo
             */
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {

        getByName("release") {

            /**
             * APK assinado
             */
            signingConfig = signingConfigs.getByName("release")

            /**
             * Necessário para mapping.txt
             */
            isMinifyEnabled = true

            /**
             * Recomendado junto com minify
             */
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    lint {
        warningsAsErrors = true
        abortOnError = true
        disable.add("GradleDependency")
    }

    // Use this block to configure different flavors
//    flavorDimensions("version")
//    productFlavors {
//        create("full") {
//            dimension = "version"
//            applicationIdSuffix = ".full"
//        }
//        create("demo") {
//            dimension = "version"
//            applicationIdSuffix = ".demo"
//        }
//    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {

    implementation(projects.libraryAndroid)
    implementation(projects.libraryCompose)
    implementation(projects.libraryKotlin)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraint.layout)
    implementation(libs.androidx.core.ktx)

    // Firebase BoM
    implementation(
        platform(
            "com.google.firebase:firebase-bom:34.13.0"
        )
    )

    // Firebase Analytics
    implementation(
        "com.google.firebase:firebase-analytics"
    )

    // Firebase Crashlytics
    implementation(
        "com.google.firebase:firebase-crashlytics"
    )

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.ext.junit.ktx)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.espresso.core)
}