package org.metaborg.spoofax.core.analysis.stratego;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.SpoofaxRuntimeException;
import org.metaborg.spoofax.core.analysis.AnalysisDebugResult;
import org.metaborg.spoofax.core.analysis.AnalysisException;
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.analysis.AnalysisResult;
import org.metaborg.spoofax.core.analysis.AnalysisTimeResult;
import org.metaborg.spoofax.core.analysis.IAnalysisService;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.messages.ISourceRegion;
import org.metaborg.spoofax.core.messages.MessageFactory;
import org.metaborg.spoofax.core.messages.MessageSeverity;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.stratego.StrategoLocalPath;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeUtils;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.core.syntax.jsglr.JSGLRSourceRegionFactory;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.util.iterators.Iterables2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.core.StackTracer;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.attachments.OriginAttachment;
import org.strategoxt.HybridInterpreter;
import org.strategoxt.imp.generator.postprocess_feedback_results_0_0;
import org.strategoxt.imp.generator.sdf2imp;
import org.strategoxt.lang.Context;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import fj.P;
import fj.P2;

public class StrategoAnalysisService implements IAnalysisService<IStrategoTerm, IStrategoTerm> {
    private static final Logger logger = LoggerFactory.getLogger(StrategoAnalysisService.class);

    private final IResourceService resourceService;
    private final ITermFactoryService termFactoryService;
    private final IStrategoRuntimeService runtimeService;

    private final StrategoLocalPath localPath;

    private final IStrategoConstructor fileCons;


    @Inject public StrategoAnalysisService(IResourceService resourceService, ITermFactoryService termFactoryService,
        IStrategoRuntimeService runtimeService, StrategoLocalPath localPath) {
        this.resourceService = resourceService;
        this.termFactoryService = termFactoryService;
        this.runtimeService = runtimeService;
        this.localPath = localPath;

        this.fileCons = termFactoryService.getGeneric().makeConstructor("File", 3);
    }


    @Override public AnalysisResult<IStrategoTerm, IStrategoTerm> analyze(Iterable<ParseResult<IStrategoTerm>> inputs,
        IContext context) throws AnalysisException {
        final ILanguage language = context.language();
        final ITermFactory termFactory = termFactoryService.getGeneric();

        final Collection<FileObject> sources = Lists.newLinkedList();
        for(ParseResult<IStrategoTerm> input : inputs) {
            sources.add(input.source);
        }

        final StrategoFacet facet = language.facet(StrategoFacet.class);
        if(facet == null) {
            final String message = String.format("Language %s does not have a Stratego facet", language);
            logger.error(message);
            throw new AnalysisException(sources, context, message);
        }

        if(facet.analysisStrategy() == null || facet.analysisMode() == null) {
            logger.debug("No analysis required for {}", language);
            return new AnalysisResult<IStrategoTerm, IStrategoTerm>(context,
                Iterables2.<AnalysisFileResult<IStrategoTerm, IStrategoTerm>>empty(), Iterables2.<String>empty(),
                new AnalysisDebugResult(termFactory), new AnalysisTimeResult());
        }

        logger.debug("Analyzing {} inputs of {}", Iterables.size(inputs), language);

        final HybridInterpreter interpreter;
        try {
            interpreter = runtimeService.runtime(context);
        } catch(SpoofaxException e) {
            throw new AnalysisException(sources, context, "Failed to get Stratego interpreter", e);
        }

        final StrategoAnalysisMode mode = facet.analysisMode();
        switch(mode) {
            case SingleAST:
                return analyzeSingleAST(inputs, sources, context, interpreter, facet.analysisStrategy(), termFactory);
            case MultiAST:
                return analyzeMultiAST(inputs, sources, context, interpreter, "analysis-cmd", termFactory);
            default: {
                final String message = String.format("Unhandled analysis mode %s", mode);
                logger.error(message);
                throw new AnalysisException(sources, context, message);
            }
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


    private AnalysisResult<IStrategoTerm, IStrategoTerm> analyzeSingleAST(Iterable<ParseResult<IStrategoTerm>> inputs,
        Iterable<FileObject> sources, IContext context, HybridInterpreter interpreter, String analysisStrategy,
        ITermFactory termFactory) throws AnalysisException {
        final FileObject contextLocation = context.location();
        final File localContextLocation;
        try {
            localContextLocation = resourceService.localFile(contextLocation);
        } catch(SpoofaxRuntimeException e) {
            final String message = String.format("Context location %s does not exist, cannot analyze", contextLocation);
            logger.error(message);
            throw new AnalysisException(sources, context, message, e);
        }

        logger.trace("Creating input terms for analysis (3-tuple terms)");
        final Collection<P2<ParseResult<IStrategoTerm>, IStrategoTuple>> analysisInputs = Lists.newLinkedList();
        for(ParseResult<IStrategoTerm> input : inputs) {
            if(input.result == null) {
                logger.warn("Input result for {} is null, cannot analyze it", input.source);
                continue;
            }

            final FileObject resource = input.source;
            final File localResource = resourceService.localPath(resource);
            if(localResource == null) {
                final String message =
                    String.format("Input %s does not reside on the local file system, cannot analyze it", resource);
                logger.error(message);
                continue;
            }

            final IStrategoString path = localPath.localResourceTerm(localResource, localContextLocation);
            final IStrategoString contextPath = localPath.localLocationTerm(localContextLocation);
            analysisInputs.add(P.p(input, termFactory.makeTuple(input.result, path, contextPath)));
        }

        final Collection<AnalysisFileResult<IStrategoTerm, IStrategoTerm>> results = Lists.newLinkedList();
        for(P2<ParseResult<IStrategoTerm>, IStrategoTuple> input : analysisInputs) {
            final ParseResult<IStrategoTerm> parseResult = input._1();
            final FileObject resource = parseResult.source;
            final IStrategoTuple inputTerm = input._2();
            try {
                logger.trace("Analysing {}", resource);
                final IStrategoTerm resultTerm = StrategoRuntimeUtils.invoke(interpreter, inputTerm, analysisStrategy);
                if(resultTerm == null) {
                    logger.trace("Analysis for {} failed", resource);
                    results.add(singleASTMakeResult(analysisFailedMessage(interpreter), parseResult, null));
                } else if(!(resultTerm instanceof IStrategoTuple)) {
                    logger.trace("Analysis for {} has unexpected result, not a tuple", resource);
                    results.add(singleASTMakeResult(String.format("Unexpected results from analysis {}", resultTerm),
                        parseResult, null));
                } else if(resultTerm.getSubtermCount() == 4) {
                    logger.trace("Analysis for {} done", resource);
                    results.add(singleASTMakeResult(resultTerm, parseResult));
                } else if(resultTerm.getSubtermCount() == 3) {
                    logger.trace("Analysis for {} done", resource);
                    results.add(singleASTMakeResultNoAST(resultTerm, parseResult));
                } else {
                    logger.trace(
                        "Analysis for {} has unexpected result, tuple with more than 4 or less than 2 elements",
                        resource);
                    results.add(singleASTMakeResult(String.format("Unexpected results from analysis {}", resultTerm),
                        parseResult, null));
                }
            } catch(SpoofaxException e) {
                logger.trace("Analysis for {} failed", resource);
                results.add(singleASTMakeResult(analysisFailedMessage(interpreter), parseResult, e));
            }
        }

        return new AnalysisResult<IStrategoTerm, IStrategoTerm>(context, results, Iterables2.<String>empty(),
            new AnalysisDebugResult(termFactory), new AnalysisTimeResult());
    }

    private AnalysisFileResult<IStrategoTerm, IStrategoTerm> singleASTMakeResult(IStrategoTerm result,
        ParseResult<IStrategoTerm> parseResult) {
        final FileObject source = parseResult.source;
        final Collection<IMessage> messages = Lists.newLinkedList();
        messages.addAll(StrategoAnalysisService.makeMessages(source, MessageSeverity.ERROR, result.getSubterm(1)));
        messages.addAll(StrategoAnalysisService.makeMessages(source, MessageSeverity.WARNING, result.getSubterm(2)));
        messages.addAll(StrategoAnalysisService.makeMessages(source, MessageSeverity.NOTE, result.getSubterm(3)));
        return new AnalysisFileResult<IStrategoTerm, IStrategoTerm>(result.getSubterm(0), source, messages, parseResult);
    }

    private AnalysisFileResult<IStrategoTerm, IStrategoTerm> singleASTMakeResultNoAST(IStrategoTerm result,
        ParseResult<IStrategoTerm> parseResult) {
        final FileObject source = parseResult.source;
        final Collection<IMessage> messages = Lists.newLinkedList();
        messages.addAll(StrategoAnalysisService.makeMessages(source, MessageSeverity.ERROR, result.getSubterm(0)));
        messages.addAll(StrategoAnalysisService.makeMessages(source, MessageSeverity.WARNING, result.getSubterm(1)));
        messages.addAll(StrategoAnalysisService.makeMessages(source, MessageSeverity.NOTE, result.getSubterm(2)));
        return new AnalysisFileResult<IStrategoTerm, IStrategoTerm>(null, source, messages, parseResult);
    }

    private AnalysisFileResult<IStrategoTerm, IStrategoTerm> singleASTMakeResult(String errorString,
        ParseResult<IStrategoTerm> parseResult, Throwable e) {
        if(e != null) {
            logger.error(errorString, e);
        } else {
            logger.error(errorString);
        }
        final FileObject source = parseResult.source;
        final IMessage message = MessageFactory.newAnalysisErrorAtTop(source, errorString, e);
        return new AnalysisFileResult<IStrategoTerm, IStrategoTerm>(null, source, Iterables2.singleton(message),
            parseResult);
    }


    private AnalysisResult<IStrategoTerm, IStrategoTerm> analyzeMultiAST(Iterable<ParseResult<IStrategoTerm>> inputs,
        Iterable<FileObject> sources, IContext context, HybridInterpreter interpreter, String analysisStrategy,
        ITermFactory termFactory) throws AnalysisException {
        final FileObject contextLocation = context.location();
        final File localContextLocation;
        try {
            localContextLocation = resourceService.localFile(contextLocation);
        } catch(SpoofaxRuntimeException e) {
            final String message = String.format("Context location %s does not exist, cannot analyze", contextLocation);
            logger.error(message);
            throw new AnalysisException(sources, context, message, e);
        }
        final ILanguage language = context.language();

        logger.trace("Creating input terms for analysis (File/3 terms)");
        final Collection<IStrategoAppl> analysisInputs = Lists.newLinkedList();
        final Map<String, FileObject> originalSources = Maps.newHashMap();
        for(ParseResult<IStrategoTerm> input : inputs) {
            if(input.result == null) {
                logger.warn("Input result for {} is null, cannot analyze it", input.source);
                continue;
            }

            final FileObject resource = input.source;
            final File localResource = resourceService.localPath(resource);
            if(localResource == null) {
                final String message =
                    String.format("Input %s does not reside on the local file system, cannot analyze it", resource);
                logger.error(message);
                continue;
            }

            final IStrategoString pathTerm = localPath.localResourceTerm(localResource, localContextLocation);
            originalSources.put(pathTerm.stringValue(), resource);
            analysisInputs.add(termFactory.makeAppl(fileCons, pathTerm, input.result,
                termFactory.makeReal(input.duration)));
        }
        final IStrategoTerm inputTerm = termFactory.makeList(analysisInputs);

        logger.trace("Invoking {} strategy", analysisStrategy);
        final IStrategoTerm resultTerm;
        try {
            resultTerm = StrategoRuntimeUtils.invoke(interpreter, inputTerm, analysisStrategy);
        } catch(SpoofaxException e) {
            final String message = analysisFailedMessage(interpreter);
            logger.error(message, e);
            throw new AnalysisException(sources, context, message, e);
        }
        if(resultTerm == null) {
            final String message = analysisFailedMessage(interpreter);
            logger.error(message);
            throw new AnalysisException(sources, context, message);
        }
        if(!(resultTerm instanceof IStrategoAppl)) {
            final String message = String.format("Unexpected results from analysis {}", resultTerm);
            logger.error(message);
            throw new AnalysisException(sources, context, message);
        }

        logger.trace("Analysis resulted in a {} term", resultTerm.getSubtermCount());
        final IStrategoTerm fileResultsTerm = resultTerm.getSubterm(0);
        final IStrategoTerm affectedPartitionsTerm = resultTerm.getSubterm(1);
        final IStrategoTerm debugResultTerm = resultTerm.getSubterm(2);
        final IStrategoTerm timeResultTerm = resultTerm.getSubterm(3);

        final int numItems = fileResultsTerm.getSubtermCount();
        logger.trace("Analysis contains {} results. Converting to analysis results", numItems);
        final Collection<AnalysisFileResult<IStrategoTerm, IStrategoTerm>> fileResults = Sets.newHashSet();
        for(IStrategoTerm result : fileResultsTerm) {
            final AnalysisFileResult<IStrategoTerm, IStrategoTerm> fileResult =
                multiASTMakeResult(result, language, originalSources);
            if(fileResult == null) {
                continue;
            }
            fileResults.add(fileResult);
        }

        final Collection<String> affectedPartitions = makeAffectedPartitions(affectedPartitionsTerm);
        final AnalysisDebugResult debugResult = makeAnalysisDebugResult(debugResultTerm);
        final AnalysisTimeResult timeResult = makeAnalysisTimeResult(timeResultTerm);

        return new AnalysisResult<IStrategoTerm, IStrategoTerm>(context, fileResults, affectedPartitions, debugResult,
            timeResult);
    }

    private AnalysisFileResult<IStrategoTerm, IStrategoTerm> multiASTMakeResult(IStrategoTerm res, ILanguage language,
        Map<String, FileObject> originalSources) {
        assert res != null;
        assert res.getSubtermCount() == 8;

        final String file = Tools.asJavaString(res.getSubterm(2));
        final FileObject resource = originalSources.get(file);
        if(resource == null) {
            logger.error("Cannot find original source for {}, skipping result", file);
            return null;
        }
        final Collection<IMessage> messages = Sets.newHashSet();
        messages.addAll(StrategoAnalysisService.makeMessages(resource, MessageSeverity.ERROR, res.getSubterm(5)));
        messages.addAll(StrategoAnalysisService.makeMessages(resource, MessageSeverity.WARNING, res.getSubterm(6)));
        messages.addAll(StrategoAnalysisService.makeMessages(resource, MessageSeverity.NOTE, res.getSubterm(7)));
        final IStrategoTerm ast = res.getSubterm(4);
        final IStrategoTerm previousAst = res.getSubterm(3);

        return new AnalysisFileResult<IStrategoTerm, IStrategoTerm>(ast, resource, messages,
            new ParseResult<IStrategoTerm>("", previousAst, resource, Arrays.asList(new IMessage[] {}), -1, language,
                null, null));
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
        return new AnalysisDebugResult(Tools.asJavaInt(collectionDebug.getSubterm(0)), Tools.asJavaInt(collectionDebug
            .getSubterm(1)), Tools.asJavaInt(collectionDebug.getSubterm(2)), Tools.asJavaInt(collectionDebug
            .getSubterm(3)), Tools.asJavaInt(collectionDebug.getSubterm(4)), (IStrategoList) debug.getSubterm(1),
            (IStrategoList) debug.getSubterm(2), (IStrategoList) debug.getSubterm(3));
    }

    private AnalysisTimeResult makeAnalysisTimeResult(IStrategoTerm time) {
        return new AnalysisTimeResult((long) Tools.asJavaDouble(time.getSubterm(0)), (long) Tools.asJavaDouble(time
            .getSubterm(1)), (long) Tools.asJavaDouble(time.getSubterm(2)), (long) Tools.asJavaDouble(time
            .getSubterm(3)), (long) Tools.asJavaDouble(time.getSubterm(4)), (long) Tools.asJavaDouble(time
            .getSubterm(5)), (long) Tools.asJavaDouble(time.getSubterm(6)));
    }


    private String analysisFailedMessage(HybridInterpreter interpreter) {
        final StackTracer stackTracer = interpreter.getContext().getStackTracer();
        return "Analysis failed\nStratego stack trace:\n" + stackTracer.getTraceString();
    }

    public static Collection<IMessage> makeMessages(FileObject file, MessageSeverity severity, IStrategoTerm msgs) {
        final Collection<IMessage> result = new ArrayList<IMessage>(msgs.getSubtermCount());

        // HACK: init sdf2shit and flatten the messages list.
        final Context context = new Context();
        sdf2imp.init(context);
        final IStrategoTerm processedMsgs = postprocess_feedback_results_0_0.instance.invoke(context, msgs);

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
                result.add(MessageFactory.newAnalysisMessage(file, region, message, severity, null));
            } else {
                result.add(MessageFactory.newAnalysisMessageAtTop(file, message, severity, null));
            }
        }

        return result;
    }

    private static ISimpleTerm minimizeMarkerSize(ISimpleTerm node) {
        // TODO: prefer lexical nodes when minimizing marker size? (e.g., not 'private')
        if(node == null)
            return null;
        while(ImploderAttachment.getLeftToken(node).getLine() < ImploderAttachment.getRightToken(node).getLine()) {
            if(node.getSubtermCount() == 0)
                break;
            node = node.getSubterm(0);
        }
        return node;
    }

    /**
     * Given a Stratego term, get the first AST node associated with any of its subterms, doing a depth-first search.
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
