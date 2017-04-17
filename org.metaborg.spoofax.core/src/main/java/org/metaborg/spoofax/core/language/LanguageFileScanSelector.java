package org.metaborg.spoofax.core.language;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgConstants;

public class LanguageFileScanSelector implements FileSelector {
    @Override public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
        final FileObject file = fileInfo.getFile();
        if(isLanguageSpecDirectory(file)) {
            return true;
        }
        return file.getName().getExtension().equals("spoofax-language");
    }

    @Override public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
        final FileObject file = fileInfo.getFile();
        // Do not traverse directory if the directory is a language specification with a component config file. This
        // directory will be selected instead.
        return !isLanguageSpecDirectory(file);
    }

    private boolean isLanguageSpecDirectory(FileObject file) throws FileSystemException {
        final FileObject configFile = file.resolveFile(MetaborgConstants.LOC_COMPONENT_CONFIG);
        return configFile.exists();
    }
}