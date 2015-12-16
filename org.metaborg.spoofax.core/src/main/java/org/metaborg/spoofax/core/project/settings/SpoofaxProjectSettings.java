package org.metaborg.spoofax.core.project.settings;

import static org.metaborg.spoofax.core.SpoofaxConstants.*;

import java.io.Serializable;
import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.project.NameUtil;
import org.metaborg.core.project.settings.IProjectSettings;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.util.file.FileUtils;

import com.google.common.collect.Lists;

@Deprecated
public class SpoofaxProjectSettings implements Serializable {
    private static final long serialVersionUID = 7439146986768086591L;
    
    private final IProjectSettings settings;
    private final String locationPath;
    private transient FileObject location;

    private Collection<String> pardonedLanguages = Lists.newLinkedList();
    private Format format = Format.ctree;
    private Collection<String> sdfArgs = Lists.newLinkedList();
    private @Nullable String externalDef;
    private Collection<String> strategoArgs = Lists.newLinkedList();
    private @Nullable String externalJar;
    private @Nullable String externalJarFlags;


    public SpoofaxProjectSettings(IProjectSettings settings, FileObject location) {
        this.settings = settings;
        this.locationPath = FileUtils.toPath(location);
        this.location = location;
    }
    
    
    public void initAfterDeserialization(IResourceService resourceService) {
        location = resourceService.resolve(locationPath);
    }


    public IProjectSettings settings() {
        return settings;
    }

    public FileObject location() {
        return location;
    }


    public Iterable<String> pardonedLanguages() {
        return pardonedLanguages;
    }

    public void setPardonedLanguages(Collection<String> pardonedLanguages) {
        this.pardonedLanguages = pardonedLanguages;
    }


    public Format format() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }


    public Iterable<String> sdfArgs() {
        return sdfArgs;
    }

    public void setSdfArgs(Collection<String> sdfArgs) {
        this.sdfArgs = sdfArgs;
    }

    public @Nullable String externalDef() {
        return externalDef;
    }

    public void setExternalDef(String externalDef) {
        this.externalDef = externalDef;
    }


    public Iterable<String> strategoArgs() {
        return strategoArgs;
    }

    public void setStrategoArgs(Collection<String> strategoArgs) {
        this.strategoArgs = strategoArgs;
    }

    public @Nullable String externalJar() {
        return externalJar;
    }

    public void setExternalJar(String externalJar) {
        this.externalJar = externalJar;
    }

    public @Nullable String externalJarFlags() {
        return externalJarFlags;
    }

    public void setExternalJarFlags(String externalJarFlags) {
        this.externalJarFlags = externalJarFlags;
    }

    
    public String sdfName() {
        return settings.name();
    }
    
    public String metaSdfName() {
        return sdfName() + "-Statego";
    }
    
    public String esvName() {
        return settings.name();
    }

    public String strategoName() {
        return NameUtil.toJavaId(settings.name().toLowerCase());
    }

    public String javaName() {
        return NameUtil.toJavaId(settings.name());
    }

    public String packageName() {
        return NameUtil.toJavaId(settings.identifier().id);
    }

    public String packagePath() {
        return packageName().replace('.', '/');
    }

    public String strategiesPackageName() {
        return packageName() + ".strategies";
    }
    
    public String packageStrategiesPath() {
        return strategiesPackageName().replace('.', '/');
    }
    

    public FileObject getGenSourceDirectory() {
        return resolve(DIR_SRCGEN);
    }

    public FileObject getIncludeDirectory() {
        return resolve(DIR_INCLUDE);
    }
    
    public FileObject getOutputDirectory() {
        return resolve(DIR_OUTPUT);
    }
    
    public FileObject getOutputClassesDirectory() {
        return resolve(DIR_CLASSES);
    }

    public FileObject getBuildDirectory() {
        return resolve(DIR_BUILD);
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

    public FileObject getGenSyntaxDirectory() {
        return resolve(DIR_SRCGEN_SYNTAX);
    }

    public FileObject getTransDirectory() {
        return resolve(DIR_TRANS);
    }

    public FileObject getCacheDirectory() {
        return resolve(DIR_CACHE);
    }

    public FileObject getMainESVFile() {
        return resolve(DIR_EDITOR + "/" + settings.name() + ".main.esv");
    }
    
    public FileObject getSdfMainFile(String sdfName) {
        return resolve(getGenSyntaxDirectory(), sdfName + ".sdf");
    }
    
    public FileObject getSdfCompiledDefFile(String sdfName) {
        return resolve(getIncludeDirectory(), sdfName + ".def");
    }
    
    public FileObject getSdfCompiledPermissiveDefFile(String sdfName) {
        return resolve(getIncludeDirectory(), sdfName + "-Permissive.def");
    }
    
    public FileObject getSdfCompiledTableFile(String sdfName) {
        return resolve(getIncludeDirectory(), sdfName + ".tbl");
    }
    
    
    public FileObject getRtgFile(String sdfName) {
        return resolve(getIncludeDirectory(), sdfName + ".rtg");
    }
    
    
    public FileObject getStrMainFile() {
        return resolve(getTransDirectory(), strategoName() + ".str");
    }
    
    public FileObject getStrJavaDirectory() {
        return resolve(DIR_STR_JAVA);
    }
    
    public FileObject getStrJavaPackageDirectory() {
        return resolve(getStrJavaDirectory(), packagePath());
    }
    
    public FileObject getStrJavaStrategiesDirectory() {
        return resolve(getStrJavaPackageDirectory(), "strategies");
    }
    
    public FileObject getStrJavaStrategiesMainFile() {
        return resolve(getStrJavaStrategiesDirectory(), "Main.java");
    }

    public FileObject getStrJavaTransDirectory() {
        return resolve(DIR_STR_JAVA_TRANS);
    }
    
    public FileObject getStrJavaMainFile() {
        return resolve(getStrJavaTransDirectory(), "Main.java");
    }
    
    public FileObject getStrCompiledJarFile() {
        return resolve(getIncludeDirectory(), strategoName() + ".jar");
    }
    
    public FileObject getStrCompiledJavaJarFile() {
        return resolve(getIncludeDirectory(), strategoName() + "-java.jar");
    }
    
    public FileObject getStrCompiledCtreeFile() {
        return resolve(getIncludeDirectory(), strategoName() + ".ctree");
    }
    
    public FileObject getStrCompiledParenthesizerFile(String sdfName) {
        return resolve(getIncludeDirectory(), sdfName + "-parenthesize.str");
    }
    
    public FileObject getStrCompiledSigFile(String sdfName) {
        return resolve(getIncludeDirectory(), sdfName + ".str");
    }
    

    public FileObject getPpFile(String sdfName) {
        return resolve(getSyntaxDirectory(), sdfName + ".pp");
    }
    
    public FileObject getPpAfCompiledFile(String sdfName) {
        return resolve(getIncludeDirectory(), sdfName + ".pp.af");
    }
    
    public FileObject getGenPpCompiledFile(String sdfName) {
        return resolve(getIncludeDirectory(), sdfName + ".generated.pp");
    }
    
    public FileObject getGenPpAfCompiledFile(String sdfName) {
        return resolve(getIncludeDirectory(), sdfName + ".generated.pp.af");
    }
    
    
    public FileObject getPackedEsv() {
        return resolve(getIncludeDirectory(), esvName() + ".packed.esv");
    }


    private FileObject resolve(String name) {
        try {
            return location.resolveFile(name);
        } catch(FileSystemException e) {
            throw new MetaborgRuntimeException(e);
        }
    }

    private FileObject resolve(FileObject file, String name) {
        try {
            return file.resolveFile(name);
        } catch(FileSystemException e) {
            throw new MetaborgRuntimeException(e);
        }
    }
}
