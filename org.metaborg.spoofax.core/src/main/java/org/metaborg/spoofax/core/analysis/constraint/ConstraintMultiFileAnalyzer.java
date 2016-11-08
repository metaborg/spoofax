package org.metaborg.spoofax.core.analysis.constraint;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.scopegraph.context.IScopeGraphUnit;
import org.metaborg.scopegraph.indices.TermIndex;
import org.metaborg.spoofax.core.analysis.AnalysisCommon;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzer;
import org.metaborg.spoofax.core.analysis.SpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.context.scopegraph.IMultiFileScopeGraphContext;
import org.metaborg.spoofax.core.context.scopegraph.IMultiFileScopeGraphUnit;
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
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;

public class ConstraintMultiFileAnalyzer extends AbstractConstraintAnalyzer<IMultiFileScopeGraphContext>
        implements ISpoofaxAnalyzer {

    public static final ILogger logger = LoggerUtils.logger(ConstraintMultiFileAnalyzer.class);

    public static final String name = "constraint-multifile";

    private final ISpoofaxUnitService unitService;
    private final IResourceService resourceService;

    @Inject public ConstraintMultiFileAnalyzer(final AnalysisCommon analysisCommon,
            final ISpoofaxUnitService unitService, final IResourceService resourceService,
            final IStrategoRuntimeService runtimeService, final IStrategoCommon strategoCommon,
            final ITermFactoryService termFactoryService, final ISpoofaxTracingService tracingService) {
        super(analysisCommon, runtimeService, strategoCommon, termFactoryService, tracingService);
        this.resourceService = resourceService;
        this.unitService = unitService;
    }


    @Override protected ISpoofaxAnalyzeResults analyzeAll(Map<String,ISpoofaxParseUnit> changed,
            Map<String,ISpoofaxParseUnit> removed, IMultiFileScopeGraphContext context, HybridInterpreter runtime,
            String strategy) throws AnalysisException {

        String globalSource = context.location().getName().getURI();
        IStrategoTerm globalTerm = termFactory.makeString(globalSource);
        TermIndex.put(globalTerm, globalSource, 0);

        final IMultiFileScopeGraphUnit globalUnit = context.unit(globalSource);
        if (globalUnit.partialAnalysis() == null) {
            globalUnit.reset();

            IStrategoTerm initialResultTerm = doAction(strategy, termFactory.makeAppl(analyzeInitial, globalTerm),
                    context, runtime);
            InitialResult initialResult;
            try {
                initialResult = InitialResult.fromTerm(initialResultTerm);
            } catch (MetaborgException e) {
                throw new AnalysisException(context, "Initial analysis failed.", e);
            }
            globalUnit.setPartialAnalysis(initialResult.solution);
        }

        for (String input : removed.keySet()) {
            context.removeUnit(input);
        }

        final Map<String,IStrategoTerm> astsByFile = Maps.newHashMap();
        final Multimap<String,IMessage> ambiguitiesByFile = HashMultimap.create();
        for (Map.Entry<String,ISpoofaxParseUnit> input : changed.entrySet()) {
            String source = input.getKey();
            ISpoofaxParseUnit parseUnit = input.getValue();

            IMultiFileScopeGraphUnit unit = context.unit(source);
            unit.reset();

            IStrategoTerm sourceTerm = termFactory.makeString(source);
            TermIndex.put(sourceTerm, source, 0);

            IStrategoTerm unitResultTerm = doAction(strategy,
                    termFactory.makeAppl(analyzeUnit, sourceTerm, parseUnit.ast(), globalUnit.partialAnalysis()),
                    context, runtime);
            UnitResult unitResult;
            try {
                unitResult = UnitResult.fromTerm(unitResultTerm);
                astsByFile.put(source, unitResult.ast);
                ambiguitiesByFile.putAll(source, analysisCommon.ambiguityMessages(parseUnit.source(), unitResult.ast));
                unit.setPartialAnalysis(unitResult.solution);
            } catch (MetaborgException e) {
                logger.warn("Skipping {}, because analysis failed\n{}", source, e.getCause());
            }
        }

        final Collection<IStrategoTerm> unitSolutions = Lists.newArrayList();
        for (IScopeGraphUnit unit : context.units()) {
            if (unit == globalUnit) {
                continue;
            }
            IStrategoTerm unitSolution = unit.analysis();
            if (unitSolution != null) {
                unitSolutions.add(unitSolution);
            }
        }

        IStrategoTerm finalResultTerm = doAction(strategy, termFactory.makeAppl(analyzeFinal, globalTerm,
                globalUnit.partialAnalysis(), termFactory.makeList(unitSolutions)), context, runtime);
        FinalResult finalResult;
        try {
            finalResult = FinalResult.fromTerm(finalResultTerm);
        } catch (MetaborgException e) {
            throw new AnalysisException(context, "Final analysis failed.", e);
        }
        context.setScopeGraph(finalResult.scopeGraph);
        context.setNameResolution(finalResult.nameResolution);
        context.setOccurrenceTypes(finalResult.occurrenceTypes);
        context.setAstMetadata(finalResult.astMetadata);
        context.setAnalysis(finalResult.analysis);

        Multimap<String,IMessage> errorsByFile = messages(finalResult.errors, MessageSeverity.ERROR);
        Multimap<String,IMessage> warningsByFile = messages(finalResult.warnings, MessageSeverity.WARNING);
        Multimap<String,IMessage> notesByFile = messages(finalResult.notes, MessageSeverity.NOTE);

        final Collection<ISpoofaxAnalyzeUnit> results = Lists.newArrayList();
        final Collection<ISpoofaxAnalyzeUnitUpdate> updateResults = Lists.newArrayList();
        for (IMultiFileScopeGraphUnit unit : context.units()) {
            final String source = unit.resource();
            final Collection<IMessage> errors = errorsByFile.get(source);
            final Collection<IMessage> warnings = warningsByFile.get(source);
            final Collection<IMessage> notes = notesByFile.get(source);
            final Collection<IMessage> ambiguities = ambiguitiesByFile.get(source);
            final Collection<IMessage> messages = Lists
                    .newArrayListWithCapacity(errors.size() + warnings.size() + notes.size() + ambiguities.size());
            messages.addAll(errors);
            messages.addAll(warnings);
            messages.addAll(notes);
            messages.addAll(ambiguities);
            if (changed.containsKey(source)) {
                results.add(unitService.analyzeUnit(changed.get(source),
                        new AnalyzeContrib(true, errors.isEmpty(), true, astsByFile.get(source), messages, -1),
                        context));
            } else {
                FileObject file = resourceService.resolve(source);
                updateResults.add(unitService.analyzeUnitUpdate(file, new AnalyzeUpdateData(messages), context));
            }
        }

        return new SpoofaxAnalyzeResults(results, updateResults, context);
    }

}
