plugins {
  id("org.metaborg.gradle.config.java-library")
}

val spoofax2Version: String by ext
dependencies {
  api(platform("org.metaborg:parent:$spoofax2Version"))

  implementation("org.apache.commons:commons-lang3")
}
