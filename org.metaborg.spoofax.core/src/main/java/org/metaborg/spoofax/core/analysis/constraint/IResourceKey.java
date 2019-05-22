package org.metaborg.spoofax.core.analysis.constraint;

import java.io.Serializable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import mb.nabl2.terms.ITerm;

public interface IResourceKey extends Serializable {

    ITerm toTerm();

    IStrategoTerm toStrategoTerm(ITermFactory termFactory);

    FileObject toFileObject(FileObject location) throws FileSystemException;

}