import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val springSecurityVersion = "5.7.11"
    implementation(platform("org.springframework.security:spring-security-bom:$springSecurityVersion"))
    implementation("org.springframework.security:spring-security-core")
    implementation("org.springframework.security:spring-security-crypto")
    implementation("org.springframework.security:spring-security-config")
    implementation("org.springframework.security:spring-security-web")
    val springBootVersion = "2.7.17"
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")

    val mybatisVersion = "2.1.4"
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:$mybatisVersion")
    testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter-test:2.3.1")

    implementation("com.mysql:mysql-connector-j:8.2.0")
    implementation("org.bouncycastle:bcprov-jdk18on:1.77")

    val testContVersion = "1.19.2"
    testImplementation(platform("org.testcontainers:testcontainers-bom:$testContVersion"))
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")

    testImplementation(kotlin("test"))
    implementation(kotlin("stdlib"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}