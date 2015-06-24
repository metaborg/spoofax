package org.metaborg.spoofax.meta.core;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.metaborg.spoofax.core.build.dependency.SpoofaxMavenConstants;
import org.metaborg.spoofax.core.project.IProject;
import org.metaborg.spoofax.generator.project.ProjectException;
import org.metaborg.spoofax.generator.project.ProjectSettings;
import org.metaborg.spoofax.generator.project.ProjectSettings.Format;

import com.google.common.collect.Lists;

public class MetaBuildInput {
    public final IProject project;
    public final Collection<String> pardonedLanguages;
    public final ProjectSettings projectSettings;
    public final Collection<String> sdfArgs;
    public final Collection<String> strategoArgs;
    public final @Nullable String externalDef;
    public final @Nullable String externalJar;
    public final @Nullable String externalJarFlags;


    public MetaBuildInput(IProject project, Collection<String> pardonedLanguages, ProjectSettings projectSettings,
        Collection<String> sdfArgs, Collection<String> strategoArgs, @Nullable String externalDef,
        @Nullable String externalJar, @Nullable String externalJarFlags) {
        this.project = project;
        this.pardonedLanguages = pardonedLanguages;
        this.projectSettings = projectSettings;
        this.sdfArgs = sdfArgs;
        this.strategoArgs = strategoArgs;
        this.externalDef = externalDef;
        this.externalJar = externalJar;
        this.externalJarFlags = externalJarFlags;
    }

    public static MetaBuildInput fromMavenProject(IProject project, MavenProject mavenProject) throws ProjectException {
        final Plugin plugin = mavenProject.getPlugin(SpoofaxMavenConstants.QUAL_PLUGIN_NAME);
        final Xpp3Dom dom = (Xpp3Dom) plugin.getConfiguration();

        final Collection<String> pardonedLanguages = Lists.newLinkedList();
        final Xpp3Dom pardonedLanguagesNode = dom.getChild("pardonedLanguages");
        if(pardonedLanguagesNode != null) {
            for(Xpp3Dom pardonedLanguageNode : pardonedLanguagesNode.getChildren()) {
                pardonedLanguages.add(pardonedLanguageNode.getValue());
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
                    format = Format.jar;
                    break;
            }
        } else {
            format = Format.jar;
        }

        final Collection<String> sdfArgs = Lists.newLinkedList();
        final Xpp3Dom sdfArgsNode = dom.getChild("sdfArgs");
        if(sdfArgsNode != null) {
            for(Xpp3Dom sdfArgNode : sdfArgsNode.getChildren()) {
                sdfArgs.add(sdfArgNode.getValue());
            }
        }

        final Collection<String> strategoArgs = Lists.newLinkedList();
        final Xpp3Dom strategoArgsNode = dom.getChild("strategoArgs");
        if(strategoArgsNode != null) {
            for(Xpp3Dom strategoArgNode : strategoArgsNode.getChildren()) {
                strategoArgs.add(strategoArgNode.getValue());
            }
        }

        final String externalDef;
        final Xpp3Dom externalDefNode = dom.getChild("externalDef");
        if(externalDefNode != null) {
            externalDef = externalDefNode.getValue();
        } else {
            externalDef = null;
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

        final ProjectSettings projectSettings = new ProjectSettings(mavenProject.getName(), mavenProject.getBasedir());
        projectSettings.setGroupId(mavenProject.getGroupId());
        projectSettings.setId(mavenProject.getArtifactId());
        projectSettings.setVersion(mavenProject.getVersion());
        projectSettings.setFormat(format);

        return new MetaBuildInput(project, pardonedLanguages, projectSettings, sdfArgs, strategoArgs, externalDef,
            externalJar, externalJarFlags);
    }
}