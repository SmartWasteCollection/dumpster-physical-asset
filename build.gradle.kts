import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    application
}

group = "swc"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.azure:azure-messaging-servicebus:7.10.1")
    implementation("com.azure:azure-digitaltwins-core:1.3.1")
    implementation("com.google.code.gson:gson:2.9.1")
    implementation("com.azure:azure-identity:1.5.3")
    implementation("com.google.code.gson:gson:2.9.1")
    implementation("io.github.cdimascio:dotenv-kotlin:6.3.1")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("swc.AppKt")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}