import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.ZoneId

plugins {
    kotlin("multiplatform") version "1.9.0" apply false
    id("maven-publish")
    signing
    id("io.gitlab.arturbosch.detekt") version "1.23.0"
    id("com.palantir.git-version") version "3.0.0"
    id("io.kotest.multiplatform") version "5.6.2"
    id("org.jetbrains.dokka") version "1.8.20"
}

buildscript {
    extra.set("kotlin_version", "1.9.0")
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

allprojects {
    repositories {
        mavenCentral()
        google()
    }

    group = "pro.felixo"
    version = version()

    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "io.kotest.multiplatform")

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
        config.setFrom(files("$rootDir/config/detekt/config.yml"))
    }
}

subprojects {
    apply(plugin = "org.jetbrains.dokka")
    apply<SigningPlugin>()
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

        publications.withType<MavenPublication> {
            val javadocJar = tasks.register("javadocJar$name", Jar::class) {
                archiveClassifier.set("javadoc")
                archiveBaseName.set("javadoc-${this@withType.name}")
                from(tasks.dokkaHtml)
            }

            artifact(javadocJar.get())

            pom {
                name = this@subprojects.name
                description = "Protocol Buffers 3 support for Kotlin Multiplatform"
                url = "https://github.com/berlix/protobuf-kotlin"
                licenses {
                    license {
                        name = "MIT License"
                        url = "https://opensource.org/license/mit/"
                    }
                }
                developers {
                    developer {
                        id = "berlix"
                        name = "Felix Engelhardt"
                        email = "pub@felix-engelhardt.de"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/berlix/protobuf-kotlin.git"
                    developerConnection = "scm:git:git@github.com:berlix/protobuf-kotlin.git"
                    url = "https://github.com/berlix/protobuf-kotlin"
                }
            }

            signing {
                useInMemoryPgpKeys(System.getenv("OSSRH_GPG_KEY"), System.getenv("OSSRH_GPG_PASSWORD"))
                sign(this@withType)
            }
        }
    }
}
