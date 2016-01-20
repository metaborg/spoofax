package org.metaborg.spoofax.core.project.settings;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.LanguageVersion;
import org.metaborg.core.project.settings.IProjectSettings;
import org.metaborg.core.project.settings.ProjectSettings;
import org.metaborg.spoofax.core.project.SpoofaxMavenConstants;

import com.google.common.collect.Lists;

@SuppressWarnings("deprecation")
@Deprecated
public class MavenProjectSettingsReader {
    public static @Nullable SpoofaxProjectSettings spoofaxSettings(FileObject location, MavenProject project) {
        final Plugin plugin = project.getPlugin(SpoofaxMavenConstants.QUAL_PLUGIN_NAME);
        if(plugin == null) {
            return null;
        }
        final Xpp3Dom dom = (Xpp3Dom) plugin.getConfiguration();
        if(dom == null) {
            return null;
        }

        final Collection<LanguageIdentifier> compileDeps = Lists.newLinkedList();
        final Collection<LanguageIdentifier> runtimeDeps = Lists.newLinkedList();

        for(Dependency dependency : project.getModel().getDependencies()) {
            if(SpoofaxMavenConstants.PACKAGING_TYPE.equalsIgnoreCase(dependency.getType())) {
                final LanguageVersion version = LanguageVersion.parse(dependency.getVersion());
                final LanguageIdentifier identifier =
                    new LanguageIdentifier(dependency.getGroupId(), dependency.getArtifactId(), version);
                compileDeps.add(identifier);
                if(dependency.getScope() != "provided") {
                    runtimeDeps.add(identifier);
                }
            }
        }

        // LEGACY: add plugin artifacts for supporting older languages
        for(Dependency dependency : plugin.getDependencies()) {
            if(SpoofaxMavenConstants.PACKAGING_TYPE.equalsIgnoreCase(dependency.getType())) {
                final LanguageVersion version = LanguageVersion.parse(dependency.getVersion());
                final LanguageIdentifier identifier =
                    new LanguageIdentifier(dependency.getGroupId(), dependency.getArtifactId(), version);
                compileDeps.add(identifier);
            }
        }

        final Collection<LanguageContributionIdentifier> langContribs = Lists.newLinkedList();
        final Xpp3Dom langContribsNode = dom.getChild("languageContributions");
        if(langContribsNode != null) {
            for(Xpp3Dom langContribNode : langContribsNode.getChildren()) {
                final Xpp3Dom groupIdNode = langContribNode.getChild("groupId");
                final Xpp3Dom artifactIdNode = langContribNode.getChild("artifactId");
                final Xpp3Dom versionNode = langContribNode.getChild("version");
                final Xpp3Dom nameNode = langContribNode.getChild("name");

                if(groupIdNode != null && artifactIdNode != null && versionNode != null && nameNode != null) {
                    final LanguageVersion version = LanguageVersion.parse(versionNode.getValue());
                    final LanguageIdentifier identifier =
                        new LanguageIdentifier(groupIdNode.getValue(), artifactIdNode.getValue(), version);
                    final LanguageContributionIdentifier contribId =
                        new LanguageContributionIdentifier(identifier, nameNode.getValue());
                    langContribs.add(contribId);
                }
            }
        }

        final Collection<String> pardonedLangs = Lists.newLinkedList();
        final Xpp3Dom pardonedLangsNode = dom.getChild("pardonedLanguages");
        if(pardonedLangsNode != null) {
            for(Xpp3Dom pardonedLangNode : pardonedLangsNode.getChildren()) {
                pardonedLangs.add(pardonedLangNode.getValue());
            }
        }

        final Format format;
        final Xpp3Dom formatNode = dom.getChild("format");
        if(formatNode != null) {
            switch(formatNode.getValue()) {
                case "jar":
                    format = Format.jar;
                    break;
                case "ctree":
                    format = Format.ctree;
                    break;
                default:
                    format = Format.ctree;
                    break;
            }
        } else {
            format = Format.ctree;
        }

        final Collection<String> sdfArgs = Lists.newLinkedList();
        final Xpp3Dom sdfArgsNode = dom.getChild("sdfArgs");
        if(sdfArgsNode != null) {
            for(Xpp3Dom sdfArgNode : sdfArgsNode.getChildren()) {
                sdfArgs.add(sdfArgNode.getValue());
            }
        }

        final String externalDef;
        final Xpp3Dom externalDefNode = dom.getChild("externalDef");
        if(externalDefNode != null) {
            externalDef = externalDefNode.getValue();
        } else {
            externalDef = null;
        }

        final Collection<String> strategoArgs = Lists.newLinkedList();
        final Xpp3Dom strategoArgsNode = dom.getChild("strategoArgs");
        if(strategoArgsNode != null) {
            for(Xpp3Dom strategoArgNode : strategoArgsNode.getChildren()) {
                strategoArgs.add(strategoArgNode.getValue());
            }
        }

        final String externalJar;
        final Xpp3Dom externalJarNode = dom.getChild("externalJar");
        if(externalJarNode != null) {
            externalJar = externalJarNode.getValue();
        } else {
            externalJar = null;
        }

        final String externalJarFlags;
        final Xpp3Dom externalJarFlagsNode = dom.getChild("externalJarFlags");
        if(externalJarFlagsNode != null) {
            externalJarFlags = externalJarFlagsNode.getValue();
        } else {
            externalJarFlags = null;
        }

        final LanguageVersion version = LanguageVersion.parse(project.getVersion());
        final LanguageIdentifier identifier =
            new LanguageIdentifier(project.getGroupId(), project.getArtifactId(), version);
        final IProjectSettings settings =
            new ProjectSettings(identifier, project.getName(), compileDeps, runtimeDeps, langContribs);
        final SpoofaxProjectSettings spoofaxSettings = new SpoofaxProjectSettings(settings, location);
        spoofaxSettings.setPardonedLanguages(pardonedLangs);
        spoofaxSettings.setFormat(format);
        spoofaxSettings.setSdfArgs(sdfArgs);
        spoofaxSettings.setExternalDef(externalDef);
        spoofaxSettings.setStrategoArgs(strategoArgs);
        spoofaxSettings.setExternalJar(externalJar);
        spoofaxSettings.setExternalJarFlags(externalJarFlags);

        return spoofaxSettings;
    }
}
