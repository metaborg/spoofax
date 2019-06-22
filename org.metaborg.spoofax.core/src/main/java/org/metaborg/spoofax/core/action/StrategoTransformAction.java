package org.metaborg.spoofax.core.action;

import com.google.common.collect.Lists;
import com.google.inject.assistedinject.Assisted;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.action.ITransformGoal;
import org.metaborg.core.action.TransformActionFlags;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.transform.TransformException;
import org.metaborg.spoofax.core.dynamicclassloading.BuilderInput;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.transform.ISpoofaxTransformAction;
import org.metaborg.spoofax.core.transform.TransformResult;
import org.metaborg.spoofax.core.unit.TransformOutput;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.time.Timer;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.strategoxt.HybridInterpreter;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class StrategoTransformAction implements ISpoofaxTransformAction {
    private static final ILogger logger = LoggerUtils.logger(StrategoTransformAction.class);

    public final String name;
    public final ITransformGoal goal;
    public final TransformActionFlags flags;
    public final String strategy;

    private final IResourceService resourceService;
    private final IStrategoRuntimeService strategoRuntimeService;
    private final IStrategoCommon common;


    @Inject public StrategoTransformAction(IResourceService resourceService,
        IStrategoRuntimeService strategoRuntimeService, IStrategoCommon common, @Assisted("name") String name,
        @Assisted ITransformGoal goal, @Assisted TransformActionFlags flags, @Assisted("strategy") String strategy) {
        this.resourceService = resourceService;
        this.strategoRuntimeService = strategoRuntimeService;
        this.common = common;
        this.name = name;
        this.goal = goal;
        this.flags = flags;
        this.strategy = strategy;
    }


    @Override public String name() {
        return name;
    }

    @Override public ITransformGoal goal() {
        return goal;
    }

    @Override public TransformActionFlags flags() {
        return flags;
    }

    @Override public String toString() {
        return name;
    }


    @Override public TransformResult transform(IContext context, FileObject source, FileObject location,
        ILanguageComponent component, BuilderInput inputTerm) throws TransformException {
        // Get Stratego runtime
        final HybridInterpreter runtime;
        try {
            runtime = strategoRuntimeService.runtime(component, context, true);
        } catch(MetaborgException e) {
            throw new TransformException("Transformation failed unexpectedly; cannot get Stratego interpreter", e);
        }

        // Transform
        logger.debug("Transforming {} with '{}'", source, name);
        final Timer timer = new Timer(true);
        final IStrategoTerm outputTerm;
        try {
            outputTerm = common.invoke(runtime, inputTerm, strategy);
        } catch(MetaborgException e) {
            throw new TransformException(e.getMessage(), e.getCause());
        }
        final long duration = timer.stop();
        if(outputTerm == null) {
            final String message = logger.format("Invoking Stratego strategy {} failed", strategy);
            throw new TransformException(message);
        }

        // Get the result and, if allowed and required, write to file
        List<TransformOutput> outputs;
        IStrategoTerm resultTerm;
        if(outputTerm.getSubtermCount() == 2 && (outputTerm instanceof IStrategoTuple)) {
            final IStrategoTerm resourceTerm = outputTerm.getSubterm(0);
            final IStrategoTerm contentTerm = outputTerm.getSubterm(1);
            try {
                if(resourceTerm instanceof IStrategoString) {
                    resultTerm = contentTerm;
                    outputs = Lists.newArrayList(output(resourceTerm, contentTerm, location));
                } else if(resourceTerm instanceof IStrategoList) {
                    if(!(contentTerm instanceof IStrategoList)
                        || resourceTerm.getSubtermCount() != contentTerm.getSubtermCount()) {
                        logger.error("List of terms does not match list of file names, cannot write to file.");
                        resultTerm = null;
                        outputs = Collections.emptyList();
                    } else {
                        outputs = Lists.newArrayListWithExpectedSize(resourceTerm.getSubtermCount());
                        for(int i = 0; i < resourceTerm.getSubtermCount(); i++) {
                            outputs.add(output(resourceTerm.getSubterm(i), contentTerm.getSubterm(i), location));
                        }
                        resultTerm = resourceTerm.getSubtermCount() == 1 ? resourceTerm.getSubterm(0) : null;
                    }
                } else {
                    logger.error(
                        "First term of result tuple {} is neither a string, nor a list, cannot write output file",
                        resourceTerm);
                    resultTerm = null;
                    outputs = Collections.emptyList();
                }
            } catch(MetaborgException ex) {
                resultTerm = null;
                outputs = Collections.emptyList();
            }
        } else {
            resultTerm = outputTerm;
            outputs = Collections.emptyList();
        }

        return new TransformResult(duration, outputs, resultTerm);
    }

    private TransformOutput output(IStrategoTerm resourceTerm, IStrategoTerm contentTerm, FileObject location)
        throws MetaborgException {
        if(!(resourceTerm instanceof IStrategoString)) {
            throw new MetaborgException("First term of result tuple {} is not a string, cannot write output file");
        } else {
            final String resourceString = Tools.asJavaString(resourceTerm);
            FileObject output = resourceService.resolve(location, resourceString);
            return new TransformOutput(resourceString, output, contentTerm);
        }
    }
}
