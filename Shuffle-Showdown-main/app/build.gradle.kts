plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")

    // Dagger Hilt
    id("com.google.dagger.hilt.android")

    id("kotlin-kapt")

    // Parcelling an object
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.shuffle_showdown"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.shuffle_showdown"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Jetpack Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.5")

    // Import Firebase + BoM
    implementation(platform("com.google.firebase:firebase-bom:32.6.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore:24.9.1")

    // Firebase authentication (for login)
    implementation("com.google.firebase:firebase-auth:22.3.0")

    // LiveData + fragment
    implementation("androidx.lifecycle:lifecycle-livedata-core-ktx:2.6.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // Dagger Hilt https://dagger.dev/hilt/gradle-setup
    // For injecting a single instance of firebase db and a viewmodel throughout app
    implementation("com.google.dagger:hilt-android:2.48.1")
    kapt("com.google.dagger:hilt-compiler:2.48.1")
    kapt("androidx.hilt:hilt-compiler:1.1.0")

}