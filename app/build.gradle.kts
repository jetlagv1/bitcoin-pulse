plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
val uploadStore = providers.environmentVariable("PULSE_UPLOAD_STORE_FILE").orNull
android {
    namespace = "com.buildapptest.bitcoinpulse"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.buildapptest.bitcoinpulse"
        minSdk = 26
        targetSdk = 36
        versionCode = 23
        versionName = "1.8.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    signingConfigs {
        if (uploadStore != null) create("playUpload") {
            storeFile = file(uploadStore)
            storePassword = providers.environmentVariable("PULSE_UPLOAD_STORE_PASSWORD").get()
            keyAlias = providers.environmentVariable("PULSE_UPLOAD_KEY_ALIAS").get()
            keyPassword = providers.environmentVariable("PULSE_UPLOAD_KEY_PASSWORD").get()
        }
    }
    buildTypes { release {
        isDebuggable = false
        isMinifyEnabled = false
        if (uploadStore != null) signingConfig = signingConfigs.getByName("playUpload")
    } }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all { it.systemProperty("robolectric.dependency.repo.url", "https://repo.maven.apache.org/maven2") }
    }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

tasks.matching { it.name == "bundleRelease" || it.name == "assembleRelease" }.configureEach {
    doFirst { check(uploadStore != null) { "Release signing is required. Use tools/build-play-release.ps1 or configure PULSE_UPLOAD_* environment variables." } }
}
dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.04.01"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")
    implementation("androidx.work:work-runtime-ktx:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
    testImplementation("org.robolectric:robolectric:4.14.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
}
