package org.metaborg.spoofax.generator.project;

import java.io.File;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.build.paths.SpoofaxProjectConstants;

public class MustacheProjectSettings {
    private final IResourceService resourceService;
    private final ProjectSettings settings;


    public MustacheProjectSettings(IResourceService resourceService, ProjectSettings settings) {
        this.resourceService = resourceService;
        this.settings = settings;
    }


    public String groupId() {
        return settings.groupId();
    }
    
    public boolean generateGroupId() {
        return !groupId().equals(SpoofaxProjectConstants.METABORG_GROUP_ID);
    }

    public String id() {
        return settings.id();
    }

    public String version() {
        return settings.version();
    }
    
    public boolean generateVersion() {
        return !version().equals(metaborgVersion());
    }

    public String eclipseVersion() {
        return settings.eclipseVersion();
    }

    public String name() {
        return settings.name();
    }

    public File getBaseDir() {
        return toFile(settings.location());
    }


    public String metaborgVersion() {
        final String metaborgVersion = settings.metaborgVersion();
        return metaborgVersion != null && !metaborgVersion.isEmpty() ? metaborgVersion
            : SpoofaxProjectConstants.METABORG_VERSION;
    }

    public String eclipseMetaborgVersion() {
        return settings.eclipseMetaborgVersion();
    }


    public Format format() {
        final Format format = settings.format();
        return format != null ? format : Format.ctree;
    }


    public String strategoName() {
        return settings.strategoName();
    }

    public String javaName() {
        return settings.javaName();
    }

    public String packageName() {
        return settings.packageName();
    }

    public String packagePath() {
        return settings.packagePath();
    }


    public @Nullable File getGeneratedSourceDirectory() {
        return toFile(settings.getGeneratedSourceDirectory());
    }

    public @Nullable File getOutputDirectory() {
        return toFile(settings.getOutputDirectory());
    }

    public @Nullable File getIconsDirectory() {
        return toFile(settings.getIconsDirectory());
    }

    public @Nullable File getLibDirectory() {
        return toFile(settings.getLibDirectory());
    }

    public @Nullable File getSyntaxDirectory() {
        return toFile(settings.getSyntaxDirectory());
    }

    public @Nullable File getEditorDirectory() {
        return toFile(settings.getEditorDirectory());
    }

    public @Nullable File getJavaDirectory() {
        return toFile(settings.getJavaDirectory());
    }

    public @Nullable File getJavaTransDirectory() {
        return toFile(settings.getJavaTransDirectory());
    }

    public @Nullable File getGeneratedSyntaxDirectory() {
        return toFile(settings.getGeneratedSyntaxDirectory());
    }

    public @Nullable File getTransDirectory() {
        return toFile(settings.getTransDirectory());
    }

    public @Nullable File getCacheDirectory() {
        return toFile(settings.getCacheDirectory());
    }


    private @Nullable File toFile(FileObject resource) {
        return resourceService.localPath(resource);
    }
}
