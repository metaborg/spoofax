package org.metaborg.spoofax.core.dynamicclassloading;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;

public interface IBuilderInput<Term, AST> {
    Term getSelection();

    Term getPosition();

    AST getAst();

    @Nullable FileObject getResource();

    @Nullable FileObject getLocation();
}