package org.metaborg.spoofax.core.transform.stratego;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.stratego.StrategoLocalPath;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeUtils;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.transform.TransformResult;
import org.metaborg.spoofax.core.transform.TransformerException;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.time.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

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


    @Inject public StrategoTransformerCommon(IResourceService resourceService, IStrategoRuntimeService strategoRuntimeService,
        ITermFactoryService termFactoryService, StrategoLocalPath localPath) {
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
        } catch(SpoofaxException e) {
            throw new TransformerException("Failed to get Stratego interpreter", e);
        }

        final IStrategoTerm result;
        final long duration;
        try {
            final Timer timer = new Timer(true);
            result = StrategoRuntimeUtils.invoke(runtime, input, strategy);
            duration = timer.stop();
        } catch(SpoofaxException e) {
            throw new TransformerException("Stratego invocation failed", e);
        }

        final TransformResult<PrevT, IStrategoTerm> transResult =
            new TransformResult<PrevT, IStrategoTerm>(result, Iterables2.<IMessage>empty(),
                Iterables2.singleton(resource), context, duration, prevResult);
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
     * @throws SpoofaxException
     *             When {@code resource} or {@code location} do not reside on the local file system.
     */
    public IStrategoTerm builderInputTerm(IStrategoTerm ast, FileObject resource, FileObject location)
        throws SpoofaxException {
        final ITermFactory termFactory = termFactoryService.getGeneric();

        // GTODO: support selected node
        final IStrategoTerm node = ast;
        // GTODO: support position
        final IStrategoTerm position = termFactory.makeList();

        final File localLocation = resourceService.localFile(location);
        if(localLocation == null) {
            final String message = String.format("Context %s does not reside on the local file system", location);
            logger.error(message);
            throw new SpoofaxException(message);
        }
        final IStrategoString locationTerm = localPath.localLocationTerm(localLocation);

        final File localResource = resourceService.localFile(resource);
        if(localResource == null) {
            final String message = String.format("Context %s does not reside on the local file system", resource);
            logger.error(message);
            throw new SpoofaxException(message);
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
                // GTODO: pretty print the ATerm.
                resultContents = resultTerm.toString();
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
}
