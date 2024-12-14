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

    val codecDir = "${projectDir}/libs/codec"
    println("Setting java.library.path to: $codecDir")

    applicationDefaultJvmArgs += listOf(
        "-Djava.library.path=$codecDir"
    )
}

dependencies {
    implementation(project(":image-encoder-core"))
    implementation("info.picocli:picocli:4.7.4")
    annotationProcessor("info.picocli:picocli-codegen:4.7.4")

    implementation("org.bytedeco:javacv:1.5.9")
    implementation("org.bytedeco:ffmpeg:6.0-1.5.9")
    implementation("org.bytedeco:javacpp:1.5.9")
}

tasks {
    shadowJar {
        archiveBaseName.set("image-encoder-cli")
        archiveVersion.set("1.0.0")
        archiveClassifier.set("")

        manifest {
            attributes(
                "Main-Class" to "com.example.image_encoder_cli.ImageEncoderCLI"
            )
        }

        mergeServiceFiles()
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }
}