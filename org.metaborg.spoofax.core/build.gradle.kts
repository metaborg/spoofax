plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.gradle.config.junit-testing")
}

fun compositeBuild(name: String) = "$group:$name:$version"
val spoofax2Version: String by ext
dependencies {
    api(platform("org.metaborg:parent:$spoofax2Version"))

    api(project(":org.metaborg.core"))

    api(libs.spoofax2.metaborg.util)
    api(libs.spoofax2.util.vfs2)
    api(libs.spoofax2.terms)
    api(libs.spoofax2.jsglr.shared)
    api(libs.spoofax2.interpreter.core)
    api(libs.spoofax2.strategoxt.strj)
    api(libs.spoofax2.nabl2.terms)
    implementation(libs.spoofax2.jsglr2)
    implementation(libs.spoofax2.sdf2table)
    implementation(libs.spoofax2.nabl2.solver)
    implementation(libs.spoofax2.statix.solver)
    implementation(libs.spoofax2.interpreter.library.index)
    implementation(libs.spoofax2.renaming.java)

    implementation(libs.spoofax2.flowspec.runtime)
    implementation(libs.spoofax2.runtime.task)

    api("org.slf4j:slf4j-api")
    api("com.google.inject:guice")
    implementation("commons-io:commons-io")
    implementation("org.apache.commons:commons-vfs2")
    implementation("com.google.guava:guava")
    // Required for Guava >= 27.0:
    implementation("com.google.guava:failureaccess")
    implementation("io.reactivex.rxjava3:rxjava")

    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("jakarta.inject:jakarta.inject-api")

    testImplementation(project(":org.metaborg.core.test"))
    testCompileOnly("junit:junit")
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
