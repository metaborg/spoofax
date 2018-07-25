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
import org.metaborg.spoofax.core.action.JavaTransformAction;
import org.metaborg.spoofax.core.action.StrategoTransformAction;
import org.metaborg.spoofax.core.semantic_provider.IBuilderInput;
import org.metaborg.spoofax.core.semantic_provider.ISemanticProviderService;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.spoofax.core.unit.TransformContrib;
import org.metaborg.spoofax.core.unit.TransformOutput;
import org.metaborg.spoofax.core.user_definable.ITransformer;
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

public class SpoofaxTransformer implements ISpoofaxTransformer {
    private static final ILogger logger = LoggerUtils.logger(SpoofaxTransformer.class);

    private final IResourceService resourceService;
    private final ISpoofaxUnitService unitService;
    private final IEditorRegistry editorRegistry;
    private final IStrategoRuntimeService strategoRuntimeService;
    private final IStrategoCommon common;
    private final ISemanticProviderService semanticProviderService;


    private static class TransformResult {
        public final long duration;
        public final List<TransformOutput> outputs;
        public final IStrategoTerm resultTerm;

        public TransformResult(long duration, List<TransformOutput> outputs, IStrategoTerm resultTerm) {
            this.duration = duration;
            this.outputs = outputs;
            this.resultTerm = resultTerm;
        }
    }


    @Inject public SpoofaxTransformer(IResourceService resourceService, ISpoofaxUnitService unitService,
        IEditorRegistry editorRegistry, IStrategoRuntimeService strategoRuntimeService, 
        IStrategoCommon common, ISemanticProviderService semanticProviderService) {
        this.resourceService = resourceService;
        this.unitService = unitService;
        this.editorRegistry = editorRegistry;
        this.strategoRuntimeService = strategoRuntimeService;
        this.common = common;
        this.semanticProviderService = semanticProviderService;
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
        final ITransformAction action = actionContribution.action;

        // Get input term
        final IBuilderInput inputTerm = common.builderInputTerm(term, source, location);

        // Transform
        TransformResult transformResult = transform(context, source, location, component, action, inputTerm);

        // Output files
        if(!config.dryRun()) {
            for (TransformOutput output : transformResult.outputs) {
                try(OutputStream stream = output.resource.getContent().getOutputStream()) {
                    IOUtils.write(common.toString(output.ast), stream, Charset.defaultCharset());
                } catch(IOException e) {
                    logger.error("Error occurred while writing output file", e);
                }
            }
        }

        // Open editor
        if(action.flags().openEditor) {
            List<FileObject> resources = Lists.newArrayListWithExpectedSize(transformResult.outputs.size());
            for(TransformOutput output : transformResult.outputs) {
                if(output.resource != null) {
                    resources.add(output.resource);
                }
            }
            editorRegistry.open(resources, context.project());
        }

        // Return result
        final TransformContrib contrib = new TransformContrib(transformResult.resultTerm != null || !Iterables.isEmpty(transformResult.outputs), true,
                transformResult.resultTerm, transformResult.outputs, Iterables2.<IMessage>empty(), transformResult.duration);
        return unitService.transformUnit(input, contrib, context, actionContribution);
    }


    private TransformResult transform(IContext context, FileObject source, FileObject location,
            ILanguageComponent component, ITransformAction action, IBuilderInput inputTerm)
            throws TransformException {
        if(action instanceof StrategoTransformAction) {
            return strategoTransform(context, source, location, component, (StrategoTransformAction) action, inputTerm);
        }
        if(action instanceof JavaTransformAction) {
            return javaTransform(context, source, location, component, (JavaTransformAction) action, inputTerm);
        }
        logger.warn("ITransformAction has unexpected type: ", action.getClass());
        return null;
    }


    private TransformResult strategoTransform(IContext context, FileObject source, FileObject location,
            ILanguageComponent component, StrategoTransformAction action, IStrategoTerm inputTerm)
            throws TransformException {
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
                    outputs = Lists.newArrayList(output(resourceTerm, contentTerm, location));
                } else if(resourceTerm instanceof IStrategoList) {
                    if(!(contentTerm instanceof IStrategoList) || resourceTerm.getSubtermCount() != contentTerm.getSubtermCount()) {
                        logger.error("List of terms does not match list of file names, cannot write to file.");
                        resultTerm = null;
                        outputs = Collections.emptyList();
                    } else {
                        outputs = Lists.newArrayListWithExpectedSize(resourceTerm.getSubtermCount());
                        for(int i = 0; i < resourceTerm.getSubtermCount(); i++) {
                            outputs.add(output(resourceTerm.getSubterm(i), contentTerm.getSubterm(i), location));
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

        return new TransformResult(duration, outputs, resultTerm);
    }


    private TransformResult javaTransform(IContext context, FileObject source, FileObject location,
            ILanguageComponent component, JavaTransformAction action, IBuilderInput inputTerm)
            throws TransformException {
        final ITransformer transformer;
        try {
            transformer = semanticProviderService.transformer(component, action.className);
        } catch(MetaborgException e) {
            throw new TransformException(e.getMessage(), e.getCause());
        }

        logger.debug("Transforming {} with '{}'", source, action.name);

        final List<TransformOutput> outputs = Lists.newArrayList();

        final Timer timer = new Timer(true);
        final IStrategoTerm resultTerm = transformer.transform(context, inputTerm, location, outputs);
        final long duration = timer.stop();

        return new TransformResult(duration, outputs, resultTerm);
    }

    private TransformOutput output(IStrategoTerm resourceTerm, IStrategoTerm contentTerm, FileObject location)
            throws MetaborgException {
        if(!(resourceTerm instanceof IStrategoString)) {
            throw new MetaborgException("First term of result tuple {} is not a string, cannot write output file");
        } else {
            final String resourceString = Tools.asJavaString(resourceTerm);
            FileObject output = resourceService.resolve(location, resourceString);
            return new TransformOutput(resourceString, output, contentTerm);
        }
    }

}