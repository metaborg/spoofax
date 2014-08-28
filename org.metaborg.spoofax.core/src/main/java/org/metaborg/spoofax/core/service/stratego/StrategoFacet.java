package org.metaborg.spoofax.core.service.stratego;

import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguageFacet;

/**
 * Represents the Stratego runtime facet of a language.
 */
public class StrategoFacet implements ILanguageFacet {
    private final Set<FileObject> ctreeFiles;
    private final Set<FileObject> jarFiles;
    private final String analysisStrategy;
    private final String onSaveStrategy;


    /**
     * Creates a Stratego facet from Stratego provider files, the analysis strategy, and the on-save strategy.
     * 
     * @param ctreeFile
     *            Set of ctree provider files.
     * @param jarFiles
     *            Set of JAR provider files.
     * @param analysisStrategy
     *            Name of the analysis strategy, or null if none.
     * @param onSaveStrategy
     *            Name of the on-save strategy, or null if none.
     */
    public StrategoFacet(Set<FileObject> ctreeFile, Set<FileObject> jarFiles, @Nullable String analysisStrategy,
        @Nullable String onSaveStrategy) {
        this.ctreeFiles = ctreeFile;
        this.jarFiles = jarFiles;
        this.analysisStrategy = analysisStrategy;
        this.onSaveStrategy = onSaveStrategy;
    }


    /**
     * Returns the ctree provider files.
     * 
     * @return Iterable over the ctree provider files.
     */
    public Iterable<FileObject> ctreeFiles() {
        return ctreeFiles;
    }

    /**
     * Returns the JAR provider files.
     * 
     * @return Iterable over the JAR provider files.
     */
    public Iterable<FileObject> jarFiles() {
        return jarFiles;
    }

    /**
     * Returns the name of the analysis strategy.
     * 
     * @return Name of the analysis strategy, or null if none.
     */
    public @Nullable String analysisStrategy() {
        return analysisStrategy;
    }

    /**
     * Returns the name of the on-save strategy.
     * 
     * @return Name of the on-save strategy, or null if none.
     */
    public @Nullable String onSaveStrategy() {
        return onSaveStrategy;
    }
}
