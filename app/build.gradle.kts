plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.college.attendx"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.college.attendx"
        minSdk = 26  // Changed from 24 to 26 for better compatibility
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
            isMinifyEnabled = true  // ✅ Enable code shrinking
            isShrinkResources = true // ✅ Remove unused resources
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // ✅ Add this to keep rules
            packagingOptions {
                resources {
                    excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    excludes += "/META-INF/*.kotlin_module"
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true  // For Java 8+ features
    }



    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/*"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.splashscreen)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)

    // Google Play Services
    implementation(libs.play.services.auth)

    // QR Code Scanning & Generation
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.1")

    // Location
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // PDF Generation
    implementation("com.itextpdf:itext7-core:7.2.2")

    // Excel/CSV Export
    implementation("org.apache.poi:poi-ooxml:5.2.3") {
        exclude(group = "org.apache.logging.log4j")
        exclude(group = "org.apache.commons", module = "commons-compress")
    }
    //icons
    implementation("androidx.compose.material:material-icons-extended:1.6.7")

    // Coroutines for async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    //itextPDF
    implementation("com.itextpdf:itext7-core:7.2.2")
    implementation("com.itextpdf:itext7-core:7.2.5")

    // For storage permission (Android 10+)
    implementation("androidx.activity:activity-ktx:1.9.0")

    // Material Icons Extended (for more icon options)
    implementation("androidx.compose.material:material-icons-extended:1.7.5")

    implementation("com.google.firebase:firebase-appcheck-debug:16.0.0")
    implementation("com.google.firebase:firebase-appcheck-playintegrity:16.0.0")

    // ✅ Security Crypto for EncryptedSharedPreferences
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // ✅ For better compatibility (if needed)
    implementation("androidx.security:security-crypto-ktx:1.1.0-alpha06")

    // ADD THESE to app/build.gradle.kts dependencies block.
// These replace zxing-android-embedded for the SCANNING side (QR
// generation for the admin's session QR still uses zxing's QRCodeWriter
// / QRCodeGenerator.kt - keep that, only the student-side scanner UI is
// being replaced).

// CameraX - portrait-native camera preview + frame analysis
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

// ML Kit Barcode Scanning - replaces ZXing's decode loop for the live
// scanner. (zxing's "core" QR-CODE-WRITER for QR *generation* in
// QRCodeGenerator.kt is unaffected and still needed - keep that dependency.)
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

// You can REMOVE this line if it's only used for scanning (not generation):
// implementation("com.journeyapps:zxing-android-embedded:4.3.0")
// Keep this one - still used by QRCodeGenerator.kt for the admin's QR:
// implementation("com.google.zxing:core:3.5.3")

    // Desugaring for Java 8+ features
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}