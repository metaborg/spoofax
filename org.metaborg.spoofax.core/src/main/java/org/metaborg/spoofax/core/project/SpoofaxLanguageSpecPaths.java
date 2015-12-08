package org.metaborg.spoofax.core.project;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.spoofax.core.project.configuration.ISpoofaxLanguageSpecConfig;

import static org.metaborg.spoofax.core.SpoofaxConstants.*;

public class SpoofaxLanguageSpecPaths implements ISpoofaxLanguageSpecPaths {

    private final FileObject root;
    private final ISpoofaxLanguageSpecConfig config;

    public SpoofaxLanguageSpecPaths(FileObject rootFolder, ISpoofaxLanguageSpecConfig config) {
        this.root = rootFolder;
        this.config = config;
    }

    @Override
    public FileObject rootFolder() {
        return this.root;
    }

    @Override
    public FileObject outputFolder() {
        return resolve(DIR_SRCGEN);
    }

    @Override
    public FileObject configFile() {
        return resolve(FILE_CONFIG);
    }

    @Override
    public FileObject generatedSourceDirectory() {
        return resolve(DIR_SRCGEN);
    }

    @Override
    public FileObject iconsDirectory() {
        return resolve(DIR_ICONS);
    }

    @Override
    public FileObject libDirectory() {
        return resolve(DIR_LIB);
    }

    @Override
    public FileObject syntaxDirectory() {
        return resolve(DIR_SYNTAX);
    }

    @Override
    public FileObject editorDirectory() {
        return resolve(DIR_EDITOR);
    }

    @Override
    public FileObject javaDirectory() {
        return resolve(DIR_JAVA);
    }

    @Override
    public FileObject javaTransDirectory() {
        return resolve(DIR_JAVA_TRANS);
    }

    @Override
    public FileObject generatedSyntaxDirectory() {
        return resolve(DIR_SRCGEN_SYNTAX);
    }

    @Override
    public FileObject transDirectory() {
        return resolve(DIR_TRANS);
    }

    @Override
    public FileObject cacheDirectory() {
        return resolve(DIR_CACHE);
    }

    @Override
    public FileObject mainEsvFile() {
        return resolve(DIR_EDITOR + "/" + this.config.name() + ".main.esv");
    }

    private FileObject resolve(final String folder) {
        try {
            return this.root.resolveFile(folder);
        } catch(FileSystemException e) {
            throw new MetaborgRuntimeException(e);
        }
    }
}
