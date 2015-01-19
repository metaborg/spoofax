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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.analysis.stratego.StrategoFacet;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.IOperatorRegistry;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.strategoxt.HybridInterpreter;
import org.strategoxt.IncompatibleJarException;
import org.strategoxt.NoInteropRegistererJarException;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;

public class StrategoRuntimeService implements IStrategoRuntimeService {
    private static final Logger logger = LogManager.getLogger(StrategoRuntimeService.class);

    private final IResourceService resourceService;
    private final ITermFactoryService termFactoryService;
    private final Set<IOperatorRegistry> strategoLibraries;

    private final Map<ILanguage, HybridInterpreter> prototypes = new HashMap<ILanguage, HybridInterpreter>();


    @Inject public StrategoRuntimeService(IResourceService resourceService,
        ITermFactoryService termFactoryService, Set<IOperatorRegistry> strategoLibraries) {
        this.resourceService = resourceService;
        this.termFactoryService = termFactoryService;
        this.strategoLibraries = strategoLibraries;
    }


    @Override public HybridInterpreter getRuntime(ILanguage lang) throws SpoofaxException {
        HybridInterpreter proto = prototypes.get(lang);
        if(proto == null) {
            proto = createPrototypeRuntime(lang);
        }

        // TODO: load overrides and contexts
        final HybridInterpreter interp = new HybridInterpreter(proto, new String[0]);
        interp.getCompiledContext().getExceptionHandler().setEnabled(false);
        interp.init();

        return interp;
    }

    @Override public IStrategoTerm callStratego(ILanguage lang, String strategy, IStrategoTerm input,
        FileObject workingLocation) throws SpoofaxException {
        assert lang != null;
        assert strategy != null && strategy.length() > 0;
        assert input != null;
        logger.trace("Calling strategy {} with input {}", strategy, input);

        final HybridInterpreter runtime = getRuntime(lang);
        boolean success = false;
        try {
            runtime.setCurrent(input);
            if(workingLocation != null) {
                ((ResourceAgent) runtime.getIOAgent()).setAbsoluteWorkingDir(workingLocation);
            }
            success = runtime.invoke(strategy);
        } catch(InterpreterException e) {
            throw new SpoofaxException("Stratego call failed", e);
        }

        if(success) {
            return runtime.current();
        } else {
            // TODO: should this return null when failed, or throw an exception?
            throw new SpoofaxException("Stratego call failed w/o exception");
        }
    }

    private HybridInterpreter createPrototypeRuntime(ILanguage lang) throws SpoofaxException {
        final ITermFactory termFactory = new ImploderOriginTermFactory(termFactoryService.get(lang));
        final HybridInterpreter interp = new HybridInterpreter(termFactory);

        interp.getCompiledContext().registerComponent("stratego_lib");
        interp.getCompiledContext().registerComponent("stratego_sglr");

        for(IOperatorRegistry library : strategoLibraries) {
            interp.getCompiledContext().addOperatorRegistry(library);
        }

        final ResourceAgent agent = new ResourceAgent(resourceService);
        agent.setAbsoluteDefinitionDir(lang.location());
        interp.setIOAgent(agent);
        loadCompilerFiles(interp, lang);

        prototypes.put(lang, interp);

        return interp;
    }

    private void loadCompilerFiles(HybridInterpreter interp, ILanguage lang) {
        final StrategoFacet strategoFacet = lang.facet(StrategoFacet.class);
        final Iterable<FileObject> jars = strategoFacet.jarFiles();
        final Iterable<FileObject> ctrees = strategoFacet.ctreeFiles();

        // for some reason the order is important. We must always load the
        // ctrees first (if any).
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
