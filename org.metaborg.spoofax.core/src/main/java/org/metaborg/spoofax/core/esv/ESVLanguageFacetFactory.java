package org.metaborg.spoofax.core.esv;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageFacetFactory;

public class ESVLanguageFacetFactory implements ILanguageFacetFactory {
    @Override public void create(ILanguage language) throws FileSystemException {
        final FileSelector selector = new FileSelector() {
            private boolean done = false;

            @Override public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
                return !done;
            }

            @Override public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
                boolean match = fileInfo.getFile().getName().getBaseName().contains("packed.esv");
                done = done || match;
                return match;
            }
        };

        final FileObject[] files = language.location().findFiles(selector);
        
    }
}
