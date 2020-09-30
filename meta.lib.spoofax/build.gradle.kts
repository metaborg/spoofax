plugins {
  id("org.metaborg.spoofax.gradle.langspec")
  `maven-publish`
}

// HACK: Set different group to prevent substitution of the baseline version to this project. I could not find another
// way to disable this substitution.
group = "org.metaborg.bootstraphack"
