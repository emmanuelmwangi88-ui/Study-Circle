import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.android)
}

// Read secrets
val secretsFile = project.rootProject.file("secrets.properties")
val secrets = Properties()
if (secretsFile.exists()) {
    secrets.load(FileInputStream(secretsFile))
}


android {
    namespace = "com.deepseek.studycircle"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.deepseek.studycircle"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Pass secrets to the build
        buildConfigField("String", "AGORA_APP_ID", "\"${secrets.getProperty("AGORA_APP_ID")}\"")
        buildConfigField("String", "AGORA_TOKEN", "\"${secrets.getProperty("AGORA_TOKEN")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
        }
        jniLibs {
            pickFirsts += "**/libaosl.so"
            pickFirsts += "**/libagora-rtc-sdk.so"
        }
    }
}

dependencies {
    // Exclude Agora JARs from libs directory as they conflict with Maven dependencies
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"), "exclude" to listOf("agora*.jar", "agorachat*.jar", "Agora*.aar"))))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.ai)

    // navigation
    implementation(libs.androidx.navigation.compose)
    // coil
    implementation(libs.coil.compose)
    // cloudinary
    implementation(libs.cloudinary.android)
    // retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.androidx.multidex)
    
    // Agora SDKs
    implementation("io.agora.rtc:full-sdk:4.2.6")
    implementation("io.agora.rtc:chat-sdk:1.2.0")
}
