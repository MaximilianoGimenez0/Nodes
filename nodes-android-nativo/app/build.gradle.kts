import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.google.gms.google-services")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use {
        localProperties.load(it)
    }
}

val appVersionCode = 5
val appVersionName = "1.4"

android {
    namespace = "com.maxigimenez.nodes"
    compileSdk = 36

    bundle {
        language {
            enableSplit = false
        }
    }

    androidResources {
        generateLocaleConfig = true
        localeFilters += setOf("es", "en", "pt")
    }

    defaultConfig {
        applicationId = "com.maxigimenez.nodes"
        minSdk = 24
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName

        // Generar recurso de string automático (R.string.app_version)
        resValue("string", "app_version", appVersionName)
        resValue("string", "app_version_code", appVersionCode.toString())

        // Manifest placeholders
        manifestPlaceholders["appVersion"] = appVersionName
        manifestPlaceholders["appVersionCode"] = appVersionCode

        buildConfigField("String", "TERMS_URL", "\"${localProperties.getProperty("TERMS_URL", "https://nodes-web.maxigimenez.com.ar/eula")}\"")
        buildConfigField("String", "PASSWORD_RESET_URL", "\"${localProperties.getProperty("PASSWORD_RESET_URL", "https://nodes-web.maxigimenez.com.ar/passwordReset")}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // ACTIVADO: Reduce el tamaño del código y dificulta la ingeniería inversa
            isMinifyEnabled = true
            // ACTIVADO: Elimina imágenes y recursos que no se usan en la app
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation("androidx.graphics:graphics-shapes:1.0.1")

    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)

    val nav_version = "2.9.5"

    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-functions")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.google.firebase:firebase-messaging")

    implementation("com.google.android.gms:play-services-auth:21.3.0")

    implementation("androidx.lifecycle:lifecycle-process:2.8.3")

    // Para Preferences DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    implementation("androidx.navigation:navigation-compose:${nav_version}")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation(libs.kotlinx.coroutines.play.services)

    implementation(libs.androidx.compose.material.icons.extended)
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}