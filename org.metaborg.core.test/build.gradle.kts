plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    api(project(":org.metaborg.core"))
    api(libs.junit4)

    implementation(libs.jakarta.annotation)
    implementation(libs.rxjava)
}
