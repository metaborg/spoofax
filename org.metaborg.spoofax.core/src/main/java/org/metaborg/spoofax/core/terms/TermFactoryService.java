package org.metaborg.spoofax.core.terms;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.build.CommonPaths;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.typesmart.TypesmartTermFactory;

public class TermFactoryService implements ITermFactoryService {
    private static final ILogger typesmartLogger = LoggerUtils.logger("Typesmart");

    private final ITermFactory genericFactory = new ImploderOriginTermFactory(new TermFactory());

    @Override public ITermFactory get(ILanguageImpl impl) {
        // TODO find typesmart contexts and merge them
        return genericFactory;
    }

    @Override public ITermFactory get(ILanguageComponent component) {
        if(component.config().typesmart()) {
            FileObject typesmartFile = new CommonPaths(component.location()).strTypesmartMergedFile();
            return new TypesmartTermFactory(genericFactory, typesmartLogger, typesmartFile);
        } else {
            return genericFactory;
        }
    }

    @Override public ITermFactory getGeneric() {
        return genericFactory;
    }
}
