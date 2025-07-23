plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
}

group = "io.github.msaggik"
version = libs.versions.versionName.get()

android {
    namespace = "io.github.msaggik.locationmanagerlite"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = libs.versions.jvmTarget.get()
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.annotation)
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(android.sourceSets["main"].java.srcDirs)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["release"])
                groupId = project.group.toString()
                artifactId = "location-manager-lite"
                version = project.version.toString()

                pom {
                    name.set("location-manager-lite")
                    description.set("A lightweight location library for Android written in Kotlin for easy and configurable access to location data.")
                    url.set("https://github.com/MSagGik/location-manager-lite")

                    licenses {
                        license {
                            name.set("Apache License 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0")
                        }
                    }

                    developers {
                        developer {
                            id.set("MSagGik")
                            name.set("Maxim Sagaciyang")
                            email.set("dev.saggik@yandex.com")
                            url.set("https://msaggik.github.io")
                        }
                    }

                    scm {
                        connection.set("scm:git:https://github.com/MSagGik/location-manager-lite.git")
                        developerConnection.set("scm:git:ssh://github.com:MSagGik/location-manager-lite.git")
                        url.set("https://github.com/MSagGik/location-manager-lite")
                    }
                }
            }
        }
    }
}