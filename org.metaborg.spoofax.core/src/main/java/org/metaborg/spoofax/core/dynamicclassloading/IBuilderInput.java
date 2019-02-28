package org.metaborg.spoofax.core.dynamicclassloading;

import org.apache.commons.vfs2.FileObject;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;

public interface IBuilderInput extends IStrategoTuple {
    IStrategoTerm getSelection();

    IStrategoTerm getPosition();

    IStrategoTerm getAst();

    String getResource();

    FileObject getLocation();
}