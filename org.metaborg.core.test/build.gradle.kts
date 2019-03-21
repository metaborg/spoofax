plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  dependencies {
    compile(project(":org.metaborg.core"))

    compileOnly("com.google.code.findbugs:jsr305:3.0.2")

    testCompileOnly("junit:junit:4.12")
  }
}
