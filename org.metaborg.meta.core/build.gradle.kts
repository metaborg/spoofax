plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    api(project(":org.metaborg.core"))
    api(libs.metaborg.util)
    api(libs.util.vfs2)

    implementation(libs.jakarta.annotation)
    implementation(libs.jakarta.inject)
}
