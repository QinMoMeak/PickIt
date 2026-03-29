plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
}

fun readConfig(name: String, defaultValue: String): String =
    (project.findProperty(name) as String?)
        ?: System.getenv(name)
        ?: defaultValue

fun escapeBuildConfigString(value: String): String = value.replace("\\", "\\\\").replace("\"", "\\\"")

android {
    namespace = "com.pickit.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.pickit.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 5
        versionName = "0.1.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        val aiProvider = readConfig("AI_PROVIDER", "zhipu")
        val aiBaseUrl = readConfig("AI_BASE_URL", "https://open.bigmodel.cn/api/paas/v4")
        val aiApiKey = readConfig("AI_API_KEY", "")
        val aiModel = readConfig("AI_MODEL", "glm-4.6v-flash")
        val aiTimeoutSeconds = readConfig("AI_TIMEOUT_SECONDS", "60")
        val aiEnableThinking = readConfig("AI_ENABLE_THINKING", "false")
        val aiMaxTokens = readConfig("AI_MAX_TOKENS", "1024")
        val aiTemperature = readConfig("AI_TEMPERATURE", "0.1")

        buildConfigField("String", "AI_PROVIDER", "\"${escapeBuildConfigString(aiProvider)}\"")
        buildConfigField("String", "AI_BASE_URL", "\"${escapeBuildConfigString(aiBaseUrl)}\"")
        buildConfigField("String", "AI_API_KEY", "\"${escapeBuildConfigString(aiApiKey)}\"")
        buildConfigField("String", "AI_MODEL", "\"${escapeBuildConfigString(aiModel)}\"")
        buildConfigField("int", "AI_TIMEOUT_SECONDS", aiTimeoutSeconds)
        buildConfigField("boolean", "AI_ENABLE_THINKING", aiEnableThinking)
        buildConfigField("int", "AI_MAX_TOKENS", aiMaxTokens)
        buildConfigField("double", "AI_TEMPERATURE", aiTemperature)
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation("androidx.navigation:navigation-compose:2.8.5")

    implementation("androidx.compose.ui:ui:1.7.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.0")
    implementation("androidx.compose.foundation:foundation:1.7.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.0")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("com.google.android.material:material:1.12.0")

    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    implementation("androidx.work:work-runtime-ktx:2.9.1")

    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("io.coil-kt:coil-compose:2.7.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}
