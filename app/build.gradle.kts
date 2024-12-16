plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.netflixplus"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.netflixplus"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            abiFilters.add("arm64-v8a")
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "google/protobuf/type.proto"
            excludes += "google/protobuf/timestamp.proto"
            excludes += "google/protobuf/duration.proto"
            excludes += "google/protobuf/empty.proto"
            excludes += "google/protobuf/struct.proto"
            excludes += "google/protobuf/wrappers.proto"
            excludes += "google/protobuf/any.proto"
            excludes += "google/protobuf/api.proto"
            excludes += "google/protobuf/field_mask.proto"
            excludes += "google/protobuf/source_context.proto"
            excludes += "google/protobuf/descriptor.proto"
        }
    }
}

dependencies {
    constraints {
        implementation("com.google.protobuf:protobuf-javalite:3.22.3") {
            because("Firebase uses protobuf-javalite")
        }
    }

    implementation("com.google.cloud:google-cloud-storage:2.22.0") {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }

    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")
    implementation("org.libtorrent4j:libtorrent4j-android-x86_64:2.0.6-26")
    implementation("org.libtorrent4j:libtorrent4j-android-arm64:2.0.6-26")
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.datasource.okhttp)

    // Media Player
    implementation(libs.rxjava)
    implementation(libs.rxandroid)

    // CardView for search bar
    implementation(libs.androidx.cardview)

    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.core:core:1.12.0")
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)

    // Lifecycle Components
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.cronet.embedded)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.gson)
    implementation(libs.okhttp3)
    implementation(libs.apache.commons.text)

    // UI Components
    implementation(libs.material)

    // Image Loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
}