plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.9.24"
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    js(IR) {
        browser()
        nodejs()
    }

    @Suppress("UNUSED_VARIABLE", "KotlinRedundantDiagnosticSuppress")
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":protobuf-kotlin-wire"))
                api(project(":protobuf-kotlin-schemadocument"))
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":protobuf-kotlin-protoscope"))
                implementation("io.kotest:kotest-framework-engine:5.9.0")
                implementation("com.willowtreeapps.assertk:assertk:0.28.1")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("io.kotest:kotest-runner-junit5-jvm:5.9.0")
            }
        }
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}
