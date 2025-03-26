import java.util.Properties

plugins {
    kotlin("kapt")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

// Load local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

val mapsApiKey = localProperties.getProperty("MAPS_API_KEY") ?: ""
val placesApiKey = localProperties.getProperty("PLACES_API_KEY") ?: ""
android {
    namespace = "com.example.savourit"
    compileSdk = 35


    defaultConfig {
        applicationId = "com.example.savourit"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
        buildConfigField("String", "PLACES_API_KEY", "\"$placesApiKey\"")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        compose = true
        dataBinding = true
        viewBinding  = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Firebase BoM (manages versions automatically)
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation(libs.google.firebase.auth)
    implementation(libs.google.firebase.database)

    // Google authentication
    implementation("com.google.android.gms:play-services-auth:21.3.0")

    // Material Design (latest stable version)
    implementation(libs.material)
    implementation(libs.play.services.measurement.api)
    implementation(libs.androidx.navigation.runtime)
    implementation(libs.androidx.tools.core)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Views/Fragments integration
    val navVersion = "2.8.8"
    implementation("androidx.navigation:navigation-fragment:$navVersion")
    implementation("androidx.navigation:navigation-ui:$navVersion")
    implementation (libs.places)

        implementation (platform("com.google.firebase:firebase-bom:32.7.0"))
        implementation ("com.google.firebase:firebase-auth")
        implementation ("com.google.firebase:firebase-database")
        implementation ("com.google.firebase:firebase-firestore:24.5.0")


}
