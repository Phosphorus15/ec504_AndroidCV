plugins {
    `java-library`
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    // Dependencies for encoding logic here
    // For the prototype this is empty
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
