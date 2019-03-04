package org.metaborg.spoofax.core.action;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.action.ITransformGoal;
import org.metaborg.core.action.TransformActionFlags;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.transform.TransformException;
import org.metaborg.spoofax.core.dynamicclassloading.IBuilderInput;
import org.metaborg.spoofax.core.dynamicclassloading.IDynamicClassLoadingService;
import org.metaborg.spoofax.core.dynamicclassloading.api.ITransformer;
import org.metaborg.spoofax.core.transform.ISpoofaxTransformAction;
import org.metaborg.spoofax.core.transform.TransformResult;
import org.metaborg.spoofax.core.unit.TransformOutput;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.time.Timer;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class JavaTransformAction implements ISpoofaxTransformAction {
    private static final ILogger logger = LoggerUtils.logger(JavaTransformAction.class);

    public final String name;
    public final ITransformGoal goal;
    public final TransformActionFlags flags;
    public final String className;

    private @Inject IDynamicClassLoadingService semanticProviderService;

    public JavaTransformAction(String name, ITransformGoal goal, TransformActionFlags flags, String className) {
        this.name = name;
        this.goal = goal;
        this.flags = flags;
        this.className = className;
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
        ILanguageComponent component, IBuilderInput inputTerm) throws TransformException {
        final ITransformer transformer;
        try {
            transformer = semanticProviderService.loadClass(component, className, ITransformer.class);
        } catch(MetaborgException e) {
            throw new TransformException(e.getMessage(), e.getCause());
        }

        logger.debug("Transforming {} with '{}'", source, name);

        final List<TransformOutput> outputs = new ArrayList<>();

        final Timer timer = new Timer(true);
        final IStrategoTerm resultTerm = transformer.transform(context, inputTerm, location, outputs);
        final long duration = timer.stop();

        return new TransformResult(duration, outputs, resultTerm);
    }
}
