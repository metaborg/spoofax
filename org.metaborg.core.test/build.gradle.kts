plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform("org.metaborg:parent:$version"))

  api(project(":org.metaborg.core"))
  api("junit:junit")

  compileOnly("com.google.code.findbugs:jsr305")
}
