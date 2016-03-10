package org.metaborg.spoofax.core.completion;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.completion.ICompletion;
import org.metaborg.core.completion.ICompletionService;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.util.iterators.Iterables2;

public class JSGLRCompletionService implements ICompletionService<ISpoofaxParseUnit> {
    @Override public Iterable<ICompletion> get(int position, ISpoofaxParseUnit parseResult) throws MetaborgException {
        return Iterables2.<ICompletion>empty();
    }
}
