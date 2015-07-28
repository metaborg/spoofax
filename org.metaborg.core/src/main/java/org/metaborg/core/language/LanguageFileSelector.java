package org.metaborg.core.language;

import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileType;

public class LanguageFileSelector implements FileSelector {
    private final ILanguageIdentifierService languageIdentifierService;
    private final ILanguage language;


    public LanguageFileSelector(ILanguageIdentifierService languageIdentifierService, ILanguage language) {
        this.languageIdentifierService = languageIdentifierService;
        this.language = language;
    }


    @Override public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
        return FileType.FILE.equals(fileInfo.getFile().getType()) &&
                languageIdentifierService.identify(fileInfo.getFile(), language);
    }

    @Override public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
        return true;
    }
}
