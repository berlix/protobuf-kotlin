plugins {
    kotlin("multiplatform")
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
                api(project(":common"))
            }
        }
        val commonTest by getting {
            dependencies {
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
