package org.metaborg.spoofax.core.analysis.constraint;

import java.util.Collection;
import java.util.Collections;
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
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class ConstraintSingleFileAnalyzer extends AbstractConstraintAnalyzer implements ISpoofaxAnalyzer {

    public static final String name = "constraint-singlefile";

    private static final ILogger logger = LoggerUtils.logger(ConstraintSingleFileAnalyzer.class);

    @Inject public ConstraintSingleFileAnalyzer(final AnalysisCommon analysisCommon,
            final IResourceService resourceService, final IStrategoRuntimeService runtimeService,
            final IStrategoCommon strategoCommon, final ITermFactoryService termFactoryService,
            final ISpoofaxTracingService tracingService, final ISpoofaxUnitService unitService) {
        super(analysisCommon, resourceService, runtimeService, strategoCommon, termFactoryService, tracingService,
                unitService);
    }

    @Override protected ISpoofaxAnalyzeResults analyzeAll(final Set<ISpoofaxParseUnit> changed,
            final Set<ISpoofaxParseUnit> removed, final IConstraintContext context, final HybridInterpreter runtime,
            final String strategy, final IProgress progress, ICancel cancel) throws AnalysisException {

        // clear removed files
        for(ISpoofaxParseUnit input : removed) {
            // clear result of input.resource(), if !input.detached()
        }

        // analyze changed files
        final Set<ISpoofaxAnalyzeUnit> results = Sets.newHashSet();
        for(ISpoofaxParseUnit input : changed) {
            final FileObject resource = input.source();
            final String source = input.detached() ? "" : resource(resource, context);
            try {
                // clear stored data for input.resource() if !input.detached()
                final IStrategoConstructor ctor = termFactory.makeConstructor("Analyze", 3);
                final IStrategoTerm resourceTerm = termFactory.makeString(source);
                final IStrategoTerm action = termFactory.makeAppl(ctor, resourceTerm, input.ast());

                final IStrategoTerm result = strategoCommon.invoke(runtime, action, strategy);
                if(result == null) {
                    logger.warn("Analysis of " + source + " failed.");
                    Iterable<IMessage> messages = Iterables2.singleton(
                            MessageFactory.newAnalysisErrorAtTop(input.source(), "File analysis failed.", null));
                    results.add(unitService.analyzeUnit(input,
                            new AnalyzeContrib(false, false, true, input.ast(), messages, -1), context));
                    continue;
                }
                // (ast, result, errors, warnings, notes)

                final IStrategoTerm analyzedAST = result.getSubterm(0);
                final IStrategoTerm analysis = result.getSubterm(1);
                final Collection<IMessage> errors =
                        analysisCommon.messages(resource, MessageSeverity.ERROR, result.getSubterm(2));
                final Collection<IMessage> warnings =
                        analysisCommon.messages(resource, MessageSeverity.WARNING, result.getSubterm(3));
                final Collection<IMessage> notes =
                        analysisCommon.messages(resource, MessageSeverity.NOTE, result.getSubterm(4));
                final Collection<IMessage> ambiguities = analysisCommon.ambiguityMessages(resource, input.ast());

                final Collection<IMessage> messages = Lists
                        .newArrayListWithCapacity(errors.size() + warnings.size() + notes.size() + ambiguities.size());
                messages.addAll(errors);
                messages.addAll(warnings);
                messages.addAll(notes);
                messages.addAll(ambiguities);

                if(!input.detached()) {
                    // store analysis
                }
                results.add(unitService.analyzeUnit(input,
                        new AnalyzeContrib(true, errors.isEmpty(), true, analyzedAST, messages, -1), context));
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
