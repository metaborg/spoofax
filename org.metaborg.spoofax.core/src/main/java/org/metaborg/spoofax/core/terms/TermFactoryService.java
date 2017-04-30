package org.metaborg.spoofax.core.terms;

import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.build.dependency.MissingDependencyException;
import org.metaborg.core.language.ILanguageCache;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.build.SpoofaxCommonPaths;
import org.metaborg.spoofax.core.config.ISpoofaxProjectConfig;
import org.metaborg.spoofax.core.config.ISpoofaxProjectConfigService;
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
    private final ISpoofaxProjectConfigService configService;

    @Inject public TermFactoryService(IDependencyService dependencyService, ISpoofaxProjectConfigService configService) {
        this.dependencyService = dependencyService;
        this.configService = configService;
    }

    private final ITermFactory genericFactory = new ImploderOriginTermFactory(new TermFactory());

    private final Map<ILanguageImpl, TypesmartContext> implMergedTypesmartContexts = Maps.newHashMap();
    private final Map<ILanguageComponent, TypesmartContext> mergedTypesmartContexts = Maps.newHashMap();

    @Override public ITermFactory get(ILanguageImpl impl, @Nullable IProject project, boolean supportsTypesmart) {
        if(!supportsTypesmart || project == null) {
            return genericFactory;
        }
        ISpoofaxProjectConfig config = configService.get(project);
        if(config == null || !config.typesmart()) {
            return genericFactory;
        }

        TypesmartContext context = getTypesmartContext(impl);
        if(!context.isEmpty()) {
            return new TypesmartTermFactory(genericFactory, typesmartLogger, context);
        } else {
            return genericFactory;
        }
    }

    @Override public ITermFactory get(ILanguageComponent component, @Nullable IProject project,
        boolean supportsTypesmart) {
        if(!supportsTypesmart || project == null) {
            return genericFactory;
        }
        ISpoofaxProjectConfig config = configService.get(project);
        if(config == null || !config.typesmart()) {
            return genericFactory;
        }

        TypesmartContext context = getTypesmartContext(component);
        if(!context.isEmpty()) {
            return new TypesmartTermFactory(genericFactory, typesmartLogger, context);
        } else {
            return genericFactory;
        }
    }

    @Override public ITermFactory getGeneric() {
        return genericFactory;
    }

    @Override public void invalidateCache(ILanguageImpl impl) {
        implMergedTypesmartContexts.remove(impl);
    }

    @Override public void invalidateCache(ILanguageComponent component) {
        mergedTypesmartContexts.remove(component);
    }

    private TypesmartContext getTypesmartContext(ILanguageImpl impl) {
        TypesmartContext context = implMergedTypesmartContexts.get(impl);
        if(context == null) {
            context = TypesmartContext.empty();
            for(ILanguageComponent component : impl.components()) {
                context = context.merge(getTypesmartContext(component));
            }
            implMergedTypesmartContexts.put(impl, context);
        }
        return context;
    }

    private TypesmartContext getTypesmartContext(ILanguageComponent component) {
        TypesmartContext context = mergedTypesmartContexts.get(component);
        if(context == null) {
            FileObject localContextFile = new SpoofaxCommonPaths(component.location()).strTypesmartExportedFile();
            context = TypesmartContext.load(localContextFile, typesmartLogger);
            try {
                for(ILanguageComponent other : dependencyService.sourceDeps(component)) {
                    FileObject otherContextFile = new SpoofaxCommonPaths(other.location()).strTypesmartExportedFile();
                    TypesmartContext otherContext = TypesmartContext.load(otherContextFile, typesmartLogger);
                    context = context.merge(otherContext);
                }
            } catch(MissingDependencyException e) {
                typesmartLogger
                    .error("Could not load source dependencies of " + component + " to resolve typesmart contexts.", e);
            }
            mergedTypesmartContexts.put(component, context);
        }
        return context;
    }
}
