plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

fun compositeBuild(name: String) = "$group:$name:$version"
val spoofax2Version: String by ext
dependencies {
    api(platform("org.metaborg:parent:$spoofax2Version"))

    api(compositeBuild("org.metaborg.util"))
    api(compositeBuild("util-vfs2"))

    api("org.slf4j:slf4j-api")
    api("com.google.inject:guice")
    implementation("commons-io:commons-io")
    api("org.apache.commons:commons-vfs2")
    implementation("org.apache.commons:commons-lang3")
    api("org.apache.commons:commons-configuration2")
    api("com.virtlink.commons:commons-configuration2-jackson")
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("com.google.guava:guava")
    // Required for Guava >= 27.0:
    implementation("com.google.guava:failureaccess")
    implementation("io.reactivex.rxjava3:rxjava")

    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("jakarta.inject:jakarta.inject-api")

    testCompileOnly("junit:junit")
    testCompileOnly("jakarta.annotation:jakarta.annotation-api")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
    testImplementation("ch.qos.logback:logback-core")
    testImplementation("ch.qos.logback:logback-classic")
    testImplementation("org.slf4j:jcl-over-slf4j")
}

// Copy test resources into classes directory, to make them accessible as classloader resources at runtime.
val copyTestResourcesTask = tasks.create<Copy>("copyTestResources") {
    from("$projectDir/src/test/resources")
    into("$buildDir/classes/java/test")
}
tasks.getByName("processTestResources").dependsOn(copyTestResourcesTask)
