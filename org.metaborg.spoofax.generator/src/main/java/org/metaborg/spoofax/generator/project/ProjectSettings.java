package org.metaborg.spoofax.generator.project;

import java.io.File;

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
        return new File(basedir, "src-gen");
    }

    public File getOutputDirectory() {
        return new File(basedir, "include");
    }

    public File getIconsDirectory() {
        return new File(basedir, "icons");
    }

    public File getLibDirectory() {
        return new File(basedir, "lib");
    }

    public File getSyntaxDirectory() {
        return new File(basedir, "syntax");
    }

    public File getEditorDirectory() {
        return new File(basedir, "editor");
    }

    public File getJavaDirectory() {
        return new File(getEditorDirectory(), "java");
    }

    public File getJavaTransDirectory() {
        return new File(getJavaDirectory(), "trans");
    }

    public File getGeneratedSyntaxDirectory() {
        return new File(getGeneratedSourceDirectory(), "syntax");
    }

    public File getTransDirectory() {
        return new File(basedir, "trans");
    }

    public File getCacheDirectory() {
        return new File(basedir, ".cache");
    }

}
