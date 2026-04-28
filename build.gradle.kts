plugins {
    kotlin("jvm") version "2.3.21"
    `maven-publish`
    signing
}

group = "com.philiprehberger"
version = project.findProperty("version") as String? ?: "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    explicitApi()
    jvmToolchain(17)
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("id-gen")
                description.set("ID generation for Kotlin: ULID, NanoID, Snowflake, and prefixed IDs")
                url.set("https://github.com/philiprehberger/kt-id-gen")
                licenses { license { name.set("MIT License"); url.set("https://opensource.org/licenses/MIT") } }
                developers { developer { id.set("philiprehberger"); name.set("Philip Rehberger") } }
                scm {
                    url.set("https://github.com/philiprehberger/kt-id-gen")
                    connection.set("scm:git:git://github.com/philiprehberger/kt-id-gen.git")
                    developerConnection.set("scm:git:ssh://github.com/philiprehberger/kt-id-gen.git")
                }
                issueManagement {
                    system.set("GitHub Issues")
                    url.set("https://github.com/philiprehberger/kt-id-gen/issues")
                }
            }
        }
    }
    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(System.getenv("GPG_PRIVATE_KEY"), System.getenv("GPG_PASSPHRASE"))
    sign(publishing.publications["maven"])
}
