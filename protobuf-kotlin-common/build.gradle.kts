plugins {
    kotlin("multiplatform")
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
        val commonTest by getting {
            dependencies {
                implementation("io.kotest:kotest-framework-engine:5.9.1")
                implementation("com.willowtreeapps.assertk:assertk:0.28.1")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("io.kotest:kotest-runner-junit5-jvm:5.9.1")
            }
        }
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}
