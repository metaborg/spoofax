package org.metaborg.spoofax.core.stratego;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.analysis.stratego.StrategoFacet;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.IOperatorRegistry;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.strategoxt.HybridInterpreter;
import org.strategoxt.IncompatibleJarException;
import org.strategoxt.NoInteropRegistererJarException;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;

public class StrategoRuntimeService implements IStrategoRuntimeService {
    private static final Logger logger = LoggerFactory.getLogger(StrategoRuntimeService.class);

    private final IResourceService resourceService;
    private final ITermFactoryService termFactoryService;
    private final Set<IOperatorRegistry> strategoLibraries;

    private final Map<ILanguage, HybridInterpreter> prototypes = new HashMap<ILanguage, HybridInterpreter>();


    @Inject public StrategoRuntimeService(IResourceService resourceService, ITermFactoryService termFactoryService,
        Set<IOperatorRegistry> strategoLibraries) {
        this.resourceService = resourceService;
        this.termFactoryService = termFactoryService;
        this.strategoLibraries = strategoLibraries;
    }


    @Override public HybridInterpreter runtime(IContext context) {
        final ILanguage language = context.language();
        HybridInterpreter prototype = prototypes.get(language);
        if(prototype == null) {
            prototype = createPrototypeRuntime(language);
        }

        // TODO: load overrides and contexts
        // TODO: this seems to copy operator registries, but they should be recreated to isolate interpreters?
        final HybridInterpreter interpreter = new HybridInterpreter(prototype, new String[0]);
        final ResourceAgent agent = new ResourceAgent(resourceService);
        agent.setAbsoluteWorkingDir(context.location());
        agent.setAbsoluteDefinitionDir(context.language().location());
        interpreter.setIOAgent(agent);
        interpreter.getContext().setContextObject(context);
        interpreter.getCompiledContext().getExceptionHandler().setEnabled(false);
        interpreter.init();

        return interpreter;
    }

    @Override public HybridInterpreter genericRuntime() {
        final ITermFactory factory = new ImploderOriginTermFactory(termFactoryService.getGeneric());
        final HybridInterpreter interpreter = new HybridInterpreter(factory);

        interpreter.getCompiledContext().registerComponent("stratego_lib");
        interpreter.getCompiledContext().registerComponent("stratego_sglr");

        for(IOperatorRegistry library : strategoLibraries) {
            interpreter.getCompiledContext().addOperatorRegistry(library);
        }

        return interpreter;
    }


    private HybridInterpreter createPrototypeRuntime(ILanguage lang) {
        final ITermFactory factory = new ImploderOriginTermFactory(termFactoryService.get(lang));
        final HybridInterpreter interpreter = new HybridInterpreter(factory);

        interpreter.getCompiledContext().registerComponent("stratego_lib");
        interpreter.getCompiledContext().registerComponent("stratego_sglr");

        for(IOperatorRegistry library : strategoLibraries) {
            interpreter.getCompiledContext().addOperatorRegistry(library);
        }

        loadCompilerFiles(interpreter, lang);

        prototypes.put(lang, interpreter);

        return interpreter;
    }

    private void loadCompilerFiles(HybridInterpreter interp, ILanguage lang) {
        final StrategoFacet strategoFacet = lang.facet(StrategoFacet.class);
        final Iterable<FileObject> jars = strategoFacet.jarFiles();
        final Iterable<FileObject> ctrees = strategoFacet.ctreeFiles();

        // For some reason the order is important, we must always load the CTrees first (if any).
        if(Iterables.size(ctrees) > 0)
            loadCompilerCTree(interp, ctrees);
        if(Iterables.size(jars) > 0)
            loadCompilerJar(interp, jars);
    }

    private void loadCompilerJar(HybridInterpreter interp, Iterable<FileObject> jars) {
        try {
            final URL[] classpath = new URL[Iterables.size(jars)];
            int i = 0;
            for(FileObject jar : jars) {
                final File localJar = resourceService.localFile(jar);
                if(localJar == null) {
                    throw new RuntimeException("Loading JARs from non-filesystem resources is not supported");
                }
                classpath[i] = localJar.toURI().toURL();
                ++i;
            }
            logger.trace("Loading jar files {}", (Object) classpath);
            interp.loadJars(classpath);
        } catch(MalformedURLException e) {
            throw new RuntimeException("Failed to load JAR", e);
        } catch(SecurityException e) {
            throw new RuntimeException("Failed to load JAR", e);
        } catch(NoInteropRegistererJarException e) {
            throw new RuntimeException("Failed to load JAR", e);
        } catch(IncompatibleJarException e) {
            throw new RuntimeException("Failed to load JAR", e);
        } catch(IOException e) {
            throw new RuntimeException("Failed to load JAR", e);
        }
    }

    private static void loadCompilerCTree(HybridInterpreter interp, Iterable<FileObject> ctrees) {
        try {
            for(FileObject file : ctrees) {
                logger.trace("Loading ctree {}", file.getName());
                interp.load(new BufferedInputStream(file.getContent().getInputStream()));
            }
        } catch(IOException e) {
            throw new RuntimeException("Failed to load ctree", e);
        } catch(InterpreterException e) {
            throw new RuntimeException("Failed to load ctree", e);
        }
    }
}
