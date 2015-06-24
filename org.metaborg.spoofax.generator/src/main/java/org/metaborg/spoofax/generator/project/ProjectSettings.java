package org.metaborg.spoofax.generator.project;

import java.io.File;

import static org.metaborg.spoofax.core.build.paths.SpoofaxProjectConstants.*;

public class ProjectSettings {
    public enum Format {
        ctree, jar
    }


    private final String name;
    private final File basedir;


    public ProjectSettings(String name, File basedir) throws ProjectException {
        if(!NameUtil.isValidName(name)) {
            throw new ProjectException("Invalid name: " + name);
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

    // ////////////////////////////////////////////////////////////

    private String groupId;

    public String groupId() {
        // Return null when group id equals org.metaborg, because group id is a duplicate of the parent group id.
        if(groupId == null || groupId.isEmpty() || groupId.equals("org.metaborg")) {
            return null;
        }
        return groupId;
    }

    public void setGroupId(String groupId) throws ProjectException {
        if(!NameUtil.isValidId(groupId)) {
            throw new ProjectException("Invalid group id: " + groupId);
        }
        this.groupId = groupId;
    }

    // ////////////////////////////////////////////////////////////

    private String id;

    public String id() {
        return id != null && !id.isEmpty() ? id : name().toLowerCase();
    }

    public void setId(String id) throws ProjectException {
        if(!NameUtil.isValidId(id)) {
            throw new ProjectException("Invalid id: " + id);
        }
        this.id = id;
    }

    // ////////////////////////////////////////////////////////////

    private String version;

    public String version() {
        // Return null when version equals MetaBorg version, because version is a duplicate of the parent version.
        if(version == null || version.isEmpty() || version.equals(metaborgVersion)) {
            return null;
        }
        return version;
    }

    public String mavenVersion() {
        return version != null ? version : "";
    }    
    
    public String eclipseVersion() {
        if(version == null) {
            return null;
        }
        return version.replace("-SNAPSHOT", ".qualifier");
    }

    public void setVersion(String version) throws ProjectException {
        this.version = version;
    }

    // ////////////////////////////////////////////////////////////

    private String metaborgVersion;

    public String metaborgVersion() {
        return metaborgVersion != null && !metaborgVersion.isEmpty() ? metaborgVersion : "1.5.0-SNAPSHOT";
    }

    public String eclipseMetaborgVersion() {
        return metaborgVersion().replace("-SNAPSHOT", ".qualifier");
    }

    public void setMetaborgVersion(String metaborgVersion) throws ProjectException {
        this.metaborgVersion = metaborgVersion;
    }

    // //////////////////////////////////////////////////////////////

    private Format format;

    public Format format() {
        return format != null ? format : Format.ctree;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    // //////////////////////////////////////////////////////////////

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

    // //////////////////////////////////////////////////////////////

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
