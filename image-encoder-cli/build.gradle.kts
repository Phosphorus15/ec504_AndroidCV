plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

application {
    mainClass.set("com.example.image_encoder_cli.ImageEncoderCLI")
}

dependencies {
    implementation(project(":image-encoder-core"))
    implementation("info.picocli:picocli:4.7.4")
    annotationProcessor("info.picocli:picocli-codegen:4.7.4")

    // Add JavaCV dependencies for video encoding
    implementation("org.bytedeco:javacv-platform:1.5.9")
    implementation("org.bytedeco:ffmpeg-platform:6.0-1.5.9")

    // Add logging dependencies
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("org.slf4j:slf4j-simple:1.7.32")
}

tasks {
    shadowJar {
        archiveBaseName.set("image-encoder-cli")
        archiveVersion.set("1.0.0")
        archiveClassifier.set("")
        manifest {
            attributes(
                "Main-Class" to "com.example.image_encoder_cli.ImageEncoderCLI",
                "Implementation-Title" to "Image Encoder CLI",
                "Implementation-Version" to "1.0.0"
            )
        }
        mergeServiceFiles()

        // Configure native library handling
        mergeServiceFiles("META-INF/native")
        mergeServiceFiles("META-INF/native-image")

        // Exclude unnecessary files
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }

    build {
        dependsOn(shadowJar)
    }

    // Add native image configuration
    create<JavaExec>("generateNativeConfig") {
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("picocli.codegen.aot.graalvm.ReflectionConfigGenerator")
        args = listOf(
            "com.example.image_encoder_cli.ImageEncoderCLI",
            "--output", "src/main/resources/META-INF/native-image/reflect-config.json"
        )
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Aproject=${project.group}/${project.name}")
}