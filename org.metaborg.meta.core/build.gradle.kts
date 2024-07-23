plugins {
    id("org.metaborg.gradle.config.java-library")
}

fun compositeBuild(name: String) = "$group:$name:$version"
val spoofax2Version: String by ext
dependencies {
    api(platform("org.metaborg:parent:$spoofax2Version"))

    api(project(":org.metaborg.core"))
    api(compositeBuild("org.metaborg.util"))
    api(compositeBuild("util-vfs2"))

    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("jakarta.inject:jakarta.inject-api")
}
