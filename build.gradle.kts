plugins {
    kotlin("jvm") version "1.3.61"
    `java-gradle-plugin`
    id("com.vanniktech.maven.publish") version "0.9.0"
}

group = "dev.tilbrook.keki.gradle"
version = "0.1.0-SNAPSHOT"

repositories {
    maven ( url = "https://dl.google.com/dl/android/maven2/")
    google()
    jcenter()
    mavenCentral()
}

gradlePlugin {
    plugins.create("clfag") {
        id = "dev.tilbrook.keki.gradle.cflag"
        implementationClass = "dev.tilbrook.keki.gradle.cflag.CFlagPlugin"
    }
}

dependencies {
    implementation("com.squareup:kotlinpoet:1.5.0")

    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly(gradleApi())
    compileOnly(kotlin("gradle-plugin", "1.3.61"))
    compileOnly("com.android.tools.build:gradle:3.6.0-rc03")

    testImplementation(gradleTestKit())
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

tasks.withType(Wrapper::class.java) {
    gradleVersion = "5.2.1"
}