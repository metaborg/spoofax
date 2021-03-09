plugins {
  id("org.metaborg.gradle.config.java-library")
}

fun compositeBuild(name: String) = "$group:$name:$version"
val pieVersion = "0.14.0"
val spoofax2Version: String by ext
dependencies {
  api(platform("org.metaborg:parent:$spoofax2Version"))

  api(project(":org.metaborg.core"))
  api(project(":org.metaborg.meta.core"))
  api(project(":org.metaborg.spoofax.core"))

  implementation(compositeBuild("org.metaborg.util"))
  implementation(compositeBuild("sdf2table"))
  implementation(compositeBuild("sdf2parenthesize"))
  implementation(compositeBuild("org.metaborg.parsetable"))
  implementation(compositeBuild("stratego.compiler.pack"))
  implementation(compositeBuild("stratego.build"))
  implementation(compositeBuild("stratego.build.spoofax2"))
  implementation(compositeBuild("nabl2.solver"))

  implementation(project(":org.metaborg.spoofax.nativebundle"))
  implementation("org.metaborg:strategoxt-min-jar:$spoofax2Version")
  implementation("org.metaborg:make-permissive:$spoofax2Version")

  implementation("build.pluto:pluto")
  implementation("build.pluto:build-java")
  implementation("org.metaborg:log.backend.slf4j:0.5.0")
  implementation("org.metaborg:pie.runtime:$pieVersion")
  implementation("org.metaborg:pie.taskdefs.guice:$pieVersion")
  api("com.google.inject:guice")
  api("com.google.inject.extensions:guice-multibindings")
  implementation("com.github.spullara.mustache.java:compiler")
  implementation("org.apache.ant:ant:1.9.6")
  implementation("ant-contrib:ant-contrib:1.0b3")

  compileOnly("com.google.code.findbugs:jsr305")
}
