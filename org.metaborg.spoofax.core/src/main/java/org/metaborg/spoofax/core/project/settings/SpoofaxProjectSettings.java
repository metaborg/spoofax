package org.metaborg.spoofax.core.project.settings;

import static org.metaborg.spoofax.core.SpoofaxProjectConstants.*;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.project.NameUtil;
import org.metaborg.core.project.settings.IProjectSettings;

import com.google.common.collect.Lists;

public class SpoofaxProjectSettings {
    private final IProjectSettings settings;
    private final FileObject location;

    private Collection<String> pardonedLanguages = Lists.newLinkedList();
    private Format format = Format.ctree;
    private Collection<String> sdfArgs = Lists.newLinkedList();
    private @Nullable String externalDef;
    private Collection<String> strategoArgs = Lists.newLinkedList();
    private @Nullable String externalJar;
    private @Nullable String externalJarFlags;


    public SpoofaxProjectSettings(IProjectSettings settings, FileObject location) {
        this.settings = settings;
        this.location = location;
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
            throw new MetaborgRuntimeException(e);
        }
    }
}
