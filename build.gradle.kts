import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.4.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.vaadin") version "0.17.0.1"
    kotlin("jvm") version "1.4.21"
    kotlin("plugin.spring") version "1.4.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"

java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    implementation("com.vaadin:vaadin-spring-boot-starter:18.0.5")
    implementation("eu.vaadinonkotlin:vok-util-vaadin10:0.9.0")
    implementation("com.github.mvysny.karibudsl:karibu-dsl:1.0.4")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
}

tasks {
    named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
        jvmArgs("-Xmx512M")
    }
    withType<KotlinCompile> {
        kotlinOptions {
//            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }
}
