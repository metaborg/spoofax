package org.metaborg.spoofax.core.service.stratego;

import java.util.Set;

import org.apache.commons.vfs2.FileName;
import org.metaborg.spoofax.core.language.ILanguageFacet;

public class StrategoFacet implements ILanguageFacet {
    private final Set<FileName> ctreeFiles;
    private final Set<FileName> jarFiles;
    private final String analysisStrategy;
    private final String onSaveStrategy;


    public StrategoFacet(Set<FileName> ctreeFile, Set<FileName> jarFiles, String analysisStrategy, String onSaveStrategy) {
        this.ctreeFiles = ctreeFile;
        this.jarFiles = jarFiles;
        this.analysisStrategy = analysisStrategy;
        this.onSaveStrategy = onSaveStrategy;
    }


    public Set<FileName> ctreeFiles() {
        return ctreeFiles;
    }

    public Set<FileName> jarFiles() {
        return jarFiles;
    }

    public String analysisStrategy() {
        return analysisStrategy;
    }

    public String onSaveStrategy() {
        return onSaveStrategy;
    }
}
