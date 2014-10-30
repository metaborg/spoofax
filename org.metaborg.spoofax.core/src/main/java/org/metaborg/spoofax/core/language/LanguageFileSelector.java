package org.metaborg.spoofax.core.language;

import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;

public class LanguageFileSelector implements FileSelector {
    private final ILanguageIdentifierService languageIdentifierService;
    private final ILanguage language;


    public LanguageFileSelector(ILanguageIdentifierService languageIdentifierService, ILanguage language) {
        this.languageIdentifierService = languageIdentifierService;
        this.language = language;
    }


    @Override public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
        return languageIdentifierService.identify(fileInfo.getFile()).equals(language);
    }

    @Override public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
        return true;
    }
}
