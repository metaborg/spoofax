plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

val spoofax2Version: String by ext
dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    api(project(":org.metaborg.core"))
    api("junit:junit")

    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("io.reactivex.rxjava3:rxjava")
}
