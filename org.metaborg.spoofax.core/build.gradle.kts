plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

dependencies {
  compile(project(":org.metaborg.core"))

  compile("org.metaborg:org.metaborg.util:2.6.0-SNAPSHOT")
  compile("org.metaborg:org.spoofax.terms:2.6.0-SNAPSHOT")
  compile("org.metaborg:nabl2.solver:2.6.0-SNAPSHOT")
  compile("org.metaborg:statix.solver:2.6.0-SNAPSHOT")
  compile("org.metaborg:flowspec.runtime:2.6.0-SNAPSHOT")
  compile("org.metaborg:org.spoofax.interpreter.core:2.6.0-SNAPSHOT")
  compile("org.metaborg:org.spoofax.jsglr2:2.6.0-SNAPSHOT")
  compile("org.metaborg:org.strategoxt.strj:2.6.0-SNAPSHOT")
  compile("org.metaborg:org.spoofax.interpreter.library.index:2.6.0-SNAPSHOT")
  compile("org.metaborg:org.metaborg.runtime.task:2.6.0-SNAPSHOT")
  compile("org.metaborg:org.spoofax.terms.typesmart:2.6.0-SNAPSHOT")

  compile("org.slf4j:slf4j-api:1.7.25")
  compile("com.google.inject:guice:4.2.0")
  compile("com.google.inject.extensions:guice-multibindings:4.2.0")
  compile("commons-io:commons-io:2.6")
  compile("org.apache.commons:commons-vfs2:2.2")
  compile("com.google.guava:guava:26.0-jre")
  compile("com.netflix.rxjava:rxjava-core:0.20.7")
  compile("org.metaborg:characterclasses:2.6.0-SNAPSHOT")
  
  compileOnly("com.google.code.findbugs:jsr305:3.0.2")

  testCompile(project(":org.metaborg.core.test"))
  testCompileOnly("junit:junit:4.12")
  testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.1.0")
  testCompile("ch.qos.logback:logback-core:1.1.2")
  testCompile("ch.qos.logback:logback-classic:1.1.2")
  testCompile("org.slf4j:jcl-over-slf4j:1.7.25")
}

// Copy test resources into classes directory, to make them accessible as classloader resources at runtime.
val copyTestResourcesTask = tasks.create<Copy>("copyTestResources") {
  from("$projectDir/src/test/resources")
  into("$buildDir/classes/java/test")
}
tasks.getByName("processTestResources").dependsOn(copyTestResourcesTask)
