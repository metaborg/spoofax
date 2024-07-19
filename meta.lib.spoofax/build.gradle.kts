plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.devenv.spoofax.gradle.langspec")
}

spoofaxLanguageSpecification {
    addCompileDependenciesFromMetaborgYaml.set(false)
    addSourceDependenciesFromMetaborgYaml.set(false)

    // We add the dependency manually and don't change the repositories
    // Eventually, this functionality should be removed from spoofax.gradle
    addSpoofaxCoreDependency.set(false)
    addSpoofaxRepository.set(false)
}

dependencies {
    compileLanguage(libs.spoofax2.esv.lang)         // Bootstrap using Spoofax 2 artifact
    compileLanguage(libs.spoofax2.stratego.lang)    // Bootstrap using Spoofax 2 artifact

    compileOnly(libs.spoofax.core)
}
