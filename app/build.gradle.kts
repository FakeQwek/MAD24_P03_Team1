plugins {
    alias(libs.plugins.androidApplication)

    id("com.google.gms.google-services")
}

android {
    namespace = "sg.edu.np.mad.inkwell"
    compileSdk = 34

    defaultConfig {
        applicationId = "sg.edu.np.mad.inkwell"
        minSdk = 33
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))

    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-core:21.0.0")

    implementation("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-firestore")
    implementation ("com.google.firebase:firebase-auth:20.0.4")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}