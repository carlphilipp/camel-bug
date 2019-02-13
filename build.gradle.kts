import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
    `java-library`
    eclipse
    maven
    id("org.springframework.boot") version "2.1.0.RELEASE"
    id("io.spring.dependency-management") version "1.0.6.RELEASE"
}

val camelVersion = "2.23.0"
val embedMongo = "2.2.0"
val restAssuredVersion = "3.2.0"
val springBootVersion = "2.1.0.RELEASE"

group = "org.apache.camel.bug"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    implementation("org.apache.camel:camel-servlet-starter:$camelVersion")
    implementation("org.apache.camel:camel-jackson:$camelVersion")
    implementation("org.apache.camel:camel-mongodb3:$camelVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation("org.apache.camel:camel-test-spring:$camelVersion")
    testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:$embedMongo")
}

tasks {

    test {
        testLogging {
            events = setOf(PASSED, SKIPPED, FAILED)
            showStandardStreams = false
        }
    }
}