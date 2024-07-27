plugins {
    alias(libs.plugins.androidApplication)

    id("com.google.gms.google-services")
}

android {
    namespace = "sg.edu.np.mad.inkwell"
    compileSdk = 34

    defaultConfig {
        applicationId = "sg.edu.np.mad.inkwell"
        minSdk = 31
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

    packagingOptions {
        resources.excludes.add("META-INF/DEPENDENCIES")
        resources.excludes.add("META-INF/LICENSE")
        resources.excludes.add("META-INF/LICENSE.txt")
        resources.excludes.add("META-INF/LICENSE.md")
        resources.excludes.add("META-INF/NOTICE")
        resources.excludes.add("META-INF/NOTICE.txt")
        resources.excludes.add("META-INF/NOTICE.md")
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
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.google.android.material:material:1.4.0")
    implementation ("androidx.biometric:biometric:1.1.0")
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-core:21.0.0")
    implementation("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-firestore")
    implementation ("com.google.firebase:firebase-auth:20.0.4")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation ("com.airbnb.android:lottie:3.4.0")
    implementation ("androidx.biometric:biometric:1.1.0")
    implementation ("androidx.biometric:biometric:1.2.0-alpha04")
    implementation ("com.sendgrid:sendgrid-java:4.7.1")
    implementation ("com.sun.mail:android-mail:1.6.7")
    implementation ("com.sun.mail:android-activation:1.6.7")
    implementation ("androidx.recyclerview:recyclerview:1.2.1")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.android.volley:volley:1.2.1")
    implementation ("com.google.mlkit:translate:16.1.2")
    implementation ("androidx.biometric:biometric:1.1.0")
    implementation ("com.google.firebase:firebase-auth:21.0.1")
    implementation ("com.google.firebase:firebase-database:20.0.4")
    implementation ("androidx.biometric:biometric:1.2.0-alpha03")
    implementation ("androidx.biometric:biometric:1.1.0")
    implementation ("androidx.biometric:biometric:1.1.0")
    implementation ("androidx.biometric:biometric:1.0.1")
    implementation ("com.google.firebase:firebase-auth:21.0.1")
    implementation ("com.google.firebase:firebase-firestore:24.0.0")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    implementation("androidx.biometric:biometric:1.0.1")
    implementation ("com.google.firebase:firebase-database:20.0.5")
    implementation ("androidx.biometric:biometric:1.1.0")
    implementation ("com.google.firebase:firebase-auth:21.0.1")
    implementation ("com.google.firebase:firebase-database:20.0.1")
    implementation ("com.google.mlkit:text-recognition:16.0.0")
    implementation ("androidx.camera:camera-core:1.1.0")
    implementation ("androidx.camera:camera-camera2:1.1.0")
    implementation ("androidx.camera:camera-lifecycle:1.1.0")
    implementation ("androidx.camera:camera-view:1.0.0-alpha32")
    implementation ("org.tensorflow:tensorflow-lite:2.6.0")
    implementation ("org.tensorflow:tensorflow-lite-support:0.2.0")
    implementation ("org.tensorflow:tensorflow-lite-task-vision:0.2.0")
    implementation ("androidx.camera:camera-camera2:1.0.0")
    implementation ("androidx.camera:camera-lifecycle:1.0.0")
    implementation ("androidx.camera:camera-core:1.0.0")
    implementation ("com.itextpdf:itext7-core:7.1.15")
}