// !! THIS FILE WAS GENERATED USING repoman !!
// Modify `repo.yaml` instead and use `repoman` to update this file
// See: https://github.com/metaborg/metaborg-gradle/

import org.metaborg.convention.Person
import org.metaborg.convention.MavenPublishConventionExtension

// Workaround for issue: https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("org.metaborg.convention.root-project")
    alias(libs.plugins.gitonium)
    alias(libs.plugins.spoofax.gradle.langspec) apply false
}

rootProjectConvention {
    // Add `publishAll` and `publish` tasks that delegate to the subprojects and included builds.
    registerPublishTasks.set(true)
}
allprojects {
    apply(plugin = "org.metaborg.gitonium")

    // Configure Gitonium before setting the version
    gitonium {
        mainBranch.set("master")
        tagPrefix.set("devenv-release/")
    }
    version = gitonium.version
    group = "org.metaborg.devenv"

    pluginManager.withPlugin("org.metaborg.convention.maven-publish") {
        extensions.configure(MavenPublishConventionExtension::class.java) {
            repoOwner.set("metaborg")
            repoName.set("spoofax")

            metadata {
                inceptionYear.set("2007")
                developers.set(listOf(
                    Person("Jeff Smits", email = null, id = "Apanatshka"),
                ))
                contributors.set(listOf(
                    Person("Gabriel Konat", email = null, id = "Gohla"),
                    Person("Lennart Kats", email = null, id = "lennartcl"),
                    Person("Hendrik van Antwerpen", email = null, id = "hendrikvanantwerpen"),
                    Person("Oskar van Rest", email = null, id = "oskar-van-rest"),
                    Person("Maartje de Jonge", email = null, id = "maartje"),
                    Person("Eduardo Souza", email = null, id = "udesou"),
                    Person("Vlad Vergu", email = null, id = "vvergu"),
                    Person("Daniel A. A. Pelsmaeker", email = null, id = "Virtlink"),
                    Person("Jasper Denkers", email = null, id = "jasperdenkers"),
                ))
            }
        }
    }
}
