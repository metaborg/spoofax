package org.metaborg.spoofax.core.stratego;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.IFacet;

/**
 * Represents the Stratego runtime facet of a language.
 */
public class StrategoRuntimeFacet implements IFacet {
    public final Iterable<FileObject> ctreeFiles;
    public final Iterable<FileObject> jarFiles;


    public StrategoRuntimeFacet(Iterable<FileObject> ctreeFiles, Iterable<FileObject> jarFiles) {
        this.ctreeFiles = ctreeFiles;
        this.jarFiles = jarFiles;
    }
}
