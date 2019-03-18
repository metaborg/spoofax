rootProject.name = "spoofax"

pluginManagement {
  repositories {
    // Get plugins from artifacts.metaborg.org, first.
    maven("https://artifacts.metaborg.org/content/repositories/releases/")
    maven("https://artifacts.metaborg.org/content/repositories/snapshots/")
    // Required by several Gradle plugins (Maven central).
    maven("https://artifacts.metaborg.org/content/repositories/central/") // Maven central mirror.
    mavenCentral() // Maven central as backup.
    // Get plugins from Gradle plugin portal.
    gradlePluginPortal()
  }
}

include("org.metaborg.core")
include("org.metaborg.core.test")
include("org.metaborg.spoofax.core")
include("org.metaborg.meta.core")
include("org.metaborg.spoofax.meta.core")
