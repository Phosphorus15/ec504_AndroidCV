plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation("org.bytedeco:javacv-platform:1.5.9")
    implementation("org.bytedeco:ffmpeg:6.0-1.5.9:android-arm64")
    implementation("org.bytedeco:ffmpeg:6.0-1.5.9:android-x86_64")
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("org.slf4j:slf4j-simple:1.7.32")
}