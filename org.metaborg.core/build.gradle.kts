plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

dependencies {
  dependencies {
    compile("org.metaborg:org.metaborg.util:2.6.0-SNAPSHOT")

    compile("org.slf4j:slf4j-api:1.7.25")
    compile("com.google.inject:guice:4.2.0")
    compile("com.google.inject.extensions:guice-multibindings:4.2.0")
    compile("commons-io:commons-io:2.6")
    compile("org.apache.commons:commons-vfs2:2.2")
    compile("org.apache.commons:commons-lang3:3.4")
    compile("org.apache.commons:commons-configuration2:2.2")
    compile("com.virtlink.commons:commons-configuration2-jackson:0.7.0")
    compile("com.fasterxml.jackson.core:jackson-core:2.9.5")
    compile("com.fasterxml.jackson.core:jackson-databind:2.9.5")
    compile("com.fasterxml.jackson.core:jackson-annotations:2.9.5")
    compile("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.5")
    compile("com.google.guava:guava:26.0-jre")
    compile("com.netflix.rxjava:rxjava-core:0.20.7")

    compileOnly("com.google.code.findbugs:jsr305:3.0.2")

    testCompileOnly("junit:junit:4.12")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.1.0")
    testCompile("ch.qos.logback:logback-core:1.1.2")
    testCompile("ch.qos.logback:logback-classic:1.1.2")
    testCompile("org.slf4j:jcl-over-slf4j:1.7.25")
  }
}

// Copy test resources into classes directory, to make them accessible as classloader resources at runtime.
val copyTestResourcesTask = tasks.create<Copy>("copyTestResources") {
  from("$projectDir/src/test/resources")
  into("$buildDir/classes/java/test")
}
tasks.getByName("processTestResources").dependsOn(copyTestResourcesTask)
