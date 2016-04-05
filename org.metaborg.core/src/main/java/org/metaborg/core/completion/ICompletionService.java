package org.metaborg.core.completion;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.syntax.IParseUnit;

public interface ICompletionService<P extends IParseUnit> {
    Iterable<ICompletion> get(int offset, P result) throws MetaborgException;
}
