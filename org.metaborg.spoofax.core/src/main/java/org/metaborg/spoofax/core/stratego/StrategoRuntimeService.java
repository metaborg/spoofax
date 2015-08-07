package org.metaborg.spoofax.core.stratego;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.SpoofaxRuntimeException;
import org.metaborg.spoofax.core.analysis.stratego.StrategoFacet;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageCache;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.stratego.strategies.ParseStrategoFileStrategy;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.IOperatorRegistry;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.strategoxt.HybridInterpreter;
import org.strategoxt.IncompatibleJarException;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.LibraryInitializer;
import org.strategoxt.lang.LibraryInitializer.InitializerSetEntry;
import org.strategoxt.lang.RegisteringStrategy;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;

public class StrategoRuntimeService implements IStrategoRuntimeService, ILanguageCache {
    private static final Logger logger = LoggerFactory.getLogger(StrategoRuntimeService.class);

    private final IResourceService resourceService;
    private final ITermFactoryService termFactoryService;
    private final Set<IOperatorRegistry> strategoLibraries;
    private final ParseStrategoFileStrategy parseStrategoFileStrategy;

    private final Map<ILanguage, HybridInterpreter> prototypes = new HashMap<ILanguage, HybridInterpreter>();


    @Inject public StrategoRuntimeService(IResourceService resourceService, ITermFactoryService termFactoryService,
        Set<IOperatorRegistry> strategoLibraries, ParseStrategoFileStrategy parseStrategoFileStrategy) {
        this.resourceService = resourceService;
        this.termFactoryService = termFactoryService;
        this.strategoLibraries = strategoLibraries;
        this.parseStrategoFileStrategy = parseStrategoFileStrategy;
    }


    @Override public HybridInterpreter runtime(IContext context) throws SpoofaxException {
        final ILanguage language = context.language();
        HybridInterpreter prototype = prototypes.get(language);
        if(prototype == null) {
            prototype = createPrototypeRuntime(language);
        }

        // TODO: this seems to copy operator registries, but they should be recreated to isolate interpreters?
        final HybridInterpreter interpreter = new HybridInterpreter(prototype, new String[0]);
        final ResourceAgent agent = new ResourceAgent(resourceService);
        agent.setAbsoluteWorkingDir(context.location());
        agent.setAbsoluteDefinitionDir(context.language().location());
        interpreter.setIOAgent(agent);
        interpreter.getContext().setContextObject(context);
        interpreter.getCompiledContext().setContextObject(context);
        interpreter.getCompiledContext().getExceptionHandler().setEnabled(false);
        // Add primitive libraries again, to make sure that our libraries override any default ones.
        for(IOperatorRegistry library : strategoLibraries) {
            interpreter.getCompiledContext().addOperatorRegistry(library);
        }
        interpreter.init();

        return interpreter;
    }

    @Override public HybridInterpreter genericRuntime() {
        return createRuntime(new ImploderOriginTermFactory(termFactoryService.getGeneric()));
    }

    @Override public void invalidateCache(ILanguage language) {
        logger.debug("Removing cached stratego runtime for {}", language);
        prototypes.remove(language);
    }


    private HybridInterpreter createRuntime(ITermFactory termFactory) {
        final HybridInterpreter interpreter = new HybridInterpreter(termFactory);

        interpreter.getCompiledContext().registerComponent("stratego_lib");
        interpreter.getCompiledContext().registerComponent("stratego_sglr");

        for(IOperatorRegistry library : strategoLibraries) {
            interpreter.getCompiledContext().addOperatorRegistry(library);
        }
        
        // Override parse Stratego file strategy with one that works with Spoofax core.
        interpreter.getCompiledContext().getStrategyCollector().addLibraryInitializers(Arrays.asList(new InitializerSetEntry(new LibraryInitializer() {

			@Override
			protected List<RegisteringStrategy> getLibraryStrategies() {
				return Arrays.<RegisteringStrategy>asList(parseStrategoFileStrategy);
			}

			@Override
			protected void initializeLibrary(Context context) {
				// TODO Auto-generated method stub
				
			}
        	
        })));
        return interpreter;
    }

    private HybridInterpreter createPrototypeRuntime(ILanguage language) throws SpoofaxException {
        logger.debug("Creating prototype runtime for {}", language);
        final HybridInterpreter interpreter =
            createRuntime(new ImploderOriginTermFactory(termFactoryService.get(language)));
        loadCompilerFiles(interpreter, language);
        prototypes.put(language, interpreter);
        return interpreter;
    }

    private void loadCompilerFiles(HybridInterpreter interp, ILanguage lang) throws SpoofaxException {
        final StrategoFacet strategoFacet = lang.facet(StrategoFacet.class);
        final Iterable<FileObject> jars = strategoFacet.jarFiles();
        final Iterable<FileObject> ctrees = strategoFacet.ctreeFiles();

        // Order is important, load CTrees first.
        if(Iterables.size(ctrees) > 0)
            loadCompilerCTree(interp, ctrees);
        if(Iterables.size(jars) > 0)
            loadCompilerJar(interp, jars);
    }

    private void loadCompilerJar(HybridInterpreter interp, Iterable<FileObject> jars) throws SpoofaxException {
        try {
            final URL[] classpath = new URL[Iterables.size(jars)];
            int i = 0;
            for(FileObject jar : jars) {
                final File localJar = resourceService.localFile(jar);
                classpath[i] = localJar.toURI().toURL();
                ++i;
            }
            logger.trace("Loading jar files {}", (Object) classpath);
            interp.loadJars(classpath);
        } catch(IncompatibleJarException | IOException | SpoofaxRuntimeException e) {
            throw new SpoofaxException("Failed to load JAR", e);
        }
    }

    private static void loadCompilerCTree(HybridInterpreter interp, Iterable<FileObject> ctrees)
        throws SpoofaxException {
        try {
            for(FileObject file : ctrees) {
                logger.trace("Loading ctree {}", file.getName());
                interp.load(new BufferedInputStream(file.getContent().getInputStream()));
            }
        } catch(IOException | InterpreterException e) {
            throw new SpoofaxException("Failed to load ctree", e);
        }
    }
}
