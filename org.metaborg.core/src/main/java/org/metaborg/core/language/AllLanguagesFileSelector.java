package org.metaborg.core.language;

import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;

public class AllLanguagesFileSelector implements FileSelector {
    private final ILanguageIdentifierService languageIdentifierService;


    public AllLanguagesFileSelector(ILanguageIdentifierService languageIdentifierService) {
        this.languageIdentifierService = languageIdentifierService;
    }


    @Override public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
        return languageIdentifierService.identify(fileInfo.getFile()) != null;
    }

    @Override public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
        return true;
    }
}
