package org.metaborg.spoofax.core.syntax;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface IParseTableTermProvider {
    FileObject resource();
    IStrategoTerm parseTableTerm() throws IOException;
}
