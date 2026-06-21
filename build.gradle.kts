plugins {
    kotlin("jvm") version "2.3.21"
    `java-library`
    id("com.vanniktech.maven.publish") version "0.37.0"
}

group = "de.fridolin1"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}