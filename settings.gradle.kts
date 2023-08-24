rootProject.name = "discord-roons-bot"

pluginManagement {
    plugins {
        kotlin("jvm") version "1.9.0"
        id("org.jetbrains.kotlin.plugin.allopen") version "1.9.10"
        id("org.jetbrains.kotlin.plugin.noarg") version "1.9.0"
        id("org.jetbrains.kotlin.plugin.spring") version "1.9.0"
        id("org.jetbrains.kotlin.plugin.jpa") version "1.9.0"
        id("org.springframework.boot") version "3.1.2"
        id("org.sonarqube") version "4.3.1.3277"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://m2.dv8tion.net/releases")
        jcenter() // needed for 'lavadsp'
    }
}

include(":api")