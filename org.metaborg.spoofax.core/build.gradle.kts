plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

fun compositeBuild(name: String) = "$group:$name:$version"
val spoofax2Version: String by ext
dependencies {
  api(platform("org.metaborg:parent:$spoofax2Version"))

  api(project(":org.metaborg.core"))

  api(compositeBuild("org.metaborg.util"))
  api(compositeBuild("org.spoofax.terms"))
  api(compositeBuild("org.spoofax.interpreter.core"))
  api(compositeBuild("org.strategoxt.strj"))
  api(compositeBuild("nabl2.terms"))
  implementation(compositeBuild("org.spoofax.jsglr2"))
  implementation(compositeBuild("nabl2.solver"))
  implementation(compositeBuild("statix.solver"))
  implementation(compositeBuild("org.spoofax.interpreter.library.index"))
  implementation(compositeBuild("renaming.java"))

  implementation("org.metaborg:flowspec.runtime:$spoofax2Version")
  implementation("org.metaborg:org.metaborg.runtime.task:$spoofax2Version")

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
