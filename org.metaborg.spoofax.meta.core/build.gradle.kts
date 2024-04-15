plugins {
  id("org.metaborg.gradle.config.java-library")
}

fun compositeBuild(name: String) = "$group:$name:$version"
val spoofax2Version: String by ext
val pieVersion = "0.18.0" // HACK: override PIE version to make it binary compatible with this version.
dependencies {
  api(platform("org.metaborg:parent:$spoofax2Version"))

  api(project(":org.metaborg.core"))
  api(project(":org.metaborg.meta.core"))
  api(project(":org.metaborg.spoofax.core"))

  implementation(compositeBuild("org.metaborg.util"))
  implementation(compositeBuild("jsglr.shared"))
  implementation(compositeBuild("sdf2table"))
  implementation(compositeBuild("sdf2parenthesize"))
  implementation(compositeBuild("org.metaborg.parsetable"))
  implementation(compositeBuild("stratego.build"))
  implementation(compositeBuild("stratego.build.spoofax2"))
  implementation(compositeBuild("nabl2.solver"))
  implementation(compositeBuild("statix.solver"))

  implementation(project(":org.metaborg.spoofax.nativebundle"))
  implementation("org.metaborg:strategoxt-min-jar:$spoofax2Version")
  implementation("org.metaborg:make-permissive:$spoofax2Version")

  implementation("build.pluto:pluto")
  implementation("build.pluto:build-java")
  implementation("org.metaborg:log.backend.slf4j:0.5.0")
  implementation("org.metaborg:pie.runtime:$pieVersion")
  implementation("org.metaborg:pie.taskdefs.guice:$pieVersion")
  api("com.google.inject:guice")
  implementation("com.github.spullara.mustache.java:compiler")
  implementation("org.apache.ant:ant:1.9.6")
  implementation("ant-contrib:ant-contrib:1.0b3")

  implementation("jakarta.annotation:jakarta.annotation-api")
  implementation("jakarta.inject:jakarta.inject-api")
}
