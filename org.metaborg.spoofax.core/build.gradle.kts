plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

dependencies {
  api(platform("org.metaborg:parent:$version"))

  api(project(":org.metaborg.core"))

  api("org.metaborg:org.metaborg.util:$version")
  api("org.metaborg:org.spoofax.terms:$version")
  api("org.metaborg:nabl2.solver:$version")
  api("org.metaborg:statix.solver:$version")
  api("org.metaborg:flowspec.runtime:$version")
  api("org.metaborg:org.spoofax.interpreter.core:$version")
  api("org.metaborg:org.spoofax.jsglr2:$version")
  api("org.metaborg:org.strategoxt.strj:$version")
  api("org.metaborg:org.spoofax.interpreter.library.index:$version")
  api("org.metaborg:org.metaborg.runtime.task:$version")

  api("org.slf4j:slf4j-api")
  api("com.google.inject:guice")
  api("com.google.inject.extensions:guice-multibindings")
  api("commons-io:commons-io")
  api("org.apache.commons:commons-vfs2")
  api("com.google.guava:guava")
  api("io.reactivex.rxjava3:rxjava")

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
