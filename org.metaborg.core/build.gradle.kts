plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    api(libs.metaborg.util)
    api(libs.util.vfs2)

    api(libs.slf4j.api)
    api(libs.guice)
    implementation(libs.commons.io)
    api(libs.commons.vfs2)
    implementation(libs.commons.lang3)
    api(libs.commons.configuration2)
    api(libs.commons.configuration2.jackson)
    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.annotations)
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.guava)
    // Required for Guava >= 27.0:
    implementation(libs.failureaccess)
    implementation(libs.rxjava)

    implementation(libs.jakarta.annotation)
    implementation(libs.jakarta.inject)

    testImplementation(libs.junit)
    testCompileOnly(libs.junit4)
    testCompileOnly(libs.jakarta.annotation)
    testRuntimeOnly(libs.junit.vintage)
    testImplementation(libs.logback.core)
    testImplementation(libs.logback)
    testImplementation(libs.jcl.over.slf4j)
}

// Copy test resources into classes directory, to make them accessible as classloader resources at runtime.
val copyTestResourcesTask = tasks.create<Copy>("copyTestResources") {
    from("$projectDir/src/test/resources")
    into("$buildDir/classes/java/test")
}
tasks.getByName("processTestResources").dependsOn(copyTestResourcesTask)
