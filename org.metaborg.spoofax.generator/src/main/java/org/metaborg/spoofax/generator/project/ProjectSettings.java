package org.metaborg.spoofax.generator.project;

import static org.metaborg.spoofax.core.build.paths.SpoofaxProjectConstants.DIR_CACHE;
import static org.metaborg.spoofax.core.build.paths.SpoofaxProjectConstants.DIR_EDITOR;
import static org.metaborg.spoofax.core.build.paths.SpoofaxProjectConstants.DIR_ICONS;
import static org.metaborg.spoofax.core.build.paths.SpoofaxProjectConstants.DIR_INCLUDE;
import static org.metaborg.spoofax.core.build.paths.SpoofaxProjectConstants.DIR_JAVA;
import static org.metaborg.spoofax.core.build.paths.SpoofaxProjectConstants.DIR_JAVA_TRANS;
import static org.metaborg.spoofax.core.build.paths.SpoofaxProjectConstants.DIR_LIB;
import static org.metaborg.spoofax.core.build.paths.SpoofaxProjectConstants.DIR_SRCGEN;
import static org.metaborg.spoofax.core.build.paths.SpoofaxProjectConstants.DIR_SRCGEN_SYNTAX;
import static org.metaborg.spoofax.core.build.paths.SpoofaxProjectConstants.DIR_SYNTAX;
import static org.metaborg.spoofax.core.build.paths.SpoofaxProjectConstants.DIR_TRANS;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.SpoofaxRuntimeException;

public class ProjectSettings {
    private final String groupId;
    private final String id;
    private final String version;
    private final String name;
    private final FileObject location;


    public ProjectSettings(String groupId, String id, String version, String name, FileObject location)
        throws ProjectException {
        if(!NameUtil.isValidId(groupId)) {
            throw new ProjectException("Invalid group id: " + groupId);
        }
        if(!NameUtil.isValidId(id)) {
            throw new ProjectException("Invalid id: " + id);
        }
        if(!NameUtil.isValidName(name)) {
            throw new ProjectException("Invalid name: " + name);
        }

        this.groupId = groupId;
        this.id = id;
        this.version = version;
        this.name = name;
        this.location = location;
    }


    public String groupId() {
        return groupId;
    }

    public String id() {
        return id;
    }

    public String version() {
        return version;
    }

    public String eclipseVersion() {
        return version.replace("-SNAPSHOT", ".qualifier");
    }

    public String name() {
        return name;
    }

    public FileObject location() {
        return location;
    }


    private String metaborgVersion;

    public @Nullable String metaborgVersion() {
        return metaborgVersion;
    }

    public @Nullable String eclipseMetaborgVersion() {
        if(metaborgVersion == null) {
            return null;
        }
        return metaborgVersion().replace("-SNAPSHOT", ".qualifier");
    }

    public void setMetaborgVersion(String metaborgVersion) throws ProjectException {
        this.metaborgVersion = metaborgVersion;
    }


    private Format format = Format.ctree;

    public @Nullable Format format() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }


    public String strategoName() {
        return NameUtil.toJavaId(name().toLowerCase());
    }

    public String javaName() {
        return NameUtil.toJavaId(name());
    }

    public String packageName() {
        return NameUtil.toJavaId(id());
    }

    public String packagePath() {
        return packageName().replace('.', '/');
    }


    public FileObject getGeneratedSourceDirectory() {
        return resolve(DIR_SRCGEN);
    }

    public FileObject getOutputDirectory() {
        return resolve(DIR_INCLUDE);
    }

    public FileObject getIconsDirectory() {
        return resolve(DIR_ICONS);
    }

    public FileObject getLibDirectory() {
        return resolve(DIR_LIB);
    }

    public FileObject getSyntaxDirectory() {
        return resolve(DIR_SYNTAX);
    }

    public FileObject getEditorDirectory() {
        return resolve(DIR_EDITOR);
    }

    public FileObject getJavaDirectory() {
        return resolve(DIR_JAVA);
    }

    public FileObject getJavaTransDirectory() {
        return resolve(DIR_JAVA_TRANS);
    }

    public FileObject getGeneratedSyntaxDirectory() {
        return resolve(DIR_SRCGEN_SYNTAX);
    }

    public FileObject getTransDirectory() {
        return resolve(DIR_TRANS);
    }

    public FileObject getCacheDirectory() {
        return resolve(DIR_CACHE);
    }


    private FileObject resolve(String directory) {
        try {
            return location.resolveFile(directory);
        } catch(FileSystemException e) {
            throw new SpoofaxRuntimeException(e);
        }
    }
}
