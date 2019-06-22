package org.metaborg.spoofax.core.transform;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.action.TransformActionContrib;
import org.metaborg.core.context.IContext;
import org.metaborg.core.editor.IEditorRegistry;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.transform.ITransformConfig;
import org.metaborg.core.transform.TransformException;
import org.metaborg.core.unit.IUnit;
import org.metaborg.spoofax.core.dynamicclassloading.BuilderInput;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.unit.*;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

public class SpoofaxTransformer implements ISpoofaxTransformer {
    private static final ILogger logger = LoggerUtils.logger(SpoofaxTransformer.class);

    private final ISpoofaxUnitService unitService;
    private final IEditorRegistry editorRegistry;
    private final IStrategoCommon common;


    @Inject public SpoofaxTransformer(ISpoofaxUnitService unitService, IEditorRegistry editorRegistry,
        IStrategoCommon common) {
        this.unitService = unitService;
        this.editorRegistry = editorRegistry;
        this.common = common;
    }


    @Override public ISpoofaxTransformUnit<ISpoofaxParseUnit> transform(ISpoofaxParseUnit input, IContext context,
        TransformActionContrib<ISpoofaxTransformAction> action, ITransformConfig config) throws TransformException {
        return transform(input, context, action, input.source(), input.ast(), config);
    }

    @Override public ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit> transform(ISpoofaxAnalyzeUnit input, IContext context,
        TransformActionContrib<ISpoofaxTransformAction> action, ITransformConfig config) throws TransformException {
        if(!input.valid()) {
            throw new TransformException("Cannot transform analyze unit " + input + ", it is not valid");
        }
        if(!input.hasAst()) {
            throw new TransformException("Cannot transform analyze unit " + input + ", it has no AST");
        }
        return transform(input, context, action, input.source(), input.ast(), config);
    }

    @Override public Collection<ISpoofaxTransformUnit<ISpoofaxParseUnit>> transformAllParsed(
        Iterable<ISpoofaxParseUnit> inputs, IContext context, TransformActionContrib<ISpoofaxTransformAction> action,
        ITransformConfig config) throws TransformException {
        final int size = Iterables.size(inputs);
        final Collection<ISpoofaxTransformUnit<ISpoofaxParseUnit>> transformUnits =
            Lists.newArrayListWithCapacity(size);
        for(ISpoofaxParseUnit input : inputs) {
            transformUnits.add(transform(input, context, action, input.source(), input.ast(), config));
        }
        return transformUnits;
    }

    @Override public Collection<ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit>> transformAllAnalyzed(
        Iterable<ISpoofaxAnalyzeUnit> inputs, IContext context, TransformActionContrib<ISpoofaxTransformAction> action,
        ITransformConfig config) throws TransformException {
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
        TransformActionContrib<ISpoofaxTransformAction> actionContribution, FileObject source, IStrategoTerm term,
        ITransformConfig config) throws TransformException {
        final FileObject location = context.location();
        final ILanguageComponent component = actionContribution.contributor;
        final ISpoofaxTransformAction action = actionContribution.action;

        // Get input term
        final BuilderInput inputTerm = common.builderInputTerm(term, source, location);

        // Transform
        TransformResult transformResult = action.transform(context, source, location, component, inputTerm);

        // Output files
        if(!config.dryRun()) {
            for(TransformOutput output : transformResult.outputs) {
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
        final TransformContrib contrib =
            new TransformContrib(transformResult.resultTerm != null || !Iterables.isEmpty(transformResult.outputs),
                true, transformResult.resultTerm, transformResult.outputs, Iterables2.<IMessage>empty(),
                transformResult.duration);
        return unitService.transformUnit(input, contrib, context, actionContribution);
    }
}