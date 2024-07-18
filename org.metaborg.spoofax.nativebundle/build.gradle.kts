plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

val spoofax2Version: String by ext
dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    implementation("org.apache.commons:commons-lang3")
}
