import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

fun loadSigningProps(projectRoot: File): Properties? {
    val propPath = (project.findProperty("signingPropertiesFile") as String?)?.trim()
    if (propPath.isNullOrBlank()) return null

    val f = File(projectRoot, propPath)
    if (!f.exists()) return null

    return Properties().apply {
        FileInputStream(f).use { fis -> load(fis) }
    }
}

fun ciVersionCodeOrNull(): Int? {
    val raw = (project.findProperty("ciVersionCode") as String?)?.trim()
    if (raw.isNullOrBlank()) return null
    return raw.toIntOrNull()
}

fun ciVersionNameOrNull(): String? {
    val raw = (project.findProperty("ciVersionName") as String?)?.trim()
    if (raw.isNullOrBlank()) return null
    return raw
}

android {
    namespace = "com.webkurier.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.webkurier.android"
        minSdk = 29
        targetSdk = 34

        val baseVersionCode = 1
        val baseVersionName = "0.1.0"

        versionCode = ciVersionCodeOrNull() ?: baseVersionCode
        versionName = ciVersionNameOrNull() ?: baseVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val signingProps = loadSigningProps(rootProject.projectDir)

    signingConfigs {
        if (signingProps != null) {
            create("release") {
                val storeFileName = signingProps.getProperty("storeFile") ?: "release-keystore.jks"
                storeFile = file(storeFileName)
                storePassword = signingProps.getProperty("storePassword")
                keyAlias = signingProps.getProperty("keyAlias")
                keyPassword = signingProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            if (signingProps != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}"
            )
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui:1.7.2")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.2")
    implementation("androidx.compose.material3:material3:1.3.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.2")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

