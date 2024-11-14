# Image Encoder Project

This image encoder convert collections of images into compressed video format with Android and CLI interfaces.

## Overview

The Image Encoder Project provides for image-to-video conversion through three main components:

- **Android Application**: User-friendly interface for image selection, encoding configuration, and video playback
- **Image Encoder Core**: Modular core logic handling the image-to-video conversion
- **CLI Tool**: Command-line interface powered by Picocli for terminal-based encoding

## Features

- Android app with image selection capabilities and encoding controls
- Modular architecture separating core logic from interfaces
- Command-line tool for automated batch processing
- Picocli integration for robust CLI argument handling
- Fat JAR generation via Shadow plugin
- In-app video playback capabilities

## Project Structure

```
ec504_AndroidCV/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       └── main/
│           ├── java/com/example/androidvideoencoder/
│           │   └── MainActivity.kt
│           ├── res/
│           │   ├── layout/
│           │   │   ├── activity_main.xml
│           │   │   └── item_image.xml
│           │   └── values/
│           │       └── strings.xml
│           └── AndroidManifest.xml
├── image-encoder-cli/
│   ├── build.gradle.kts
│   └── src/main/java/com/example/image_encoder_cli/
│       └── ImageEncoderCLI.java
├── image-encoder-core/
│   ├── build.gradle.kts
│   └── src/main/java/com/example/image_encoder_core/
│       └── ImageEncoder.java
└── gradle/
```

## Technologies

**Core Stack**
- Kotlin and Java
- Gradle with Kotlin DSL
- Android SDK

**Plugins**
- Android Gradle Plugin
- Kotlin Android Plugin
- Shadow Plugin
- Picocli

## Prerequisites

- JDK 8+
- Latest Android Studio
- Gradle 8.9
- Git

## Installation

```bash
git clone https://github.com/yourusername/ec504_AndroidCV.git
cd ec504_AndroidCV
```

## Building the Project

**Android Application**
1. Open project in Android Studio
2. Sync Gradle files
3. Build project (Ctrl + F9)
4. Run on device/emulator

**CLI Tool**
```bash
# Linux/Mac
./gradlew :image-encoder-cli:shadowJar --rerun-tasks --info

# Windows
gradlew.bat :image-encoder-cli:shadowJar --rerun-tasks --info

# Run CLI
java -jar image-encoder-cli/build/libs/image-encoder-cli-1.0.0.jar --help
```

