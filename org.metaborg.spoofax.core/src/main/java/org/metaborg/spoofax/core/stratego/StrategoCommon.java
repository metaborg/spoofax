package org.metaborg.spoofax.core.stratego;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.AggregateMetaborgException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.resource.ResourceUtils;
import org.spoofax.interpreter.core.InterpreterErrorExit;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.core.UndefinedStrategyException;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;
import org.strategoxt.lang.Context;
import org.strategoxt.stratego_aterm.aterm_escape_strings_0_0;
import org.strategoxt.stratego_aterm.pp_aterm_box_0_0;
import org.strategoxt.stratego_gpp.box2text_string_0_1;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * Common code for using Stratego transformations in Spoofax.
 */
public class StrategoCommon implements IStrategoCommon {
    private static final ILogger logger = LoggerUtils.logger(StrategoCommon.class);

    private final IStrategoRuntimeService strategoRuntimeService;
    private final ITermFactoryService termFactoryService;


    @Inject public StrategoCommon(IStrategoRuntimeService strategoRuntimeService,
        ITermFactoryService termFactoryService) {
        this.strategoRuntimeService = strategoRuntimeService;
        this.termFactoryService = termFactoryService;
    }


    @Override public @Nullable IStrategoTerm invoke(ILanguageComponent component, IContext context, IStrategoTerm input,
        String strategy) throws MetaborgException {
        if(component.facet(StrategoRuntimeFacet.class) == null) {
            return null;
        }
        final HybridInterpreter runtime = strategoRuntimeService.runtime(component, context, true);
        return invoke(runtime, input, strategy);
    }

    @Override public @Nullable IStrategoTerm invoke(ILanguageImpl impl, IContext context, IStrategoTerm input,
        String strategy) throws MetaborgException {
        List<MetaborgException> exceptions = Lists.newArrayList();
        for(ILanguageComponent component : impl.components()) {
            if(component.facet(StrategoRuntimeFacet.class) == null) {
                continue;
            }

            final HybridInterpreter runtime = strategoRuntimeService.runtime(component, context, true);
            try {
                final IStrategoTerm result = invoke(runtime, input, strategy);
                return result;
            } catch (MetaborgException ex) {
                exceptions.add(ex);
            }

        }
        throw new AggregateMetaborgException(exceptions);
    }

    @Override public @Nullable IStrategoTerm invoke(ILanguageImpl impl, FileObject location, IStrategoTerm input,
        String strategy) throws MetaborgException {
        List<MetaborgException> exceptions = Lists.newArrayList();
        for(ILanguageComponent component : impl.components()) {
            if(component.facet(StrategoRuntimeFacet.class) == null) {
                continue;
            }

            final HybridInterpreter runtime = strategoRuntimeService.runtime(component, location, true);
            try {
                final IStrategoTerm result = invoke(runtime, input, strategy);
                return result;
            } catch (MetaborgException ex) {
                exceptions.add(ex);
            }

        }
        throw new AggregateMetaborgException(exceptions);
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
            throw handleException(e, runtime, strategy);
        }
    }

    private MetaborgException handleException(InterpreterException ex, HybridInterpreter runtime, String strategy) {
        final String trace = traceToString(runtime.getCompiledContext().getTrace());
        try {
            throw ex;
        } catch(InterpreterErrorExit e) {
            final String message;
            final IStrategoTerm term = e.getTerm();
            final String innerTrace = e.getTrace() != null ? traceToString(e.getTrace()) : trace;
            if(term != null) {
                final String termString;
                final IStrategoString ppTerm = prettyPrint(term);
                if(ppTerm != null) {
                    termString = ppTerm.stringValue();
                } else {
                    termString = term.toString();
                }
                message = logger.format("Invoking Stratego strategy {} failed at term:\n\t{}\n{}\n{}", strategy, termString, innerTrace, e.getMessage());
            } else {
                message = logger.format("Invoking Stratego strategy {} failed.\n{}\n{}", strategy, innerTrace, e.getMessage());
            }
            return new MetaborgException(message, e);
        } catch(InterpreterExit e) {
            final String message =
                logger.format("Invoking Stratego strategy {} failed with exit code {}\n{}\n{}", strategy, e.getValue(), trace, e);
            return new MetaborgException(message, e);
        } catch(UndefinedStrategyException e) {
            final String message =
                logger.format("Invoking Stratego strategy {} failed, strategy is undefined\n{}\n{}", strategy, trace, e);
            return new MetaborgException(message, e);
        } catch(InterpreterException e) {
            final Throwable cause = e.getCause();
            if(cause != null && cause instanceof InterpreterException) {
                return handleException((InterpreterException) cause, runtime, strategy);
            } else {
                String message = logger.format("Invoking Stratego strategy {} failed unexpectedly\n{}\n{}", strategy, trace, e);
                return new MetaborgException(message);
            }
        }
    }

    private String traceToString(String[] trace) {
        StringBuilder sb = new StringBuilder();
        sb.append("Stratego trace:");
        final int depth = trace.length;
        for(int i = 0; i < depth; i++) {
            sb.append("\n\t");
            sb.append(trace[depth - i - 1]);
        }
        return sb.toString();
    }
    
    private String traceToString(IStrategoList trace) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Stratego trace:");
        final int depth = trace.getSubtermCount();
        for(int i = 0; i < depth; i++) {
            final IStrategoTerm t = trace.getSubterm(depth - i - 1);
            sb.append("\n\t");
            sb.append(t.getTermType() == IStrategoTerm.STRING ? Tools.asJavaString(t) : t);
        }
        return sb.toString();
    }

    @Override public IStrategoString locationTerm(FileObject location) {
        final ITermFactory termFactory = termFactoryService.getGeneric();

        final String locationURI = location.getName().getURI();
        final IStrategoString locationTerm = termFactory.makeString(locationURI);

        return locationTerm;
    }

    @Override public IStrategoString resourceTerm(FileObject resource, FileObject location) {
        final ITermFactory termFactory = termFactoryService.getGeneric();

        String resourceURI = ResourceUtils.relativeName(resource.getName(), location.getName(), false);
        final IStrategoString resourceTerm = termFactory.makeString(resourceURI);

        return resourceTerm;
    }

    @Override public IStrategoTerm builderInputTerm(IStrategoTerm ast, FileObject resource, FileObject location) {
        final ITermFactory termFactory = termFactoryService.getGeneric();

        // TODO: support selected node
        final IStrategoTerm node = ast;
        // TODO: support position
        final IStrategoTerm position = termFactory.makeList();

        final String locationURI = location.getName().getURI();
        final IStrategoString locationTerm = termFactory.makeString(locationURI);

        String resourceURI = ResourceUtils.relativeName(resource.getName(), location.getName(), false);
        final IStrategoString resourceTerm = termFactory.makeString(resourceURI);

        return termFactory.makeTuple(node, position, ast, resourceTerm, locationTerm);
    }

    @Override public String toString(IStrategoTerm term) {
        if(term instanceof IStrategoString) {
            return ((IStrategoString) term).stringValue();
        } else {
            final IStrategoString pp = prettyPrint(term);
            if(pp != null) {
                return pp.stringValue();
            } else {
                logger.error("Could not pretty print ATerm, falling back to non-pretty printed ATerm");
                return term.toString();
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
