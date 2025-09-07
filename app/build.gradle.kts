plugins {
    alias(libs.plugins.android.application)
    id("com.google.dagger.hilt.android") version "2.57.1"
}

android {
    namespace = "com.uade.tpo_mobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.uade.tpo_mobile"
        minSdk = 26
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    lint {
        abortOnError = false
    }
    hilt {
        namespace = "com.example.tpo_mobile"
    }
}

dependencies {
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.core)
    val fragment_version = "1.8.9"
    implementation(libs.material)


    // Dagger Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    annotationProcessor("com.google.dagger:hilt-android-compiler:2.48")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")
    implementation("androidx.fragment:fragment:${fragment_version}")

    // Desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    testImplementation(libs.junit)

    //navigation component

    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")

    //biometria
    implementation("androidx.biometric:biometric:1.1.0")
//material design
    implementation("com.google.android.material:material:1.5.0")


}