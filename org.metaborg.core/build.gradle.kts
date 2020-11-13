plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

dependencies {
  api(platform("org.metaborg:parent:$version"))

  api("org.metaborg:org.metaborg.util:$version")

  api("org.slf4j:slf4j-api")
  api("com.google.inject:guice")
  api("com.google.inject.extensions:guice-multibindings")
  api("commons-io:commons-io")
  api("org.apache.commons:commons-vfs2")
  api("org.apache.commons:commons-lang3")
  api("org.apache.commons:commons-configuration2")
  api("com.virtlink.commons:commons-configuration2-jackson")
  api("com.fasterxml.jackson.core:jackson-core")
  api("com.fasterxml.jackson.core:jackson-databind")
  api("com.fasterxml.jackson.core:jackson-annotations")
  api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
  api("com.google.guava:guava")
  api("io.reactivex.rxjava3:rxjava")

  compileOnly("com.google.code.findbugs:jsr305")

  testCompileOnly("junit:junit")
  testCompileOnly("com.google.code.findbugs:jsr305")
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
