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
    api(libs.spoofax.terms)
    api(libs.jsglr.shared)
    api(libs.interpreter.core)
    api(libs.strategoxt.strj)
    api(libs.nabl2.terms)
    implementation(libs.jsglr2)
    implementation(libs.sdf2table)
    implementation(libs.nabl2.solver)
    implementation(libs.statix.solver)
    implementation(libs.interpreter.library.index)
    implementation(libs.nabl.renaming.java)

    implementation(libs.spoofax2.flowspec.runtime)
    implementation(libs.spoofax2.metaborg.runtime.task)

    api(libs.slf4j.api)
    api(libs.guice)
    implementation(libs.commons.io)
    implementation(libs.commons.vfs2)
    implementation(libs.guava)
    // Required for Guava >= 27.0:
    implementation(libs.failureaccess)
    implementation(libs.rxjava)

    implementation(libs.jakarta.annotation)
    implementation(libs.jakarta.inject)

    testImplementation(project(":org.metaborg.core.test"))
    testImplementation(libs.junit)
    testCompileOnly(libs.junit4)
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
