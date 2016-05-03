package org.metaborg.spoofax.core.terms;

import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.build.CommonPaths;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.build.dependency.MissingDependencyException;
import org.metaborg.core.language.ILanguageCache;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageComponent;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.typesmart.TypesmartContext;
import org.spoofax.terms.typesmart.TypesmartTermFactory;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class TermFactoryService implements ITermFactoryService, ILanguageCache {
    private static final ILogger typesmartLogger = LoggerUtils.logger("Typesmart");

    private final IDependencyService dependencyService;

    @Inject public TermFactoryService(IDependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    private final ITermFactory genericFactory = new ImploderOriginTermFactory(new TermFactory());

    private final Map<ILanguageComponent, TypesmartContext> mergedTypesmartContexts = Maps.newHashMap();


    @Override public ITermFactory get(ILanguageImpl impl) {
        // TODO find typesmart contexts and merge them
        return genericFactory;
    }

    @Override public ITermFactory get(ILanguageComponent component) {
        if(component.config().typesmart()) {
            TypesmartContext context = getTypesmartContext(component);
            return new TypesmartTermFactory(genericFactory, typesmartLogger, context);
        } else {
            return genericFactory;
        }
    }

    @Override public ITermFactory getGeneric() {
        return genericFactory;
    }

    @Override public void invalidateCache(ILanguageImpl impl) {
    }

    @Override public void invalidateCache(ILanguageComponent component) {
        mergedTypesmartContexts.remove(component);
    }

    private TypesmartContext getTypesmartContext(ILanguageComponent component) {
        TypesmartContext context = mergedTypesmartContexts.get(component);
        if(context == null) {
            FileObject localContextFile = new CommonPaths(component.location()).strTypesmartExportedFile();
            context = TypesmartContext.load(localContextFile, typesmartLogger);

            try {
                for(ILanguageComponent other : dependencyService.sourceDeps(component)) {
                    FileObject otherContextFile = new CommonPaths(other.location()).strTypesmartExportedFile();
                    TypesmartContext otherContext = TypesmartContext.load(otherContextFile, typesmartLogger);
                    context = context.merge(otherContext);
                }
            } catch(MissingDependencyException e) {
                typesmartLogger.error("Could not load source dependencies of " + component + " to resolve typesmart contexts.", e);
            }
        }
        return context;
    }
}
