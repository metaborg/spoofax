plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

val pieVersion = "0.18.0" // HACK: override PIE version to make it binary compatible with this version.
dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    api(project(":org.metaborg.core"))
    api(project(":org.metaborg.meta.core"))
    api(project(":org.metaborg.spoofax.core"))

    implementation(libs.metaborg.util)
    implementation(libs.util.vfs2)
    implementation(libs.jsglr.shared)
    implementation(libs.sdf2table)
    implementation(libs.sdf2parenthesize)
    implementation(libs.parsetable)
    implementation(libs.stratego.build)
    implementation(libs.stratego.build.spoofax2)
    implementation(libs.nabl2.solver)
    implementation(libs.statix.solver)

    implementation(project(":org.metaborg.spoofax.nativebundle"))
    implementation(libs.strategoxt.minjar)
    implementation(libs.makepermissive)

    implementation(libs.pluto)
    implementation(libs.pluto.build.java)
    implementation(libs.metaborg.log.backend.slf4j)
    implementation(libs.metaborg.pie.runtime) { version { require(pieVersion) } }
    implementation(libs.metaborg.pie.taskdefs.guice) { version { require(pieVersion) } }
    api(libs.guice)
    implementation(libs.mustache.compiler)
    implementation(libs.ant)
    implementation(libs.ant.contrib)

    implementation(libs.jakarta.annotation)
    implementation(libs.jakarta.inject)
}
