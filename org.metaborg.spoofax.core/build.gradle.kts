plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

dependencies {
  api(platform("org.metaborg:parent:$version"))

  api(project(":org.metaborg.core"))

  api("org.metaborg:org.metaborg.util:$version")
  api("org.metaborg:org.spoofax.terms:$version")
  implementation("org.metaborg:nabl2.solver:$version")
  implementation("org.metaborg:statix.solver:$version")
  implementation("org.metaborg:flowspec.runtime:$version")
  api("org.metaborg:org.spoofax.interpreter.core:$version")
  implementation("org.metaborg:org.spoofax.jsglr2:$version")
  api("org.metaborg:org.strategoxt.strj:$version")
  implementation("org.metaborg:org.spoofax.interpreter.library.index:$version")
  implementation("org.metaborg:org.metaborg.runtime.task:$version")

  api("org.slf4j:slf4j-api")
  api("com.google.inject:guice")
  api("com.google.inject.extensions:guice-multibindings")
  implementation("commons-io:commons-io")
  implementation("org.apache.commons:commons-vfs2")
  implementation("com.google.guava:guava")
  implementation("io.reactivex.rxjava3:rxjava")

  compileOnly("com.google.code.findbugs:jsr305")

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
