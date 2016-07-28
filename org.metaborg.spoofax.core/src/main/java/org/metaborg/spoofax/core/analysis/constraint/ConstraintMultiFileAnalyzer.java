package org.metaborg.spoofax.core.analysis.constraint;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.analysis.AnalysisCommon;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzer;
import org.metaborg.spoofax.core.analysis.SpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.context.scopegraph.IScopeGraphContext;
import org.metaborg.spoofax.core.context.scopegraph.IScopeGraphUnit;
import org.metaborg.spoofax.core.context.scopegraph.ScopeGraphUnit;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.stratego.primitives.scopegraph.ASTIndex;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.tracing.ISpoofaxTracingService;
import org.metaborg.spoofax.core.unit.AnalyzeContrib;
import org.metaborg.spoofax.core.unit.AnalyzeUpdateData;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;

public class ConstraintMultiFileAnalyzer extends AbstractConstraintAnalyzer implements ISpoofaxAnalyzer {
    public static final String name = "constraint-multifile";

    private final ISpoofaxUnitService unitService;
    private final IResourceService resourceService;

    @Inject public ConstraintMultiFileAnalyzer(
            final AnalysisCommon analysisCommon,
            final ISpoofaxUnitService unitService,
            final IResourceService resourceService,
            final IStrategoRuntimeService runtimeService,
            final IStrategoCommon strategoCommon,
            final ITermFactoryService termFactoryService,
            final ISpoofaxTracingService tracingService
    ) {
        super(analysisCommon, runtimeService, strategoCommon, termFactoryService, tracingService);
        this.resourceService = resourceService;
        this.unitService = unitService;
    }


    @Override
    protected ISpoofaxAnalyzeResults analyzeAll( Map<String,ISpoofaxParseUnit> changed,
            Map<String,ISpoofaxParseUnit> removed, IScopeGraphContext context,
            HybridInterpreter runtime, String strategy) throws AnalysisException {

        String globalSource = context.location().getName().getURI();
        IScopeGraphUnit global = context.unit(globalSource);
        ASTIndex globalIndex = new ASTIndex(globalSource, defaultIndex);
        if(global == null) {
            global = new ScopeGraphUnit(globalSource, null);
            context.addUnit(global);
            IStrategoTerm globalTerm = makeAppl("Global", new IStrategoTerm[0],
                    termFactory.makeList(globalIndex.toTerm(termFactory)));
            global.setConstraint(initialize(globalSource, globalTerm, strategy, context, runtime));
        }
        IStrategoTerm globalParams = global.metadata(defaultIndex, paramsKey);
        if(globalParams == null) {
            throw new AnalysisException(context, "Initial parameters missing.");
        }
        IStrategoTerm globalType = global.metadata(defaultIndex, typeKey);
        IStrategoTerm fullParams = globalType == null ?
                globalParams : termFactory.makeTuple(globalParams, globalType);
 
        for(String input : removed.keySet()) {
            context.removeUnit(input);
        }

        final Map<String,IStrategoTerm> astsByFile = Maps.newHashMap();
        final Multimap<String,IMessage> ambiguitiesByFile = HashMultimap.create();
        for(Map.Entry<String,ISpoofaxParseUnit> input : changed.entrySet()) {
            String source = input.getKey();
            ISpoofaxParseUnit parseUnit = input.getValue();
            IScopeGraphUnit unit = new ScopeGraphUnit(source, parseUnit);
            context.addUnit(unit);
            IStrategoTerm desugared = preprocessAST(source, parseUnit.ast(), strategy,
                    context, runtime);
            IStrategoTerm indexed = indexAST(source, desugared, strategy, context, runtime);
            IStrategoTerm fileConstraint = generateConstraint(source, indexed,
                    fullParams, strategy, context, runtime);
            astsByFile.put(source, indexed);
            ambiguitiesByFile.putAll(source, analysisCommon.ambiguityMessages(
                    parseUnit.source(), parseUnit.ast()));
            unit.setConstraint(fileConstraint);
        }

        final Collection<IStrategoTerm> constraints = Lists.newArrayList();
        for(IScopeGraphUnit unit : context.units()) {
            constraints.add(unit.constraint());
        }
        IStrategoTerm constraint = normalizeConstraint(conj(constraints), strategy, context, runtime);
        IStrategoTerm result = solveConstraint(constraint, strategy, context, runtime);
        global.setAnalysis(result.getSubterm(3));

        Multimap<String,IMessage> errorsByFile = messages(result.getSubterm(0), MessageSeverity.ERROR);
        Multimap<String,IMessage> warningsByFile = messages(result.getSubterm(1), MessageSeverity.WARNING);
        Multimap<String,IMessage> notesByFile = messages(result.getSubterm(2), MessageSeverity.NOTE);

        final Collection<ISpoofaxAnalyzeUnit> results =
            Lists.newArrayList();
        final Collection<ISpoofaxAnalyzeUnitUpdate> updateResults =
            Lists.newArrayList();
        for(IScopeGraphUnit unit : context.units()) {
            final String source = unit.source();
            final Collection<IMessage> errors = errorsByFile.get(source);
            final Collection<IMessage> warnings = warningsByFile.get(source);
            final Collection<IMessage> notes = notesByFile.get(source);
            final Collection<IMessage> ambiguities = ambiguitiesByFile.get(source);
            final Collection<IMessage> messages =
                Lists.newArrayListWithCapacity(errors.size() + warnings.size() + notes.size() + ambiguities.size());
            messages.addAll(errors);
            messages.addAll(warnings);
            messages.addAll(notes);
            messages.addAll(ambiguities);
            if(changed.containsKey(source)) {
                results.add(unitService.analyzeUnit(
                        changed.get(source),
                        new AnalyzeContrib(true, errors.isEmpty(), true, astsByFile.get(source), messages, -1), context));
            } else {
                // GTODO : set success value here too
                FileObject file = null;
                if(unit.parseUnit() != null) {
                    file = unit.parseUnit().source();
                }
                if(file == null) {
                    file = resourceService.resolve(source);
                }
                updateResults.add(unitService.analyzeUnitUpdate(file, new AnalyzeUpdateData(messages), context));
            }
        }

        return new SpoofaxAnalyzeResults(results, updateResults, context);
    }

}
