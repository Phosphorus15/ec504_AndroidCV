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
- Android NDK Installed
- Gradle 8.5
- Git

## Installation

Clone the repository:
```bash
git clone https://github.com/yourusername/ec504_AndroidCV.git
cd ec504_AndroidCV
```

## Setting Up the Gradle Wrapper

From terminal run:
```bash
gradle wrapper --gradle-version 8.5 --distribution-type all
```

## Building the Project

### Android Application

1. **Open in Android Studio**:
   Launch Android Studio and open the `ec504_AndroidCV` project.

2. **Sync Gradle**:
   Click on **File > Sync Project with Gradle Files** to resolve dependencies.

3. **Build the Project**:
   Navigate to **Build > Build Project**. 

4. **Run the App**:
   Start an emulator, then click the Run button (This launches the app and shows two interfaces (Splash activity and Main activity)
 

### CLI Tool
From Terminal 
1. **Navigate to the Project Root**:
   ```bash
   cd ec504_AndroidCV
   ```

2. **Build the CLI Module**:
   - On Linux/Mac:
     ```bash
     ./gradlew :image-encoder-cli:shadowJar --rerun-tasks --info
     ```
   - On Windows:
     ```cmd
     gradlew.bat :image-encoder-cli:shadowJar --rerun-tasks --info
     ```

   **Note** that we only provided pre-built `libencoder_jni` for x64 Linux or ARM macOS. If you want to run the CLI on Windows you'll need to go to the native library [Repository](https://github.com/eburhansjah/ec504_ImageEncoder) and build the native `.dll` library following the guide and put it in the root folder of this project.

3. **Locate the Generated JAR**:
   After the build, the fat JAR will be located at:
   ```
   image-encoder-cli/build/libs/image-encoder-cli-1.0.0.jar
   ```
4. 
  **Note: This jar file does not ship with the native library counterpart, so you have to put the native library in appropriate places (e.g. `libencoder_jni.dylib` to `/usr/lib/java`) for the CLI to work.**
**To see help information**:
   ```
   java -jar image-encoder-cli-1.0.0.jar --help
   ```
   Intended usage of commandline below at the end of the project.
## CLI Tool Usage

1. **Ensure Java is Installed**:
   Verify Java installation with:
   ```bash
   java -version
   ```

2. **Run the JAR File**:
   Navigate to the directory containing the JAR file:
   ```bash
   cd ec504_AndroidCV/image-encoder-cli/build/libs/
   ```

3. **Execute the JAR with Required Arguments**: 
   ```bash
   java -jar image-encoder-cli-1.0.0.jar --input /path/to/input/images --output /path/to/output/video.mpeg1 --format mpeg1 --quality 80
   ```

   ### Parameters:
   - `--input` or `-i`: Path to the directory containing input JPEG images.
   - `--output` or `-o`: Desired output file path for the encoded video.
   - `--format` or `-f`: Video format (e.g., mpeg1, mpeg2, h264, hevc).
   - `--quality` or `-q`: Encoding quality (1-100).

   ### Example:
   ```bash
   java -jar image-encoder-cli-1.0.0.jar --input ./images --output ./videos/output_video.mpeg1 --format mpeg1 --quality 80
   ```

--- 
