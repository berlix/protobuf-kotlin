import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.ZoneId

plugins {
    kotlin("multiplatform") version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
    id("maven-publish")
    id("io.gitlab.arturbosch.detekt") version "1.22.0"
    id("com.palantir.git-version") version "2.0.0"
}

buildscript {
    extra.set("kotlin_version", "1.8.10")
    repositories {
        google()
        mavenCentral()
    }
}

val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra

fun version(): String = versionDetails().run {
    if (commitDistance == 0 && isCleanTag && lastTag.matches(Regex("""\d+\.\d+\.\d+""")))
        version
    else (
            System.getenv("GITHUB_RUN_NUMBER")?.let { "ci-${branchName}-$it-${gitHash}" }
                ?: "dev-${branchName}-${
                    DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.of("UTC")).format(Instant.now())
                }-${gitHash}"
            )
}

repositories {
    mavenCentral()
    google()
}

group = "pro.felixo"
version = version()

apply(plugin = "io.gitlab.arturbosch.detekt")

detekt {
    source.setFrom(
        "src/commonMain/kotlin",
        "src/commonTest/kotlin",
        "src/jvmMain/kotlin",
        "src/jvmTest/kotlin",
        "src/jsMain/kotlin",
        "src/jsTest/kotlin"
    )
    buildUponDefaultConfig = true
    config = files("$rootDir/config/detekt/config.yml")
}

apply<MavenPublishPlugin>()

publishing {
    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_TOKEN")
            }
        }
    }
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.4.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("com.willowtreeapps.assertk:assertk:0.25")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
