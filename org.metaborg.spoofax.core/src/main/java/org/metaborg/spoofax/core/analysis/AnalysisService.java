package org.metaborg.spoofax.core.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
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

import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class AnalysisService {
    private static final Logger logger = LogManager.getLogger(AnalysisService.class);

    private final static String ANALYSIS_CRASHED_MSG = "Analysis failed";

    private final IResourceService resourceService;
    private final ILanguageIdentifierService languageIdentifierService;
    private final ITermFactoryService termFactoryService;
    private final IStrategoRuntimeService runtimeService;

    @Inject public AnalysisService(IResourceService resourceService,
        ILanguageIdentifierService languageIdentifierService, ITermFactoryService termFactoryService,
        IStrategoRuntimeService runtimeService) {
        this.resourceService = resourceService;
        this.languageIdentifierService = languageIdentifierService;
        this.termFactoryService = termFactoryService;
        this.runtimeService = runtimeService;
    }

    /**
     * Run the analysis on the given files. The analysis is started on all files on a per-language basis.
     * 
     * @see #analyze(File)
     * @param inputs
     * @throws SpoofaxException
     */
    public Collection<AnalysisResult> analyze(Collection<ParseResult<IStrategoTerm>> inputs)
        throws SpoofaxException {
        logger.debug("Analyzing {} files", inputs.size());
        Map<ILanguage, Collection<ParseResult<IStrategoTerm>>> lang2files =
            new HashMap<ILanguage, Collection<ParseResult<IStrategoTerm>>>();
        for(ParseResult<IStrategoTerm> input : inputs) {
            final FileObject file = input.source;
            final ILanguage lang = languageIdentifierService.identify(file);
            if(lang2files.get(lang) == null) {
                lang2files.put(lang, new LinkedList<ParseResult<IStrategoTerm>>());
            }
            lang2files.get(lang).add(input);
        }
        logger.trace("Files grouped in {} languages", lang2files.size());
        final Collection<AnalysisResult> results = new HashSet<AnalysisResult>();
        for(ILanguage lang : lang2files.keySet()) {
            results.add(analyze(lang, lang2files.get(lang)));
        }
        return results;
    }

    private AnalysisResult analyze(ILanguage lang, Collection<ParseResult<IStrategoTerm>> inputs)
        throws SpoofaxException {
        logger.debug("Analyzing {} files of the {} language", inputs.size(), lang.name());
        final ITermFactory termFactory = termFactoryService.get(lang);
        final HybridInterpreter runtime = runtimeService.getRuntime(lang);
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
            final String function = lang.facet(StrategoFacet.class).analysisStrategy();
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
                final Collection<AnalysisFileResult> fileResults = new HashSet<AnalysisFileResult>();
                for(IStrategoTerm result : fileResultsTerm) {
                    fileResults.add(makeAnalysisFileResult(result));
                }

                final Collection<String> affectedPartitions = makeAffectedPartitions(affectedPartitionsTerm);
                final AnalysisDebugResult debugResult = makeAnalysisDebugResult(debugResultTerm);
                final AnalysisTimeResult timeResult = makeAnalysisTimeResult(timeResultTerm);

                logger.debug("Analysis done");

                return new AnalysisResult(lang, fileResults, affectedPartitions, debugResult, timeResult);
            }
        } catch(InterpreterException interpex) {
            throw new SpoofaxException(ANALYSIS_CRASHED_MSG, interpex);
        }
    }

    private AnalysisFileResult makeAnalysisFileResult(IStrategoTerm res) {
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

        return new AnalysisFileResult(new ParseResult<IStrategoTerm>(previousAst, file,
            Arrays.asList(new IMessage[] {}), -1), file, messages, ast);
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
