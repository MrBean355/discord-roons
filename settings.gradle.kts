rootProject.name = "discord-roons-bot"

pluginManagement {
    plugins {
        kotlin("jvm") version "1.8.21"
        id("org.jetbrains.kotlin.plugin.allopen") version "1.8.21"
        id("org.jetbrains.kotlin.plugin.noarg") version "1.8.21"
        id("org.jetbrains.kotlin.plugin.spring") version "1.8.21"
        id("org.jetbrains.kotlin.plugin.jpa") version "1.8.21"
        id("org.springframework.boot") version "3.1.0"
        id("org.sonarqube") version "4.2.0.3129"
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