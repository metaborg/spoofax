package org.metaborg.spoofax.generator.project;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.NameUtil;
import org.metaborg.core.project.ProjectException;
import org.metaborg.spoofax.core.SpoofaxProjectConstants;
import org.metaborg.spoofax.core.project.Format;
import org.metaborg.spoofax.core.project.SpoofaxProjectSettings;

public class GeneratorProjectSettings {
    private final SpoofaxProjectSettings settings;


    public GeneratorProjectSettings(SpoofaxProjectSettings settings) {
        this.settings = settings;
    }


    private @Nullable String metaborgVersion;

    public void setMetaborgVersion(String metaborgVersion) throws ProjectException {
        if(metaborgVersion != null && !NameUtil.isValidVersion(metaborgVersion)) {
            throw new ProjectException("Invalid metaborg version: " + metaborgVersion);
        }
        this.metaborgVersion = metaborgVersion;
    }

    public String metaborgVersion() {
        return metaborgVersion != null && !metaborgVersion.isEmpty() ? metaborgVersion
            : SpoofaxProjectConstants.METABORG_VERSION;
    }

    public String eclipseMetaborgVersion() {
        return metaborgVersion().replace("-SNAPSHOT", ".qualifier");
    }


    public String groupId() {
        return settings.identifier().groupId;
    }

    public boolean generateGroupId() {
        return !groupId().equals(SpoofaxProjectConstants.METABORG_GROUP_ID);
    }

    public String id() {
        return settings.identifier().id;
    }

    public String version() {
        return settings.identifier().version.toString();
    }

    public boolean generateVersion() {
        return !version().equals(metaborgVersion());
    }

    public String eclipseVersion() {
        return version().replace("-SNAPSHOT", ".qualifier");
    }

    public String name() {
        return settings.name();
    }

    public FileObject location() {
        return settings.location();
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
}
