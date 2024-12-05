plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation("org.bytedeco:javacv:1.5.9")
    implementation("org.bytedeco:ffmpeg:6.0-1.5.9")
    implementation("org.bytedeco:javacpp:1.5.9")
}