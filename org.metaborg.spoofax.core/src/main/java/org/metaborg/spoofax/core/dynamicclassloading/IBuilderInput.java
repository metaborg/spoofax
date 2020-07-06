package org.metaborg.spoofax.core.dynamicclassloading;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;

public interface IBuilderInput<Term, AST> {
    Term getSelection();

    Term getPosition();

    AST getAst();

    @Nullable FileObject getResource();

    @Nullable FileObject getLocation();
}