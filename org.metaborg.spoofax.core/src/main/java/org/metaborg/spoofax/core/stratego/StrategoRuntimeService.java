package org.metaborg.spoofax.core.stratego;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.dynamicclassloading.DynamicClassLoader;
import org.metaborg.spoofax.core.dynamicclassloading.DynamicClassLoadingFacet;
import org.metaborg.spoofax.core.stratego.strategies.ParseStrategoFileStrategy;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.IOperatorRegistry;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;
import org.strategoxt.IncompatibleJarException;
import org.strategoxt.strc.parse_stratego_file_0_0;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;

public class StrategoRuntimeService implements IStrategoRuntimeService, AutoCloseable {
    private static final ILogger logger = LoggerUtils.logger(StrategoRuntimeService.class);

    private final IResourceService resourceService;
    private final ITermFactoryService termFactoryService;
    private final Set<IOperatorRegistry> strategoLibraries;
    private final ParseStrategoFileStrategy parseStrategoFileStrategy;
    private final Set<ClassLoader> additionalClassLoaders;

    private final Map<ILanguageComponent, HybridInterpreter> prototypes = new HashMap<>();


    @Inject public StrategoRuntimeService(IResourceService resourceService, ITermFactoryService termFactoryService,
        Set<IOperatorRegistry> strategoLibraries, ParseStrategoFileStrategy parseStrategoFileStrategy,
        Set<ClassLoader> additionalClassLoaders) {
        this.resourceService = resourceService;
        this.termFactoryService = termFactoryService;
        this.strategoLibraries = strategoLibraries;
        this.parseStrategoFileStrategy = parseStrategoFileStrategy;
        this.additionalClassLoaders = additionalClassLoaders;
    }

    @Override public void close() {
        // Uninitialize prototype hybrid interpreters and clear the cache.
        for(HybridInterpreter runtime : prototypes.values()) {
            runtime.uninit();
        }
        prototypes.clear();

        // Set parse Stratego file strategy to default, freeing reference to services held by ParseStrategoFileStrategy.
        parse_stratego_file_0_0.instance = new parse_stratego_file_0_0();
    }


    @Override public HybridInterpreter runtime(ILanguageComponent component, IContext context)
        throws MetaborgException {
        HybridInterpreter prototype = prototypes.get(component);
        if(prototype == null) {
            prototype = createPrototype(component);
        }

        final HybridInterpreter runtime = clone(prototype, context.location(), component);
        runtime.getContext().setContextObject(context);
        runtime.getCompiledContext().setContextObject(context);
        return runtime;
    }

    @Override public HybridInterpreter runtime(ILanguageComponent component, FileObject location)
        throws MetaborgException {
        HybridInterpreter prototype = prototypes.get(component);
        if(prototype == null) {
            prototype = createPrototype(component);
        }

        final HybridInterpreter runtime = clone(prototype, location, component);
        return runtime;
    }

    @Override public HybridInterpreter genericRuntime() {
        final ITermFactory termFactory = termFactoryService.getGeneric();
        return createNew(termFactory);
    }


    @Override public void invalidateCache(ILanguageComponent component) {
        logger.debug("Removing cached stratego runtime for {}", component);
        final @Nullable HybridInterpreter runtime = prototypes.remove(component);
        if(runtime != null) {
            runtime.uninit();
        }
    }

    @Override public void invalidateCache(ILanguageImpl impl) {
        logger.debug("Removing cached stratego runtime for {}", impl);
        for(ILanguageComponent component : impl.components()) {
            final @Nullable HybridInterpreter runtime = prototypes.remove(component);
            if(runtime != null) {
                runtime.uninit();
            }
        }
    }


    private HybridInterpreter clone(HybridInterpreter prototype, FileObject workingLocation,
        ILanguageComponent component) {
        // TODO: this seems to copy operator registries, but they should be recreated to isolate interpreters?
        final HybridInterpreter runtime = new HybridInterpreter(prototype);

        final ResourceAgent agent = new ResourceAgent(resourceService);
        agent.setAbsoluteWorkingDir(workingLocation);
        agent.setAbsoluteDefinitionDir(component.location());
        runtime.setIOAgent(agent);

        runtime.getCompiledContext().getExceptionHandler().setEnabled(false);

        // Add primitive libraries again, to make sure that our libraries override any default ones.
        for(IOperatorRegistry library : strategoLibraries) {
            runtime.getCompiledContext().addOperatorRegistry(library);
        }

        final ITermFactory termFactory = termFactoryService.getGeneric();
        runtime.getContext().setFactory(termFactory);
        runtime.getCompiledContext().setFactory(termFactory);

        runtime.init();

        return runtime;
    }

    private HybridInterpreter createNew(ITermFactory termFactory) {
        final HybridInterpreter interpreter = new HybridInterpreter(termFactory);

        interpreter.getCompiledContext().registerComponent("stratego_lib");
        interpreter.getCompiledContext().registerComponent("stratego_sglr");

        for(IOperatorRegistry library : strategoLibraries) {
            interpreter.getCompiledContext().addOperatorRegistry(library);
        }

        // Override parse Stratego file strategy with one that works with Spoofax core.
        parse_stratego_file_0_0.instance = parseStrategoFileStrategy;

        return interpreter;
    }

    private HybridInterpreter createPrototype(ILanguageComponent component) throws MetaborgException {
        logger.debug("Creating prototype runtime for {}", component);
        final ITermFactory termFactory = termFactoryService.getGeneric();
        final HybridInterpreter runtime = createNew(termFactory);
        loadFiles(runtime, component);
        prototypes.put(component, runtime);
        return runtime;
    }

    private void loadFiles(HybridInterpreter runtime, ILanguageComponent component) throws MetaborgException {
        final StrategoRuntimeFacet strategoRuntimeFacet = component.facet(StrategoRuntimeFacet.class);
        if(strategoRuntimeFacet == null) {
            final String message =
                String.format("Cannot get Stratego runtime for %s, it does not have a Stratego facet", component);
            logger.error(message);
            throw new MetaborgException(message);
        }
        final DynamicClassLoadingFacet dynamicClassLoadingFacet = component.facet(DynamicClassLoadingFacet.class);
        if(dynamicClassLoadingFacet == null) {
            final String message =
                String.format("Cannot get Stratego runtime for %s, it does not have a Stratego facet", component);
            logger.error(message);
            throw new MetaborgException(message);
        }

        // Order is important, load CTrees first.
        final Iterable<FileObject> ctrees = strategoRuntimeFacet.ctreeFiles;
        if(Iterables.size(ctrees) > 0) {
            loadCtrees(runtime, ctrees);
        }
        final Iterable<FileObject> jars = dynamicClassLoadingFacet.jarFiles;
        if(Iterables.size(jars) > 0) {
            loadJars(runtime, jars);
        }
    }

    private void loadJars(HybridInterpreter runtime, Iterable<FileObject> jars) throws MetaborgException {
        try {
            final URL[] classpath = new URL[Iterables.size(jars)];
            int i = 0;
            for(FileObject jar : jars) {
                final File localJar = resourceService.localFile(jar);
                classpath[i] = localJar.toURI().toURL();
                ++i;
            }
            logger.trace("Loading jar files {}", (Object) classpath);
            final ClassLoader classLoader = new DynamicClassLoader(additionalClassLoaders);
            runtime.loadJars(classLoader, classpath);
        } catch(IncompatibleJarException | IOException | MetaborgRuntimeException e) {
            throw new MetaborgException("Failed to load JAR", e);
        }
    }

    private static void loadCtrees(HybridInterpreter runtime, Iterable<FileObject> ctrees) throws MetaborgException {
        try {
            for(FileObject file : ctrees) {
                logger.trace("Loading ctree {}", file.getName());
                runtime.load(new BufferedInputStream(file.getContent().getInputStream()));
            }
        } catch(IOException | InterpreterException e) {
            throw new MetaborgException("Failed to load ctree", e);
        }
    }
}
