package org.metaborg.spoofax.core.terms;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.build.CommonPaths;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.typesmart.TypesmartContext;
import org.spoofax.terms.typesmart.TypesmartTermFactory;

public class TermFactoryService implements ITermFactoryService {
    private final ITermFactory genericFactory = new ImploderOriginTermFactory(new TermFactory());


    @Override public ITermFactory get(ILanguageImpl impl) {
        return genericFactory;
    }

    @Override public ITermFactory get(ILanguageComponent component) {
        FileObject typesmartFile = new CommonPaths(component.location()).strTypesmartFile();
        try {
            if(typesmartFile.exists()) {
                TypesmartContext context = null; // TODO
                ILogger logger = LoggerUtils.logger(TermFactory.class);
                return new TypesmartTermFactory(genericFactory, logger, context);
            } 
        } catch(FileSystemException e) {
        }
        return genericFactory;
    }

    @Override public ITermFactory getGeneric() {
        return genericFactory;
    }
}
