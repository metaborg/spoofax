rootProject.name = "spoofax2.root"

pluginManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
    }
}

// This allows us to use the catalog in dependencies
dependencyResolutionManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
    }
    versionCatalogs {
        create("libs") {
            from("org.metaborg.spoofax3:catalog:0.3.3")
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include(":org.metaborg.core")
include(":org.metaborg.core.test")
include(":org.metaborg.spoofax.core")
include(":org.metaborg.meta.core")
include(":org.metaborg.spoofax.meta.core")
include(":org.metaborg.spoofax.nativebundle")
