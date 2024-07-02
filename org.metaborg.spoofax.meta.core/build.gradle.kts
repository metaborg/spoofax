plugins {
    id("org.metaborg.gradle.config.java-library")
}

fun compositeBuild(name: String) = "$group:$name:$version"
val spoofax2Version: String by ext
val pieVersion = "0.18.0" // HACK: override PIE version to make it binary compatible with this version.
dependencies {
    api(platform("org.metaborg:parent:$spoofax2Version"))

    api(project(":org.metaborg.core"))
    api(project(":org.metaborg.meta.core"))
    api(project(":org.metaborg.spoofax.core"))

    implementation(libs.spoofax2.metaborg.util)
    implementation(libs.spoofax2.util.vfs2)
    implementation(libs.spoofax2.jsglr.shared)
    implementation(libs.spoofax2.sdf2table)
    implementation(libs.spoofax2.sdf2parenthesize)
    implementation(libs.spoofax2.parsetable)
    implementation(libs.spoofax2.stratego.build)
    implementation(libs.spoofax2.stratego.build.spoofax2)
    implementation(libs.spoofax2.nabl2.solver)
    implementation(libs.spoofax2.statix.solver)

    implementation(project(":org.metaborg.spoofax.nativebundle"))
    implementation(libs.spoofax2.strategoxt)
    implementation(libs.spoofax2.makepermissive)

    implementation("build.pluto:pluto")
    implementation("build.pluto:build-java")
    implementation("org.metaborg:log.backend.slf4j:0.5.0")
    implementation("org.metaborg:pie.runtime:$pieVersion")
    implementation("org.metaborg:pie.taskdefs.guice:$pieVersion")
    api("com.google.inject:guice")
    implementation("com.github.spullara.mustache.java:compiler")
    implementation("org.apache.ant:ant:1.9.6")
    implementation("ant-contrib:ant-contrib:1.0b3")

    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("jakarta.inject:jakarta.inject-api")
}
