package org.metaborg.spoofax.core.stratego;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.transform.TransformResult;
import org.metaborg.core.transform.TransformerException;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.time.Timer;
import org.spoofax.interpreter.core.InterpreterErrorExit;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.core.UndefinedStrategyException;
import org.spoofax.interpreter.stratego.CallT;
import org.spoofax.interpreter.stratego.SDefT;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;
import org.strategoxt.lang.Context;
import org.strategoxt.stratego_aterm.aterm_escape_strings_0_0;
import org.strategoxt.stratego_aterm.pp_aterm_box_0_0;
import org.strategoxt.stratego_gpp.box2text_string_0_1;

import com.google.inject.Inject;

/**
 * Common code for using Stratego transformations in Spoofax.
 */
public class StrategoCommon implements IStrategoCommon {
    private static final ILogger logger = LoggerUtils.logger(StrategoCommon.class);

    private final IResourceService resourceService;
    private final IStrategoRuntimeService strategoRuntimeService;
    private final ITermFactoryService termFactoryService;


    @Inject public StrategoCommon(IResourceService resourceService, IStrategoRuntimeService strategoRuntimeService,
        ITermFactoryService termFactoryService) {
        this.resourceService = resourceService;
        this.strategoRuntimeService = strategoRuntimeService;
        this.termFactoryService = termFactoryService;
    }


    @Override public @Nullable IStrategoTerm invoke(ILanguageComponent component, IContext context,
        IStrategoTerm input, String strategy) throws MetaborgException {
        final HybridInterpreter runtime = strategoRuntimeService.runtime(component, context);
        return invoke(runtime, input, strategy);
    }

    @Override public @Nullable IStrategoTerm invoke(ILanguageImpl impl, IContext context, IStrategoTerm input,
        String strategy) throws MetaborgException {
        for(ILanguageComponent component : impl.components()) {
            if(component.facet(StrategoRuntimeFacet.class) == null) {
                continue;
            }

            final HybridInterpreter runtime = strategoRuntimeService.runtime(component, context);
            final IStrategoTerm result = invoke(runtime, input, strategy);
            if(result != null) {
                return result;
            }

        }
        return null;
    }

    @Override public @Nullable IStrategoTerm invoke(HybridInterpreter runtime, IStrategoTerm input, String strategy)
        throws MetaborgException {
        runtime.setCurrent(input);
        try {
            boolean success = runtime.invoke(strategy);
            if(!success) {
                return null;
            }
            return runtime.current();
        } catch(InterpreterException e) {
            handleException(e, strategy);
            throw new MetaborgException("Invoking Stratego strategy failed unexpectedly", e);
        }
    }

    @Override public IStrategoTerm invoke(HybridInterpreter runtime, IStrategoTerm input, String strategy,
        Strategy[] sp, IStrategoTerm... tp) {
        final SDefT def = runtime.lookupUncifiedSVar(strategy);
        final Strategy strat = def.getBody();
        final CallT callT = (CallT) strat;

        final org.spoofax.interpreter.core.IContext context = runtime.getContext();
        context.setCurrent(input);
        boolean success = false;
        try {
            success = callT.evaluateWithArgs(context, sp, tp);
        } catch(InterpreterException e) {
            throw new RuntimeException("Failed to evaluate strategy " + strategy, e);
        }
        return success ? context.current() : null;
    }

    private void handleException(InterpreterException ex, String strategy) throws MetaborgException {
        try {
            throw ex;
        } catch(InterpreterErrorExit e) {
            final String message;
            final IStrategoTerm term = e.getTerm();
            if(term != null) {
                final String termString;
                final IStrategoString ppTerm = prettyPrint(term);
                if(ppTerm != null) {
                    termString = ppTerm.stringValue();
                } else {
                    termString = term.toString();
                }
                message = logger.format("Invoking Stratego strategy {} failed at term\n\t{}", strategy, termString);
            } else {
                message = logger.format("Invoking Stratego strategy {} failed", strategy);
            }
            throw new MetaborgException(message, e);
        } catch(InterpreterExit e) {
            final String message =
                logger.format("Invoking Stratego strategy {} failed with exit code {}", strategy, e.getValue());
            throw new MetaborgException(message, e);
        } catch(UndefinedStrategyException e) {
            final String message =
                logger.format("Invoking Stratego strategy {} failed, strategy is undefined", strategy);
            throw new MetaborgException(message, e);
        } catch(InterpreterException e) {
            final Throwable cause = e.getCause();
            if(cause != null && cause instanceof InterpreterException) {
                handleException((InterpreterException) cause, strategy);
            } else {
                throw new MetaborgException("Invoking Stratego strategy failed unexpectedly", e);
            }
        }
    }


    @Override public <PrevT> TransformResult<PrevT, IStrategoTerm> transform(ILanguageComponent component,
        IContext context, PrevT prevResult, String strategy, IStrategoTerm input, FileObject resource)
        throws MetaborgException {
        final HybridInterpreter runtime;
        try {
            runtime = strategoRuntimeService.runtime(component, context);
        } catch(MetaborgException e) {
            throw new TransformerException("Failed to get Stratego interpreter", e);
        }

        final Timer timer = new Timer(true);
        final IStrategoTerm result = invoke(runtime, input, strategy);
        final long duration = timer.stop();
        if(result == null) {
            final String message = logger.format("Invoking Stratego strategy {} failed", strategy);
            throw new MetaborgException(message);
        }

        final TransformResult<PrevT, IStrategoTerm> transResult =
            new TransformResult<PrevT, IStrategoTerm>(result, Collections.<IMessage>emptyList(),
                Collections.singletonList(resource), context, duration, prevResult);
        return transResult;
    }


    @Override public IStrategoString localLocationTerm(File localLocation) {
        final ITermFactory termFactory = termFactoryService.getGeneric();
        final String locationPath = localLocation.getAbsolutePath();
        final IStrategoString locationPathTerm = termFactory.makeString(locationPath);
        return locationPathTerm;
    }

    @Override public IStrategoString localResourceTerm(File localResource, File localLocation) {
        final ITermFactory termFactory = termFactoryService.getGeneric();
        final String resourcePath = localLocation.toURI().relativize(localResource.toURI()).getPath();
        final IStrategoString resourcePathTerm = termFactory.makeString(resourcePath);
        return resourcePathTerm;
    }

    @Override public IStrategoTerm builderInputTerm(IStrategoTerm ast, FileObject resource, FileObject location)
        throws MetaborgException {
        final ITermFactory termFactory = termFactoryService.getGeneric();

        // GTODO: support selected node
        final IStrategoTerm node = ast;
        // GTODO: support position
        final IStrategoTerm position = termFactory.makeList();

        final File localLocation;
        try {
            localLocation = resourceService.localFile(location);
        } catch(MetaborgRuntimeException e) {
            final String message = String.format("Location %s does not exist", location);
            logger.error(message, e);
            throw new MetaborgException(message, e);
        }
        final IStrategoString locationTerm = localLocationTerm(localLocation);

        final File localResource = resourceService.localPath(resource);
        if(localResource == null) {
            final String message = String.format("Resource %s does not reside on the local file system", resource);
            logger.error(message);
            throw new MetaborgException(message);
        }
        final IStrategoString resourceTerm = localResourceTerm(localResource, localLocation);

        return termFactory.makeTuple(node, position, ast, resourceTerm, locationTerm);
    }

    @Override public FileObject builderWriteResult(IStrategoTerm result, FileObject location) {
        if(!(result instanceof IStrategoTuple))
            return null;

        final IStrategoTerm resourceTerm = result.getSubterm(0);
        if(!(resourceTerm instanceof IStrategoString)) {
            logger.error("First term of result tuple {} is not a string, cannot write output file");
        } else {
            final String resourceString = Tools.asJavaString(resourceTerm);
            final String resultContents = builderResultToString(result);

            final FileObject resource = resourceService.resolve(location, resourceString);
            try(OutputStream stream = resource.getContent().getOutputStream()) {
                IOUtils.write(resultContents, stream);
            } catch(IOException e) {
                logger.error("Error occured while writing output file", e);
            }
            return resource;
        }

        return null;
    }

    @Override public String builderResultToString(IStrategoTerm result) {
        if(result.getSubtermCount() == 0) {
            return "";
        }
        final IStrategoTerm resultTerm = result.getSubterm(1);
        if(resultTerm.getTermType() == IStrategoTerm.STRING) {
            return ((IStrategoString) resultTerm).stringValue();
        } else {
            final IStrategoString pp = prettyPrint(resultTerm);
            if(pp != null) {
                return pp.stringValue();
            } else {
                logger.error("Could not pretty print ATerm, falling back to non-pretty printed ATerm");
                return resultTerm.toString();
            }
        }
    }


    @Override public IStrategoString prettyPrint(IStrategoTerm term) {
        final Context context = strategoRuntimeService.genericRuntime().getCompiledContext();
        final ITermFactory termFactory = termFactoryService.getGeneric();
        org.strategoxt.stratego_aterm.Main.init(context);
        term = aterm_escape_strings_0_0.instance.invoke(context, term);
        term = pp_aterm_box_0_0.instance.invoke(context, term);
        term = box2text_string_0_1.instance.invoke(context, term, termFactory.makeInt(120));
        return (IStrategoString) term;
    }
}
