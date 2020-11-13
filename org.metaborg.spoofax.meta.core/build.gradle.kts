plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform("org.metaborg:parent:$version"))

  api(project(":org.metaborg.core"))
  api(project(":org.metaborg.meta.core"))
  api(project(":org.metaborg.spoofax.core"))

  implementation("org.metaborg:org.metaborg.spoofax.nativebundle:$version") // TODO: project dependency
  api("org.metaborg:org.metaborg.util:$version")
  api("org.metaborg:strategoxt-min-jar:$version")
  implementation("org.metaborg:make-permissive:$version")
  implementation("org.metaborg:sdf2table:$version")
  implementation("org.metaborg:sdf2parenthesize:$version")
  implementation("org.metaborg:org.metaborg.parsetable:$version")
  implementation("org.metaborg:nabl2.solver:$version")
  implementation("org.metaborg:stratego.compiler.pack:$version")
  implementation("org.metaborg:stratego.build:$version")
  implementation("org.metaborg:stratego.build.spoofax2:$version")
  implementation("build.pluto:pluto")
  implementation("build.pluto:build-java")
  implementation("org.metaborg:log.backend.slf4j:0.4.0")
  implementation("org.metaborg:pie.runtime")
  implementation("org.metaborg:pie.taskdefs.guice")
  api("com.google.inject:guice")
  api("com.google.inject.extensions:guice-multibindings")
  implementation("com.github.spullara.mustache.java:compiler")
  implementation("org.apache.ant:ant:1.9.6")
  implementation("ant-contrib:ant-contrib:1.0b3")

  compileOnly("com.google.code.findbugs:jsr305")
}
