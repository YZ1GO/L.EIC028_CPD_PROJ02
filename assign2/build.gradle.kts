plugins {
    id("java")
    id("application")
}

group = "com.noiatalk"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.register<JavaExec>("server") {
    mainClass.set("com.noiatalk.Server")
    classpath = sourceSets.main.get().runtimeClasspath
    standardInput = System.`in`
}

tasks.register<JavaExec>("client") {
    mainClass.set("com.noiatalk.Client")
    classpath = sourceSets.main.get().runtimeClasspath
    standardInput = System.`in`
}