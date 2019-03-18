plugins {
  id("org.metaborg.gradle.config.root-project") version "0.3.5"
  id("org.metaborg.gitonium") version "0.1.0"
}

subprojects {
  metaborg {
    configureSubProject()
  }
}

allprojects {
  repositories {
    maven("https://pluto-build.github.io/mvnrepository/")
    maven("https://sugar-lang.github.io/mvnrepository/")
    maven("http://nexus.usethesource.io/content/repositories/public/")
  }
}
