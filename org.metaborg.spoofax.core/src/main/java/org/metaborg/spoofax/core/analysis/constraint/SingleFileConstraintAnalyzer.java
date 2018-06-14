package org.metaborg.spoofax.core.analysis.constraint;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class SingleFileConstraintAnalyzer extends AbstractConstraintAnalyzer implements ISpoofaxAnalyzer {

    public static final String name = "constraint-singlefile";

    private static final ILogger logger = LoggerUtils.logger(SingleFileConstraintAnalyzer.class);

    @Inject public SingleFileConstraintAnalyzer(final AnalysisCommon analysisCommon,
            final IResourceService resourceService, final IStrategoRuntimeService runtimeService,
            final IStrategoCommon strategoCommon, final ITermFactoryService termFactoryService,
            final ISpoofaxTracingService tracingService, final ISpoofaxUnitService unitService) {
        super(analysisCommon, resourceService, runtimeService, strategoCommon, termFactoryService, tracingService,
                unitService);
    }

    @Override protected ISpoofaxAnalyzeResults analyzeAll(final Map<String, ISpoofaxParseUnit> changed,
            final Set<String> removed, final IConstraintContext context, final HybridInterpreter runtime,
            final String strategy, final IProgress progress, ICancel cancel) throws AnalysisException {

        // clear removed files
        for(String source : removed) {
            context.remove(source);
        }

        // analyze changed files
        final Set<ISpoofaxAnalyzeUnit> results = Sets.newHashSet();
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
                final IStrategoTerm action = build("AnalyzeSingleUnit", sourceTerm, input.ast());
                final List<IStrategoTerm> unitResult =
                        match(strategoCommon.invoke(runtime, action, strategy), "SingleUnitResult", 5);
                if(unitResult == null) {
                    logger.warn("Analysis of " + source + " failed.");
                    Iterable<IMessage> messages = Iterables2.singleton(
                            MessageFactory.newAnalysisErrorAtTop(input.source(), "File analysis failed.", null));
                    results.add(unitService.analyzeUnit(input,
                            new AnalyzeContrib(false, false, true, input.ast(), messages, -1), context));
                    continue;
                }

                // result = (ast, analysis, errors, warnings, notes)
                final IStrategoTerm analyzedAST = unitResult.get(0);
                final IStrategoTerm analysis = unitResult.get(1);
                final Collection<IMessage> messages = Lists.newArrayList();
                messages.addAll(analysisCommon.ambiguityMessages(resource, input.ast()));
                messages.addAll(analysisCommon.messages(resource, MessageSeverity.ERROR, unitResult.get(2)));
                messages.addAll(analysisCommon.messages(resource, MessageSeverity.WARNING, unitResult.get(3)));
                messages.addAll(analysisCommon.messages(resource, MessageSeverity.NOTE, unitResult.get(4)));

                if(!input.detached()) {
                    context.put(source, new FileResult(analyzedAST, analysis, messages));
                }
                results.add(unitService.analyzeUnit(input,
                        new AnalyzeContrib(true, success(messages), true, analyzedAST, messages, -1), context));
            } catch(MetaborgException e) {
                logger.warn("Analysis of " + source + " failed.", e);
                Iterable<IMessage> messages = Iterables2
                        .singleton(MessageFactory.newAnalysisErrorAtTop(input.source(), "File analysis failed.", e));
                results.add(unitService.analyzeUnit(input,
                        new AnalyzeContrib(false, false, true, input.ast(), messages, -1), context));
            }
        }

        return new SpoofaxAnalyzeResults(results, Collections.emptyList(), context);
    }

}