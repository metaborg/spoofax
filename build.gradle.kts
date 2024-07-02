plugins {
    `java-library`
    alias(libs.plugins.gitonium)
    alias(libs.plugins.metaborg.gradle.rootproject)
}

val spoofax2Version: String = System.getProperty("spoofax2Version")
val spoofax2BaselineVersion: String = System.getProperty("spoofax2BaselineVersion")
val spoofax2DevenvVersion: String = System.getProperty("spoofax2DevenvVersion")
allprojects {
    group = "org.metaborg"
    ext["spoofax2Version"] = spoofax2Version
    ext["spoofax2BaselineVersion"] = spoofax2BaselineVersion
    ext["spoofax2DevenvVersion"] = spoofax2DevenvVersion

    repositories {
        maven(url = "https://artifacts.metaborg.org/content/groups/public/")
        mavenCentral() // Backup
    }
}
