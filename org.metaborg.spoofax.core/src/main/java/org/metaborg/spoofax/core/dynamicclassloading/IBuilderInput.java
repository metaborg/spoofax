package org.metaborg.spoofax.core.dynamicclassloading;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;

public interface IBuilderInput extends IStrategoTuple {
    IStrategoTerm getSelection();

    IStrategoTerm getPosition();

    IStrategoTerm getAst();

    @Nullable FileObject getResource();

    @Nullable FileObject getLocation();
}