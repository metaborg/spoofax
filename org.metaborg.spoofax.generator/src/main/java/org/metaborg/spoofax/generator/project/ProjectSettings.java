package org.metaborg.spoofax.generator.project;

import java.io.File;
import static org.metaborg.spoofax.core.project.SpoofaxProjectConstants.*;

public class ProjectSettings {
 
    public enum Format {
        ctree,
        jar
    }

    private final String name;
    private final File basedir;

    public ProjectSettings(String name, File basedir) throws ProjectException {
        if ( !NameUtil.isValidName(name) ) {
            throw new ProjectException("Invalid name: "+name);
        }
        this.name = name;
        this.basedir = basedir;
    }

    public String name() {
        return name;
    }

    public File getBaseDir() {
        return basedir;
    }

    ////////////////////////////////////////////////////////////////

    private String id;

    public String id() {
        return id != null && !id.isEmpty() ?
                id : name().toLowerCase();
    }

    public void setId(String id) throws ProjectException {
        if ( !NameUtil.isValidId(id) ) {
            throw new ProjectException("Invalid id: "+id);
        }
        this.id = id;
    }

    private String version;

    public String version() {
        return version != null ? version : "1.0-SNAPSHOT";
    }

    public void setVersion(String version) {
        this.version = version;
    }

    ////////////////////////////////////////////////////////////////

    private Format format;

    public Format format() {
        return format != null ?
                format : Format.ctree;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    ////////////////////////////////////////////////////////////////

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

    ////////////////////////////////////////////////////////////////

    public File getGeneratedSourceDirectory() {
        return new File(basedir, DIR_SRCGEN);
    }

    public File getOutputDirectory() {
        return new File(basedir, DIR_INCLUDE);
    }

    public File getIconsDirectory() {
        return new File(basedir, "icons");
    }

    public File getLibDirectory() {
        return new File(basedir, DIR_LIB);
    }

    public File getSyntaxDirectory() {
        return new File(basedir, DIR_SYNTAX);
    }

    public File getEditorDirectory() {
        return new File(basedir, DIR_EDITOR);
    }

    public File getJavaDirectory() {
        return new File(basedir, DIR_JAVA);
    }

    public File getJavaTransDirectory() {
        return new File(basedir, DIR_JAVA_TRANS);
    }

    public File getGeneratedSyntaxDirectory() {
        return new File(basedir, DIR_SRCGEN_SYNTAX);
    }

    public File getTransDirectory() {
        return new File(basedir, DIR_TRANS);
    }

    public File getCacheDirectory() {
        return new File(basedir, DIR_CACHE);
    }

}
