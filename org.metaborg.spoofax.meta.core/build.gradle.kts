plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform("org.metaborg:parent:$version"))

  api(project(":org.metaborg.core"))
  api(project(":org.metaborg.meta.core"))
  api(project(":org.metaborg.spoofax.core"))

  api("org.metaborg:org.metaborg.spoofax.nativebundle:$version") // TODO: project dependency
  api("org.metaborg:org.metaborg.util:$version")
  api("org.metaborg:strategoxt-min-jar:$version")
  api("org.metaborg:make-permissive:$version")
  api("org.metaborg:sdf2table:$version")
  api("org.metaborg:sdf2parenthesize:$version")
  api("org.metaborg:tableinterfaces:$version")
  api("org.metaborg:stratego.compiler.pack:$version")

  api("build.pluto:pluto")
  api("build.pluto:build-java")
  api("com.google.inject:guice")
  api("com.google.inject.extensions:guice-multibindings")
  api("com.github.spullara.mustache.java:compiler")
  api("org.apache.ant:ant:1.9.6")
  api("ant-contrib:ant-contrib:1.0b3")

  compileOnly("com.google.code.findbugs:jsr305")
}
