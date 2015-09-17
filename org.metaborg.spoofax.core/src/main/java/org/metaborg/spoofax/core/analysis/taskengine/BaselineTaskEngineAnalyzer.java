package org.metaborg.spoofax.core.analysis.taskengine;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.analysis.AnalysisResult;
import org.metaborg.core.analysis.IAnalyzerData;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzer;
import org.metaborg.spoofax.core.analysis.SpoofaxAnalysisFacet;
import org.metaborg.spoofax.core.analysis.SpoofaxAnalysisCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.stratego.StrategoLocalPath;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeUtils;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

/**
 * Analyzer for NaBL + TS + index + task engine projects, older version for keeping the baseline working. Calls the
 * analysis strategy with a list of all inputs.
 */
public class BaselineTaskEngineAnalyzer implements ISpoofaxAnalyzer {
    public static final String name = "baseline-taskengine";

    private static final Logger logger = LoggerFactory.getLogger(BaselineTaskEngineAnalyzer.class);

    private final IResourceService resourceService;
    private final ITermFactoryService termFactoryService;
    private final IStrategoRuntimeService runtimeService;

    private final StrategoLocalPath localPath;

    private final IStrategoConstructor fileCons;


    @Inject public BaselineTaskEngineAnalyzer(IResourceService resourceService, ITermFactoryService termFactoryService,
        IStrategoRuntimeService runtimeService, StrategoLocalPath localPath) {
        this.resourceService = resourceService;
        this.termFactoryService = termFactoryService;
        this.runtimeService = runtimeService;
        this.localPath = localPath;

        this.fileCons = termFactoryService.getGeneric().makeConstructor("File", 3);
    }


    @Override public AnalysisResult<IStrategoTerm, IStrategoTerm> analyze(Iterable<ParseResult<IStrategoTerm>> inputs,
        IContext context) throws AnalysisException {
        final ILanguageImpl language = context.language();
        final ITermFactory termFactory = termFactoryService.getGeneric();

        final FacetContribution<SpoofaxAnalysisFacet> facetContribution =
            language.facetContribution(SpoofaxAnalysisFacet.class);
        if(facetContribution == null) {
            logger.debug("No analysis required for {}", language);
            return new AnalysisResult<IStrategoTerm, IStrategoTerm>(context);
        }
        final SpoofaxAnalysisFacet facet = facetContribution.facet;

        final HybridInterpreter interpreter;
        try {
            interpreter = runtimeService.runtime(facetContribution.contributor, context);
        } catch(MetaborgException e) {
            throw new AnalysisException(context, "Failed to get Stratego interpreter", e);
        }

        return analyze(inputs, context, interpreter, facet.strategyName, termFactory);
    }

    private AnalysisResult<IStrategoTerm, IStrategoTerm> analyze(Iterable<ParseResult<IStrategoTerm>> inputs,
        IContext context, HybridInterpreter interpreter, String analysisStrategy, ITermFactory termFactory)
        throws AnalysisException {
        final FileObject contextLocation = context.location();
        final File localContextLocation;
        try {
            localContextLocation = resourceService.localFile(contextLocation);
        } catch(MetaborgRuntimeException e) {
            final String message = String.format("Context location %s does not exist, cannot analyze", contextLocation);
            logger.error(message);
            throw new AnalysisException(context, message, e);
        }

        logger.trace("Creating input terms for analysis (File/3 terms)");
        final Collection<IStrategoAppl> analysisInputs = Lists.newLinkedList();
        final Map<String, FileObject> originalSources = Maps.newHashMap();
        for(ParseResult<IStrategoTerm> input : inputs) {
            if(input.result == null) {
                logger.warn("Input result for {} is null, cannot analyze it", input.source);
                continue;
            }

            final FileObject resource = input.source;
            final File localResource;
            try {
                if(input.source.exists()) {
                    localResource = resourceService.localFile(resource);
                } else {
                    localResource = resourceService.localPath(resource);
                }
                if(localResource == null) {
                    logger.error(
                        "Input {} does not exist, and cannot reside on the local file system, cannot analyze it",
                        resource);
                    continue;
                }
            } catch(FileSystemException e) {
                logger.error("Cannot determine if input {} exists, cannot analyze it", resource);
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
        } catch(MetaborgException e) {
            final String message = SpoofaxAnalysisCommon.analysisFailedMessage(interpreter);
            logger.error(message, e);
            throw new AnalysisException(context, message, e);
        }
        if(resultTerm == null) {
            final String message = SpoofaxAnalysisCommon.analysisFailedMessage(interpreter);
            logger.error(message);
            throw new AnalysisException(context, message);
        }
        if(!(resultTerm instanceof IStrategoAppl)) {
            final String message = String.format("Unexpected results from analysis {}", resultTerm);
            logger.error(message);
            throw new AnalysisException(context, message);
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
                result(result, context, originalSources);
            if(fileResult == null) {
                continue;
            }
            fileResults.add(fileResult);
        }

        final Collection<String> affectedPartitions = affectedPartitions(affectedPartitionsTerm);
        final AnalysisDebugResult debugResult = analysisDebugResult(debugResultTerm);
        final AnalysisTimeResult timeResult = analysisTimeResult(timeResultTerm);

        final IAnalyzerData data = new TaskEngineAnalyzerData(affectedPartitions, debugResult, timeResult);
        return new AnalysisResult<IStrategoTerm, IStrategoTerm>(context, fileResults, data);
    }

    private AnalysisFileResult<IStrategoTerm, IStrategoTerm> result(IStrategoTerm result, IContext context,
        Map<String, FileObject> originalSources) {
        final String file = Tools.asJavaString(result.getSubterm(2));
        final FileObject resource = originalSources.get(file);
        if(resource == null) {
            logger.error("Cannot find original source for {}, skipping result", file);
            return null;
        }
        final IStrategoTerm previousAst = result.getSubterm(3);
        final IStrategoTerm ast = result.getSubterm(4);
        final Collection<IMessage> messages = Sets.newHashSet();
        messages.addAll(SpoofaxAnalysisCommon.messages(resource, MessageSeverity.ERROR, result.getSubterm(5)));
        messages.addAll(SpoofaxAnalysisCommon.messages(resource, MessageSeverity.WARNING, result.getSubterm(6)));
        messages.addAll(SpoofaxAnalysisCommon.messages(resource, MessageSeverity.NOTE, result.getSubterm(7)));

        return new AnalysisFileResult<IStrategoTerm, IStrategoTerm>(ast, resource, context, messages,
            new ParseResult<IStrategoTerm>("", previousAst, resource, Arrays.asList(new IMessage[] {}), -1,
                context.language(), null, null));
    }

    private Collection<String> affectedPartitions(IStrategoTerm affectedTerm) {
        final Collection<String> affected = new ArrayList<String>(affectedTerm.getSubtermCount());
        for(IStrategoTerm partition : affectedTerm) {
            affected.add(Tools.asJavaString(partition));
        }
        return affected;
    }

    private AnalysisDebugResult analysisDebugResult(IStrategoTerm debug) {
        final IStrategoTerm collectionDebug = debug.getSubterm(0);
        return new AnalysisDebugResult(Tools.asJavaInt(collectionDebug.getSubterm(0)), Tools.asJavaInt(collectionDebug
            .getSubterm(1)), Tools.asJavaInt(collectionDebug.getSubterm(2)), Tools.asJavaInt(collectionDebug
            .getSubterm(3)), Tools.asJavaInt(collectionDebug.getSubterm(4)), (IStrategoList) debug.getSubterm(1),
            (IStrategoList) debug.getSubterm(2), (IStrategoList) debug.getSubterm(3));
    }

    private AnalysisTimeResult analysisTimeResult(IStrategoTerm time) {
        return new AnalysisTimeResult((long) Tools.asJavaDouble(time.getSubterm(0)), (long) Tools.asJavaDouble(time
            .getSubterm(1)), (long) Tools.asJavaDouble(time.getSubterm(2)), (long) Tools.asJavaDouble(time
            .getSubterm(3)), (long) Tools.asJavaDouble(time.getSubterm(4)), (long) Tools.asJavaDouble(time
            .getSubterm(5)), (long) Tools.asJavaDouble(time.getSubterm(6)));
    }
}
