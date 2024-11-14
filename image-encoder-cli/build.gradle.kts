import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

application {
    mainClass.set("com.example.image_encoder_cli.ImageEncoderCLI")
}

dependencies {
    implementation("info.picocli:picocli:4.7.4")
    annotationProcessor("info.picocli:picocli-codegen:4.7.4")
    implementation(project(":image-encoder-core"))
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    withType<ShadowJar> {
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
    }

    build {
        dependsOn(shadowJar)
    }

    jar {
        enabled = false
    }
}

tasks.named<JavaExec>("run") {
    mainClass.set("com.example.image_encoder_cli.ImageEncoderCLI")
}