plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  compile(project(":org.metaborg.core"))
  compile(project(":org.metaborg.meta.core"))
  compile(project(":org.metaborg.spoofax.core"))

  compile("org.metaborg:org.metaborg.spoofax.nativebundle:2.6.0-SNAPSHOT")
  compile("org.metaborg:org.metaborg.util:2.6.0-SNAPSHOT")
  compile("org.metaborg:strategoxt-min-jar:2.6.0-SNAPSHOT")
  compile("org.metaborg:make-permissive:2.6.0-SNAPSHOT")
  compile("org.metaborg:sdf2table:2.6.0-SNAPSHOT")
  compile("org.metaborg:sdf2parenthesize:2.6.0-SNAPSHOT")
  compile("org.metaborg:characterclasses:2.6.0-SNAPSHOT")
  compile("org.metaborg:tableinterfaces:2.6.0-SNAPSHOT")
  compile("org.metaborg:stratego.compiler.pack:2.6.0-SNAPSHOT")

  compile("build.pluto:pluto:1.11.0")
  compile("build.pluto:build-java:1.7.0")
  compile("com.google.inject:guice:4.2.0")
  compile("com.google.inject.extensions:guice-multibindings:4.2.0")
  compile("com.github.spullara.mustache.java:compiler:0.9.2")
  compile("org.apache.ant:ant:1.9.6")
  compile("ant-contrib:ant-contrib:1.0b3")

  compileOnly("com.google.code.findbugs:jsr305:3.0.2")
}
