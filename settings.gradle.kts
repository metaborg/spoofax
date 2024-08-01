// !! THIS FILE WAS GENERATED USING repoman !!
// Modify `repo.yaml` instead and use `repoman` to update this file
// See: https://github.com/metaborg/metaborg-gradle/

dependencyResolutionManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
        mavenCentral()
    }
}

pluginManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
        gradlePluginPortal()
    }
}

plugins {
    id("org.metaborg.convention.settings") version "latest.integration"
}

rootProject.name = "spoofax2-project"
include(":meta.lib.spoofax")
include(":org.metaborg.core")
include(":org.metaborg.core.test")
include(":org.metaborg.meta.core")
include(":org.metaborg.spoofax.core")
include(":org.metaborg.spoofax.meta.core")
include(":org.metaborg.spoofax.nativebundle")
