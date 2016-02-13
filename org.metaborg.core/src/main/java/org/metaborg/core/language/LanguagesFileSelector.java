package org.metaborg.core.language;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileType;

public class LanguagesFileSelector implements FileSelector {
    private final ILanguageIdentifierService languageIdentifierService;
    private final Iterable<ILanguageImpl> languages;


    public LanguagesFileSelector(ILanguageIdentifierService languageIdentifierService, Iterable<ILanguageImpl> languages) {
        this.languageIdentifierService = languageIdentifierService;
        this.languages = languages;
    }


    @Override public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
        final FileObject file = fileInfo.getFile();
        return FileType.FILE.equals(file.getType()) && languageIdentifierService.identify(file, languages) != null;
    }

    @Override public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
        return true;
    }
}
