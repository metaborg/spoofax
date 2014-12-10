package org.metaborg.spoofax.core.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.vfs2.FileObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.messages.MessageHelper;
import org.metaborg.spoofax.core.messages.MessageSeverity;
import org.metaborg.spoofax.core.parser.ParseResult;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.service.stratego.StrategoFacet;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class AnalysisService implements IAnalysisService<IStrategoTerm, IStrategoTerm> {
    private static final Logger logger = LogManager.getLogger(AnalysisService.class);

    private final static String ANALYSIS_CRASHED_MSG = "Analysis failed";

    private final IResourceService resourceService;
    private final ITermFactoryService termFactoryService;
    private final IStrategoRuntimeService runtimeService;

    @Inject public AnalysisService(IResourceService resourceService, ITermFactoryService termFactoryService,
        IStrategoRuntimeService runtimeService) {
        this.resourceService = resourceService;
        this.termFactoryService = termFactoryService;
        this.runtimeService = runtimeService;
    }

    @Override public AnalysisResult<IStrategoTerm, IStrategoTerm> analyze(
        Iterable<ParseResult<IStrategoTerm>> inputs, ILanguage language) throws SpoofaxException {
        logger.debug("Analyzing {} files of the {} language", Iterables.size(inputs), language.name());
        final ITermFactory termFactory = termFactoryService.get(language);
        final HybridInterpreter runtime = runtimeService.getRuntime(language);
        assert runtime != null;

        logger.trace("Creating input terms for analysis (File/2 terms)");
        IStrategoConstructor file_3_constr = termFactory.makeConstructor("File", 3);
        Collection<IStrategoAppl> analysisInput = new LinkedList<IStrategoAppl>();
        for(ParseResult<IStrategoTerm> input : inputs) {
            IStrategoString filename = termFactory.makeString(input.source.getName().getPath());
            analysisInput.add(termFactory.makeAppl(file_3_constr, filename, input.result,
                termFactory.makeReal(-1.0)));
        }

        final IStrategoList inputTerm = termFactory.makeList(analysisInput);
        runtime.setCurrent(inputTerm);

        logger.trace("Input term set to {}", inputTerm);

        try {
            final String function = language.facet(StrategoFacet.class).analysisStrategy();
            logger.debug("Invoking analysis strategy {}", function);
            boolean success = runtime.invoke(function);
            logger.debug("Analysis completed with success: {}", success);
            if(!success) {
                throw new SpoofaxException(ANALYSIS_CRASHED_MSG);
            } else {
                if(!(runtime.current() instanceof IStrategoAppl)) {
                    logger.fatal("Unexpected results from analysis {}", runtime.current());
                    throw new SpoofaxException("Unexpected results from analysis: " + runtime.current());
                }
                final IStrategoTerm resultTerm = runtime.current();
                logger.trace("Analysis resulted in a {} term", resultTerm.getSubtermCount());

                final IStrategoTerm fileResultsTerm = resultTerm.getSubterm(0);
                final IStrategoTerm affectedPartitionsTerm = resultTerm.getSubterm(1);
                final IStrategoTerm debugResultTerm = resultTerm.getSubterm(2);
                final IStrategoTerm timeResultTerm = resultTerm.getSubterm(3);

                final int numItems = fileResultsTerm.getSubtermCount();
                logger.trace("Analysis contains {} results. Marshalling to analysis results.", numItems);
                final Collection<AnalysisFileResult<IStrategoTerm, IStrategoTerm>> fileResults =
                    Sets.newHashSet();
                for(IStrategoTerm result : fileResultsTerm) {
                    fileResults.add(makeAnalysisFileResult(result, language));
                }

                final Collection<String> affectedPartitions = makeAffectedPartitions(affectedPartitionsTerm);
                final AnalysisDebugResult debugResult = makeAnalysisDebugResult(debugResultTerm);
                final AnalysisTimeResult timeResult = makeAnalysisTimeResult(timeResultTerm);

                logger.debug("Analysis done");

                return new AnalysisResult<IStrategoTerm, IStrategoTerm>(language, fileResults,
                    affectedPartitions, debugResult, timeResult);
            }
        } catch(InterpreterException interpex) {
            throw new SpoofaxException(ANALYSIS_CRASHED_MSG, interpex);
        }
    }

    private AnalysisFileResult<IStrategoTerm, IStrategoTerm> makeAnalysisFileResult(IStrategoTerm res,
        ILanguage language) {
        assert res != null;
        assert res.getSubtermCount() == 8;

        FileObject file = resourceService.resolve(((IStrategoString) res.getSubterm(2)).stringValue());
        Collection<IMessage> messages = Sets.newHashSet();
        messages.addAll(MessageHelper.makeMessages(file, MessageSeverity.ERROR,
            (IStrategoList) res.getSubterm(5)));
        messages.addAll(MessageHelper.makeMessages(file, MessageSeverity.WARNING,
            (IStrategoList) res.getSubterm(6)));
        messages.addAll(MessageHelper.makeMessages(file, MessageSeverity.NOTE,
            (IStrategoList) res.getSubterm(7)));
        IStrategoTerm ast = res.getSubterm(4);
        IStrategoTerm previousAst = res.getSubterm(3);

        return new AnalysisFileResult<IStrategoTerm, IStrategoTerm>(new ParseResult<IStrategoTerm>(
            previousAst, file, Arrays.asList(new IMessage[] {}), -1, language), file, messages, ast);
    }

    private Collection<String> makeAffectedPartitions(IStrategoTerm affectedTerm) {
        final Collection<String> affected = new ArrayList<String>(affectedTerm.getSubtermCount());
        for(IStrategoTerm partition : affectedTerm) {
            affected.add(Tools.asJavaString(partition));
        }
        return affected;
    }

    private AnalysisDebugResult makeAnalysisDebugResult(IStrategoTerm debug) {
        final IStrategoTerm collectionDebug = debug.getSubterm(0);
        return new AnalysisDebugResult(Tools.asJavaInt(collectionDebug.getSubterm(0)),
            Tools.asJavaInt(collectionDebug.getSubterm(1)), Tools.asJavaInt(collectionDebug.getSubterm(2)),
            Tools.asJavaInt(collectionDebug.getSubterm(3)), Tools.asJavaInt(collectionDebug.getSubterm(4)),
            (IStrategoList) debug.getSubterm(1), (IStrategoList) debug.getSubterm(2),
            (IStrategoList) debug.getSubterm(3));
    }

    private AnalysisTimeResult makeAnalysisTimeResult(IStrategoTerm time) {
        return new AnalysisTimeResult((long) Tools.asJavaDouble(time.getSubterm(0)),
            (long) Tools.asJavaDouble(time.getSubterm(1)), (long) Tools.asJavaDouble(time.getSubterm(2)),
            (long) Tools.asJavaDouble(time.getSubterm(3)), (long) Tools.asJavaDouble(time.getSubterm(4)),
            (long) Tools.asJavaDouble(time.getSubterm(5)), (long) Tools.asJavaDouble(time.getSubterm(6)));
    }
}
