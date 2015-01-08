package org.metaborg.spoofax.core.analysis.stratego;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.analysis.AnalysisDebugResult;
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.analysis.AnalysisResult;
import org.metaborg.spoofax.core.analysis.AnalysisTimeResult;
import org.metaborg.spoofax.core.analysis.IAnalysisService;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.messages.ISourceRegion;
import org.metaborg.spoofax.core.messages.MessageFactory;
import org.metaborg.spoofax.core.messages.MessageSeverity;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.core.syntax.jsglr.JSGLRSourceRegionFactory;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.attachments.OriginAttachment;
import org.strategoxt.HybridInterpreter;
import org.strategoxt.imp.generator.postprocess_feedback_results_0_0;
import org.strategoxt.imp.generator.sdf2imp;
import org.strategoxt.lang.Context;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class StrategoAnalysisService implements IAnalysisService<IStrategoTerm, IStrategoTerm> {
    private static final Logger logger = LogManager.getLogger(StrategoAnalysisService.class);

    private final static String ANALYSIS_CRASHED_MSG = "Analysis failed";

    private final IResourceService resourceService;
    private final ITermFactoryService termFactoryService;
    private final IStrategoRuntimeService runtimeService;

    @Inject public StrategoAnalysisService(IResourceService resourceService,
        ITermFactoryService termFactoryService, IStrategoRuntimeService runtimeService) {
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
            if(input.result == null) {
                // TODO: this should be handled in a more verbose way.
                continue;
            }
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

    @Override public @Nullable IStrategoTerm origin(IStrategoTerm analyzed) {
        return OriginAttachment.getOrigin(analyzed);
    }

    @Override public @Nullable ISourceRegion region(IStrategoTerm analyzed) {
        final IStrategoTerm origin = origin(analyzed);
        if(origin == null)
            return null;
        final IToken left = ImploderAttachment.getLeftToken(origin);
        final IToken right = ImploderAttachment.getRightToken(origin);
        if(left == null || right == null)
            return null;
        return JSGLRSourceRegionFactory.fromTokens(left, right);
    }


    private AnalysisFileResult<IStrategoTerm, IStrategoTerm> makeAnalysisFileResult(IStrategoTerm res,
        ILanguage language) {
        assert res != null;
        assert res.getSubtermCount() == 8;

        FileObject file = resourceService.resolve(((IStrategoString) res.getSubterm(2)).stringValue());
        Collection<IMessage> messages = Sets.newHashSet();
        messages.addAll(StrategoAnalysisService.makeMessages(file, MessageSeverity.ERROR,
            (IStrategoList) res.getSubterm(5)));
        messages.addAll(StrategoAnalysisService.makeMessages(file, MessageSeverity.WARNING,
            (IStrategoList) res.getSubterm(6)));
        messages.addAll(StrategoAnalysisService.makeMessages(file, MessageSeverity.NOTE,
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


    public static Collection<IMessage> makeMessages(FileObject file, MessageSeverity severity,
        IStrategoList msgs) {
        final Collection<IMessage> result = new ArrayList<IMessage>(msgs.getSubtermCount());

        final Context context = new Context();
        sdf2imp.init(context);
        final IStrategoList processedMsgs =
            (IStrategoList) postprocess_feedback_results_0_0.instance.invoke(context, msgs);

        for(IStrategoTerm msg : processedMsgs.getAllSubterms()) {
            final IStrategoTerm term;
            final String message;
            if(Tools.isTermTuple(msg) && msg.getSubtermCount() == 2) {
                term = Tools.termAt(msg, 0);
                IStrategoString messageTerm = Tools.termAt(msg, 1);
                message = messageTerm.stringValue();
            } else {
                term = msg;
                message = msg.toString() + " (no tree node indicated)";
            }

            final ISimpleTerm node = minimizeMarkerSize(getClosestAstNode(term));

            if(node != null) {
                final IToken left = ImploderAttachment.getLeftToken(node);
                final IToken right = ImploderAttachment.getRightToken(node);
                final ISourceRegion region = JSGLRSourceRegionFactory.fromTokens(left, right);
                result.add(MessageFactory.newAnalysisMessage(file, region, message, severity));
            } else {
                result.add(MessageFactory.newAnalysisMessageAtTop(file, message, severity));
            }
        }

        return result;
    }

    private static ISimpleTerm minimizeMarkerSize(ISimpleTerm node) {
        // TODO: prefer lexical nodes when minimizing marker size? (e.g., not 'private')
        if(node == null)
            return null;
        while(ImploderAttachment.getLeftToken(node).getLine() < ImploderAttachment.getRightToken(node)
            .getLine()) {
            if(node.getSubtermCount() == 0)
                break;
            node = node.getSubterm(0);
        }
        return node;
    }

    /**
     * Given a Stratego term, get the first AST node associated with any of its subterms, doing a depth-first
     * search.
     */
    private static ISimpleTerm getClosestAstNode(IStrategoTerm term) {
        if(ImploderAttachment.hasImploderOrigin(term)) {
            return OriginAttachment.tryGetOrigin(term);
        } else if(term == null) {
            return null;
        } else {
            for(int i = 0; i < term.getSubtermCount(); i++) {
                ISimpleTerm result = getClosestAstNode(Tools.termAt(term, i));
                if(result != null)
                    return result;
            }
            return null;
        }
    }
}
