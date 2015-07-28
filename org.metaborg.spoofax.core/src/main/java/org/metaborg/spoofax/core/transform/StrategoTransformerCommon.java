package org.metaborg.spoofax.core.transform;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.transform.TransformResult;
import org.metaborg.core.transform.TransformerException;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.stratego.StrategoLocalPath;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeUtils;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.util.time.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.core.Tools;
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
public class StrategoTransformerCommon {
    private static final Logger logger = LoggerFactory.getLogger(StrategoTransformerCommon.class);

    private final IResourceService resourceService;
    private final IStrategoRuntimeService strategoRuntimeService;
    private final ITermFactoryService termFactoryService;

    private final StrategoLocalPath localPath;


    @Inject public StrategoTransformerCommon(IResourceService resourceService,
        IStrategoRuntimeService strategoRuntimeService, ITermFactoryService termFactoryService,
        StrategoLocalPath localPath) {
        this.resourceService = resourceService;
        this.strategoRuntimeService = strategoRuntimeService;
        this.termFactoryService = termFactoryService;
        this.localPath = localPath;
    }


    /**
     * Executes given strategy and creates a transformation result.
     * 
     * @param context
     *            Context to execute Stratego code in.
     * @param prevResult
     *            Originating result
     * @param strategy
     *            Strategy to execute
     * @param input
     *            Term to execute the strategy with.
     * @param resource
     *            Origin resource of the input term.
     * @return Transformation result
     * @throws TransformerException
     *             When Stratego invocation fails.
     */
    public <PrevT> TransformResult<PrevT, IStrategoTerm> transform(IContext context, PrevT prevResult, String strategy,
        IStrategoTerm input, FileObject resource) throws TransformerException {
        final HybridInterpreter runtime;
        try {
            runtime = strategoRuntimeService.runtime(context);
        } catch(MetaborgException e) {
            throw new TransformerException("Failed to get Stratego interpreter", e);
        }

        final IStrategoTerm result;
        final long duration;
        try {
            final Timer timer = new Timer(true);
            result = StrategoRuntimeUtils.invoke(runtime, input, strategy);
            duration = timer.stop();
        } catch(MetaborgException e) {
            throw new TransformerException("Stratego invocation failed", e);
        }

        final TransformResult<PrevT, IStrategoTerm> transResult =
            new TransformResult<PrevT, IStrategoTerm>(result, Collections.<IMessage>emptyList(),
                Collections.singletonList(resource), context, duration, prevResult);
        return transResult;
    }

    /**
     * Creates an input term for a builder.
     * 
     * @param ast
     *            Term to use as the AST.
     * @param resource
     *            Location of the input resource.
     * @param location
     *            Location of the input context.
     * @return A 5-tuple input term (selected, position, ast, path, project-path).
     * @throws MetaborgException
     *             When {@code resource} or {@code location} do not reside on the local file system.
     */
    public IStrategoTerm builderInputTerm(IStrategoTerm ast, FileObject resource, FileObject location)
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
        final IStrategoString locationTerm = localPath.localLocationTerm(localLocation);

        final File localResource = resourceService.localPath(resource);
        if(localResource == null) {
            final String message = String.format("Resource %s does not reside on the local file system", resource);
            logger.error(message);
            throw new MetaborgException(message);
        }
        final IStrategoString resourceTerm = localPath.localResourceTerm(localResource, localLocation);

        return termFactory.makeTuple(node, position, ast, resourceTerm, locationTerm);
    }

    /**
     * Attempts to write given builder result to a file.
     * 
     * @param result
     *            Builder result to write, should be a 2-tuple (term, filename).
     * @param location
     *            Directory to write the file to.
     * @return Written file, or null if writing a file failed.
     */
    public FileObject builderWriteResult(IStrategoTerm result, FileObject location) {
        if(!(result instanceof IStrategoTuple))
            return null;

        final IStrategoTerm resourceTerm = result.getSubterm(0);
        if(!(resourceTerm instanceof IStrategoString)) {
            logger.error("First term of result tuple {} is not a string, cannot write output file");
        } else {
            final String resourceString = Tools.asJavaString(resourceTerm);
            final IStrategoTerm resultTerm = result.getSubterm(1);
            final String resultContents;
            if(resultTerm.getTermType() == IStrategoTerm.STRING) {
                resultContents = ((IStrategoString) resultTerm).stringValue();
            } else {
                final IStrategoString pp = ppATerm(resultTerm);
                if(pp != null) {
                    resultContents = pp.stringValue();
                } else {
                    logger.error("Could not pretty print ATerm, falling back to non-pretty printed ATerm");
                    resultContents = resultTerm.toString();
                }
            }

            try {
                final FileObject resource = location.resolveFile(resourceString);
                try(OutputStream stream = resource.getContent().getOutputStream()) {
                    IOUtils.write(resultContents, stream);
                } catch(IOException e) {
                    logger.error("Error occured while writing output file", e);
                }
                return resource;
            } catch(FileSystemException e) {
                logger.error("Error occured while writing output file", e);
            }
        }

        return null;
    }

    /**
     * Pretty prints an ATerm.
     * 
     * @param term
     *            ATerm to pretty print.
     * @return Pretty printed ATerm as a Stratego string.
     */
    private IStrategoString ppATerm(IStrategoTerm term) {
        final Context context = strategoRuntimeService.genericRuntime().getCompiledContext();
        final ITermFactory termFactory = termFactoryService.getGeneric();
        term = aterm_escape_strings_0_0.instance.invoke(context, term);
        term = pp_aterm_box_0_0.instance.invoke(context, term);
        term = box2text_string_0_1.instance.invoke(context, term, termFactory.makeInt(120));
        return (IStrategoString) term;
    }
}
