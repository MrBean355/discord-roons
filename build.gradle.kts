/*
 * Copyright 2021 Michael Johnston
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.sonarqube.gradle.SonarQubeTask

plugins {
    kotlin("jvm") version "1.4.32"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.4.32"
    id("org.jetbrains.kotlin.plugin.noarg") version "1.4.32"
    id("org.jetbrains.kotlin.plugin.spring") version "1.4.32"
    id("org.jetbrains.kotlin.plugin.jpa") version "1.4.32"
    id("org.springframework.boot") version "2.4.5"
    id("org.sonarqube") version "3.1.1"
    jacoco
}

group = "com.github.mrbean355"
version = "1.14.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

java {
    sourceCompatibility = JavaVersion.VERSION_15
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JacocoReport> {
    dependsOn(tasks.test)
    sourceSets(sourceSets.main.get())
    reports {
        xml.isEnabled = true
    }
}

tasks.withType<SonarQubeTask> {
    dependsOn(tasks.named("jacocoTestReport"))
}

sonarqube {
    properties {
        property("sonar.projectKey", "discord-roons-bot")
        property("sonar.organization", "admiral-bulldog-sounds")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")

    implementation("org.springframework.boot:spring-boot-starter-data-rest:2.4.4")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:2.4.5")
    implementation("mysql:mysql-connector-java:8.0.23")
    implementation("com.google.code.gson:gson:2.8.6")

    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")
    implementation("net.dv8tion:JDA:4.2.0_247")
    implementation("com.sedmelluq:lavaplayer:1.3.73")
    implementation("com.github.natanbc:lavadsp:0.7.6")
    implementation("com.vdurmont:semver4j:3.1.0")
    implementation("org.telegram:telegrambots:5.2.0")
    implementation("org.telegram:telegrambots-spring-boot-starter:5.1.1")

    testImplementation(platform("org.junit:junit-bom:5.7.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.mockk:mockk:1.11.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.4.3")
}