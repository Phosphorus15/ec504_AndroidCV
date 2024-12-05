plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.androidvideoencoder"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.androidvideoencoder"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true

        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    buildFeatures {
        viewBinding = true
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

    packaging {
        resources {
            pickFirsts += "META-INF/native-image/ios-x86_64/jnijavacpp/reflect-config.json"
            excludes += listOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/*.kotlin_module",
                "META-INF/native-image/**",
                "META-INF/native/**",
                "META-INF/INDEX.LIST"
            )
        }
        jniLibs {
            useLegacyPackaging = true
            pickFirsts += setOf(
                "**/libjniavutil.so",
                "**/libjniavcodec.so",
                "**/libjniavformat.so",
                "**/libjniavfilter.so",
                "**/libjniavdevice.so",
                "**/libjniswresample.so",
                "**/libjniswscale.so",
                "libpostproc.so",
                "libjnipostproc.so"
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
}

dependencies {
    implementation(project(":image-encoder-core"))

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.8.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("org.bytedeco:javacv:1.5.9")
    implementation("org.bytedeco:ffmpeg:6.0-1.5.9:android-arm64") {
        exclude(group = "org.bytedeco", module = "javacpp")
    }
    implementation("org.bytedeco:javacpp:1.5.9:android-arm64")

    implementation("androidx.multidex:multidex:2.0.1")
}