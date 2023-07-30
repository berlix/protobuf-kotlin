plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.9.0"
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    @Suppress("UNUSED_VARIABLE", "KotlinRedundantDiagnosticSuppress")
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":wire"))
                api(project(":schema-document"))
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":protoscope"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("com.willowtreeapps.assertk:assertk:0.26.1")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
