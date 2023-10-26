plugins {
  id("org.metaborg.gradle.config.java-library")
}

val spoofax2Version: String by ext
dependencies {
  api(platform("org.metaborg:parent:$spoofax2Version"))

  api(project(":org.metaborg.core"))

  compileOnly("jakarta.annotation:jakarta.annotation-api")
}
