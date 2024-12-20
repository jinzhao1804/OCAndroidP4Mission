plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
}

android {
  namespace = "com.aura"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.aura"
    minSdk = 24
    targetSdk = 33
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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


// Testing
  testImplementation ("org.junit.jupiter:junit-jupiter-api:5.7.0")
  testImplementation ("org.junit.jupiter:junit-jupiter-engine:5.7.0")
  testImplementation ("io.mockk:mockk:1.12.0")
  testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
  testImplementation ("org.junit.jupiter:junit-jupiter-params:5.7.0")
  testImplementation ("androidx.arch.core:core-testing:2.2.0")





  // Retrofit and Gson for HTTP requests and JSON parsing
  implementation ("com.squareup.retrofit2:retrofit:2.9.0")
  implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
  implementation ("com.squareup.okhttp3:okhttp:4.12.0")

  // Coroutine support for Retrofit
  implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")



  implementation ("androidx.activity:activity-ktx:1.9.3")  // Add this line for Activity extensions


  // ViewModel and LiveData
  implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
  implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")


// Coroutine dependencies for using Flow
  implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
  implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")


  implementation("androidx.core:core-ktx:1.15.0")
  implementation("androidx.appcompat:appcompat:1.7.0")
  implementation("com.google.android.material:material:1.12.0")
  implementation("androidx.annotation:annotation:1.9.1")
  implementation("androidx.constraintlayout:constraintlayout:2.2.0")
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.2.1")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}