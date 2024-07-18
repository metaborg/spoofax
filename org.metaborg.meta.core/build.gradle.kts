plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

fun compositeBuild(name: String) = "$group:$name:$version"
val spoofax2Version: String by ext
dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    api(project(":org.metaborg.core"))
    api(compositeBuild("org.metaborg.util"))
    api(compositeBuild("util-vfs2"))

    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("jakarta.inject:jakarta.inject-api")
}
