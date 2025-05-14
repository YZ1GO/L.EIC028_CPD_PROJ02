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

val trustStorePath = "config/serverkeystore.jks"
val trustStorePassword = "password"

tasks.register<JavaExec>("server") {
    mainClass.set("com.noiatalk.Server")
    classpath = sourceSets.main.get().runtimeClasspath
    standardInput = System.`in`
    jvmArgs = listOf(
        "-Djavax.net.ssl.keyStore=$trustStorePath",
        "-Djavax.net.ssl.keyStorePassword=$trustStorePassword"
    )
}

tasks.register<JavaExec>("client") {
    mainClass.set("com.noiatalk.Client")
    classpath = sourceSets.main.get().runtimeClasspath
    standardInput = System.`in`
    jvmArgs = listOf(
        "-Djavax.net.ssl.trustStore=$trustStorePath",
        "-Djavax.net.ssl.trustStorePassword=$trustStorePassword"
    )
}