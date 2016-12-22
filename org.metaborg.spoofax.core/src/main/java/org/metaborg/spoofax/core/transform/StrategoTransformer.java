package org.metaborg.spoofax.core.transform;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.action.TransformActionContrib;
import org.metaborg.core.context.IContext;
import org.metaborg.core.editor.IEditorRegistry;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.transform.ITransformConfig;
import org.metaborg.core.transform.TransformException;
import org.metaborg.core.unit.IUnit;
import org.metaborg.spoofax.core.action.TransformAction;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.spoofax.core.unit.TransformContrib;
import org.metaborg.spoofax.core.unit.TransformOutput;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.time.Timer;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class StrategoTransformer implements IStrategoTransformer {
    private static final ILogger logger = LoggerUtils.logger(StrategoTransformer.class);

    private final IResourceService resourceService;
    private final ISpoofaxUnitService unitService;
    private final IEditorRegistry editorRegistry;
    private final IStrategoRuntimeService strategoRuntimeService;
    private final IStrategoCommon common;


    @Inject public StrategoTransformer(IResourceService resourceService, ISpoofaxUnitService unitService,
        IEditorRegistry editorRegistry, IStrategoRuntimeService strategoRuntimeService, IStrategoCommon common) {
        this.resourceService = resourceService;
        this.unitService = unitService;
        this.editorRegistry = editorRegistry;
        this.strategoRuntimeService = strategoRuntimeService;
        this.common = common;
    }


    @Override public ISpoofaxTransformUnit<ISpoofaxParseUnit> transform(ISpoofaxParseUnit input, IContext context,
        TransformActionContrib action, ITransformConfig config) throws TransformException {
        return transform(input, context, action, input.source(), input.ast(), config);
    }

    @Override public ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit> transform(ISpoofaxAnalyzeUnit input, IContext context,
        TransformActionContrib action, ITransformConfig config) throws TransformException {
        if(!input.valid()) {
            throw new TransformException("Cannot transform analyze unit " + input + ", it is not valid");
        }
        if(!input.hasAst()) {
            throw new TransformException("Cannot transform analyze unit " + input + ", it has no AST");
        }
        return transform(input, context, action, input.source(), input.ast(), config);
    }

    @Override public Collection<ISpoofaxTransformUnit<ISpoofaxParseUnit>> transformAllParsed(
        Iterable<ISpoofaxParseUnit> inputs, IContext context, TransformActionContrib action, ITransformConfig config)
        throws TransformException {
        final int size = Iterables.size(inputs);
        final Collection<ISpoofaxTransformUnit<ISpoofaxParseUnit>> transformUnits =
            Lists.newArrayListWithCapacity(size);
        for(ISpoofaxParseUnit input : inputs) {
            transformUnits.add(transform(input, context, action, input.source(), input.ast(), config));
        }
        return transformUnits;
    }

    @Override public Collection<ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit>> transformAllAnalyzed(
        Iterable<ISpoofaxAnalyzeUnit> inputs, IContext context, TransformActionContrib action, ITransformConfig config)
        throws TransformException {
        final int size = Iterables.size(inputs);
        final Collection<ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit>> transformUnits =
            Lists.newArrayListWithCapacity(size);
        for(ISpoofaxAnalyzeUnit input : inputs) {
            if(!input.valid()) {
                throw new TransformException("Cannot transform analyze unit " + input + ", it is not valid");
            }
            if(!input.hasAst()) {
                throw new TransformException("Cannot transform analyze unit " + input + ", it has no AST");
            }
            transformUnits.add(transform(input, context, action, input.source(), input.ast(), config));
        }
        return transformUnits;
    }


    private <I extends IUnit> ISpoofaxTransformUnit<I> transform(I input, IContext context,
        TransformActionContrib actionContribution, FileObject source, IStrategoTerm term, ITransformConfig config)
        throws TransformException {
        final FileObject location = context.location();
        final ILanguageComponent component = actionContribution.contributor;
        final TransformAction action = action(actionContribution.action);

        // Get input term
        final IStrategoTerm inputTerm;
        try {
            inputTerm = common.builderInputTerm(term, source, location);
        } catch(MetaborgException e) {
            throw new TransformException("Transformation failed unexpectedly; cannot create input term", e);
        }

        // Get Stratego runtime
        final HybridInterpreter runtime;
        try {
            runtime = strategoRuntimeService.runtime(component, context, true);
        } catch(MetaborgException e) {
            throw new TransformException("Transformation failed unexpectedly; cannot get Stratego interpreter", e);
        }

        // Transform
        logger.debug("Transforming {} with '{}'", source, action.name);
        final Timer timer = new Timer(true);
        final IStrategoTerm outputTerm;
        try {
            outputTerm = common.invoke(runtime, inputTerm, action.strategy);
        } catch(MetaborgException e) {
            throw new TransformException(e.getMessage(), e.getCause());
        }
        final long duration = timer.stop();
        if(outputTerm == null) {
            final String message = logger.format("Invoking Stratego strategy {} failed", action.strategy);
            throw new TransformException(message);
        }

        // Get the result and, if allowed and required, write to file
        List<TransformOutput> outputs;
        IStrategoTerm resultTerm;
        if(outputTerm.getSubtermCount() == 2 && (outputTerm instanceof IStrategoTuple)) {
            final IStrategoTerm resourceTerm = outputTerm.getSubterm(0);
            final IStrategoTerm contentTerm = outputTerm.getSubterm(1);
            try {
                if(resourceTerm instanceof IStrategoString) {
                    resultTerm = contentTerm;
                    outputs = Lists.newArrayList(output(resourceTerm, contentTerm, location, config));
                } else if(resourceTerm instanceof IStrategoList) {
                    if(!(contentTerm instanceof IStrategoList) || resourceTerm.getSubtermCount() != contentTerm.getSubtermCount()) {
                        logger.error("List of terms does not match list of file names, cannot write to file.");
                        resultTerm = null;
                        outputs = Collections.emptyList();
                    } else {
                        outputs = Lists.newArrayListWithExpectedSize(resourceTerm.getSubtermCount());
                        for(int i = 0; i < resourceTerm.getSubtermCount(); i++) {
                            outputs.add(output(resourceTerm.getSubterm(i), contentTerm.getSubterm(i), location, config));
                        }
                        resultTerm = resourceTerm.getSubtermCount() == 1 ? resourceTerm.getSubterm(0) : null;
                    }
                } else {
                    logger.error("First term of result tuple {} is neither a string, nor a list, cannot write output file", resourceTerm);
                    resultTerm = null;
                    outputs = Collections.emptyList();
                }
            } catch(MetaborgException ex) {
                resultTerm = null;
                outputs = Collections.emptyList();
            }
        } else {
            resultTerm = outputTerm;
            outputs = Collections.emptyList();
        }

        // Open editor
        if(action.flags.openEditor) {
            List<FileObject> resources = Lists.newArrayListWithExpectedSize(outputs.size());
            for(TransformOutput output : outputs) {
                if(output.resource != null) {
                    resources.add(output.resource);
                }
            }
            editorRegistry.open(resources, context.project());
        }

        // Return result
        final TransformContrib contrib = new TransformContrib(resultTerm != null || !Iterables.isEmpty(outputs), true,
            resultTerm, outputs, Iterables2.<IMessage>empty(), duration);
        return unitService.transformUnit(input, contrib, context, actionContribution);
    }

    private TransformAction action(ITransformAction action) throws TransformException {
        if(!(action instanceof TransformAction)) {
            final String message = logger.format("Action {} is not a Stratego transformation action", action);
            throw new TransformException(message);
        }
        return (TransformAction) action;
    }
 
    private TransformOutput output(IStrategoTerm resourceTerm, IStrategoTerm contentTerm, FileObject location,
            ITransformConfig config) throws MetaborgException {
        if(!(resourceTerm instanceof IStrategoString)) {
            throw new MetaborgException("First term of result tuple {} is not a string, cannot write output file");
        } else {
            final String resourceString = Tools.asJavaString(resourceTerm);
            final String resultContents = common.toString(contentTerm);
            // writing to output file is allowed
            FileObject output;
            if(!config.dryRun()) {
                output = resourceService.resolve(location, resourceString);
                try(OutputStream stream = output.getContent().getOutputStream()) {
                    IOUtils.write(resultContents, stream, Charset.defaultCharset());
                } catch(IOException e) {
                    logger.error("Error occurred while writing output file", e);
                }
            } else {
                output = null;
            }
            return new TransformOutput(resourceString, output, contentTerm);
        }
    }
 
}