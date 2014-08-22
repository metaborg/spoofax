package org.metaborg.spoofax.core.parser;

import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.io.ParseTableManager;

import com.google.inject.Inject;

public class ParseService implements IParseService {
    private final ITermFactoryService termFactoryService;
    private final ParseTableManager parseTableManager;


    @Inject public ParseService(ITermFactoryService termFactoryService) {
        this.termFactoryService = termFactoryService;

        final ITermFactory termFactory =
            this.termFactoryService.getGeneric().getFactoryWithStorageType(IStrategoTerm.MUTABLE);
        this.parseTableManager = new ParseTableManager(termFactory);
    }

    @Override public ParseTableManager parseTableManager() {
        return parseTableManager;
    }
}
