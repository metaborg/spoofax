package org.metaborg.spoofax.core.analysis.constraint;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageFactory;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.analysis.AnalysisCommon;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzer;
import org.metaborg.spoofax.core.analysis.SpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.context.constraint.IConstraintContext;
import org.metaborg.spoofax.core.context.constraint.IConstraintContext.FileResult;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.tracing.ISpoofaxTracingService;
import org.metaborg.spoofax.core.unit.AnalyzeContrib;
import org.metaborg.spoofax.core.unit.AnalyzeUpdateData;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class MultiFileConstraintAnalyzer extends AbstractConstraintAnalyzer implements ISpoofaxAnalyzer {

    public static final String name = "constraint-multifile";

    private static final ILogger logger = LoggerUtils.logger(MultiFileConstraintAnalyzer.class);

    @Inject public MultiFileConstraintAnalyzer(final AnalysisCommon analysisCommon,
            final IResourceService resourceService, final IStrategoRuntimeService runtimeService,
            final IStrategoCommon strategoCommon, final ITermFactoryService termFactoryService,
            final ISpoofaxTracingService tracingService, final ISpoofaxUnitService unitService) {
        super(analysisCommon, resourceService, runtimeService, strategoCommon, termFactoryService, tracingService,
                unitService);
    }

    @Override protected ISpoofaxAnalyzeResults analyzeAll(final Map<String, ISpoofaxParseUnit> changed,
            final Set<String> removed, final IConstraintContext context, final HybridInterpreter runtime,
            final String strategy, final IProgress progress, ICancel cancel) throws AnalysisException {
        final String project = context.resourceKey(context.location());
        final IStrategoTerm projectTerm = termFactory.makeString(project);

        // defined early to add results for failed files
        final Set<ISpoofaxAnalyzeUnit> analysisResults = Sets.newHashSet();
        final Set<ISpoofaxAnalyzeUnitUpdate> updateResults = Sets.newHashSet();

        // clear removed files
        for(String source : removed) {
            context.remove(source);
        }

        // analyze project initial
        final IConstraintContext.InitialResult initialResult;
        if(context.hasInitial()) {
            initialResult = context.getInitial();
        } else {
            final IStrategoTerm action = build("AnalyzeMultiInitial", projectTerm);
            final List<IStrategoTerm> result;
            try {
                result = match(strategoCommon.invoke(runtime, action, strategy), "MultiInitialResult", 1);
            } catch(MetaborgException ex) {
                logger.error("Initial analysis of failed.", ex);
                throw new AnalysisException(context, "Initial project analysis failed.", ex);
            }
            if(result == null) {
                logger.error("Initial analysis of failed.");
                throw new AnalysisException(context, "Initial project analysis failed.");
            }

            // result = analysis
            final IStrategoTerm analysis = result.get(0);
            initialResult = new IConstraintContext.InitialResult(analysis);

            context.setInitial(initialResult);
        }

        // analyze changed files
        final Map<String, IConstraintContext.FileResult> unitResults = Maps.newHashMap();
        for(Entry<String, ISpoofaxParseUnit> entry : changed.entrySet()) {
            final String source = entry.getKey();
            final ISpoofaxParseUnit input = entry.getValue();
            final FileObject resource = input.source();
            final IStrategoTerm sourceTerm = termFactory.makeString(source);
            try {
                if(!input.detached()) {
                    context.remove(source);
                }

                // analyze single unit
                final IStrategoTerm action = build("AnalyzeMultiUnit", sourceTerm, input.ast(), initialResult.analysis);
                final List<IStrategoTerm> result =
                        match(strategoCommon.invoke(runtime, action, strategy), "SingleResult", 5);
                if(result == null) {
                    logger.warn("Analysis of '" + source + "' failed.");
                    List<IMessage> messages = ImmutableList
                            .of(MessageFactory.newAnalysisErrorAtTop(input.source(), "File analysis failed.", null));
                    analysisResults.add(unitService.analyzeUnit(input,
                            new AnalyzeContrib(false, false, true, input.ast(), messages, -1), context));
                    continue;
                }

                // result = (ast, analysis)
                final IStrategoTerm analyzedAST = result.get(0);
                final IStrategoTerm analysis = result.get(1);
                final Collection<IMessage> messages = analysisCommon.ambiguityMessages(resource, input.ast());

                final FileResult unitResult = new FileResult(analyzedAST, analysis, messages);
                unitResults.put(source, unitResult);
                if(!input.detached()) {
                    context.put(source, unitResult);
                }
            } catch(MetaborgException e) {
                logger.warn("Analysis of '" + source + "' failed.", e);
                List<IMessage> messages = ImmutableList
                        .of(MessageFactory.newAnalysisErrorAtTop(input.source(), "File analysis failed.", e));
                analysisResults.add(unitService.analyzeUnit(input,
                        new AnalyzeContrib(false, false, true, input.ast(), messages, -1), context));
            }
        }

        // analyze project final
        final Multimap<FileObject, IMessage> messagesByFile = HashMultimap.create();
        {
            final IStrategoList unitAnalyses = termFactory
                    .makeList(context.entrySet().stream().map(e -> e.getValue().analysis).collect(Collectors.toList()));
            final IStrategoTerm action =
                    build("AnalyzeMultiInitial", projectTerm, initialResult.analysis, unitAnalyses);
            final List<IStrategoTerm> result;
            try {
                result = match(strategoCommon.invoke(runtime, action, strategy), "MultiFinalResult", 1);
            } catch(MetaborgException ex) {
                logger.error("Initial analysis of failed.", ex);
                throw new AnalysisException(context, "Initial project analysis failed.", ex);
            }
            if(result == null) {
                logger.error("Final analysis of failed.");
                throw new AnalysisException(context, "Final project analysis failed.");
            }

            // result = (analysis, errors, warnings, notes)
            final IStrategoTerm finalAnalysis = result.get(0);
            messagesByFile.putAll(analysisCommon.messages(MessageSeverity.ERROR, result.get(1)));
            messagesByFile.putAll(analysisCommon.messages(MessageSeverity.WARNING, result.get(2)));
            messagesByFile.putAll(analysisCommon.messages(MessageSeverity.NOTE, result.get(3)));

            context.setFinal(new IConstraintContext.FinalResult(finalAnalysis, messagesByFile.values()));
        }
        if(messagesByFile.containsKey(null)) {
            logger.warn("Found messages not attached to file: {}", messagesByFile.get(null));
        }

        // produce results
        for(Entry<String, FileResult> entry : unitResults.entrySet()) { // all changed files
            final String source = entry.getKey();
            final FileResult unitResult = entry.getValue();
            final ISpoofaxParseUnit input = changed.get(source);
            final Collection<IMessage> messages = ImmutableList.<IMessage>builder().addAll(unitResult.messages)
                    .addAll(messagesByFile.get(input.source())).build();
            analysisResults.add(unitService.analyzeUnit(input,
                    new AnalyzeContrib(true, success(messages), true, unitResult.ast, messages, -1), context));
        }

        for(Entry<String, FileResult> entry : context.entrySet()) { // all other known files
            final String source = entry.getKey();
            if(changed.containsKey(source)) {
                continue;
            }
            final FileResult unitResult = entry.getValue();
            FileObject resource;
            try {
                resource = context.keyResource(source);
            } catch(IOException e) {
                logger.warn("Cannot update analysis for {}, because it cannot be resolved", source);
                continue;
            }
            final Collection<IMessage> messages = ImmutableList.<IMessage>builder().addAll(unitResult.messages)
                    .addAll(messagesByFile.get(resource)).build();
            updateResults.add(unitService.analyzeUnitUpdate(resource, new AnalyzeUpdateData(messages), context));
        }


        return new SpoofaxAnalyzeResults(analysisResults, updateResults, context, null);
    }

}