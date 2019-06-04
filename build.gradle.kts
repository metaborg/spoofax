plugins {
  id("org.metaborg.gradle.config.root-project") version "0.3.8"
  id("org.metaborg.gitonium") version "0.1.2"
}

subprojects {
  metaborg {
    configureSubProject()
  }
}

allprojects {
  version = "2.6.0-SNAPSHOT" // Override version from Git, as Spoofax Core uses a different versioning scheme.
  
  repositories {
    maven("https://pluto-build.github.io/mvnrepository/")
    maven("https://sugar-lang.github.io/mvnrepository/")
    maven("http://nexus.usethesource.io/content/repositories/public/")
  }
}
