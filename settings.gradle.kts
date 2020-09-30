rootProject.name = "spoofax2.root"

pluginManagement {
  repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
  }
}

if(org.gradle.util.VersionNumber.parse(gradle.gradleVersion).major < 6) {
  enableFeaturePreview("GRADLE_METADATA")
}

include("org.metaborg.core")
include("org.metaborg.core.test")
include("org.metaborg.spoofax.core")
include("org.metaborg.meta.core")
include("org.metaborg.spoofax.meta.core")
include("meta.lib.spoofax")
