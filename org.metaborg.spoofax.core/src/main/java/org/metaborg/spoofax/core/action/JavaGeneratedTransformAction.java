package org.metaborg.spoofax.core.action;

import java.util.Collections;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.action.ITransformGoal;
import org.metaborg.core.action.TransformActionFlags;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.transform.TransformException;
import org.metaborg.spoofax.core.dynamicclassloading.IBuilderInput;
import org.metaborg.spoofax.core.transform.ISpoofaxTransformAction;
import org.metaborg.spoofax.core.transform.TransformResult;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

public class JavaGeneratedTransformAction implements ISpoofaxTransformAction {
    private static final ILogger logger = LoggerUtils.logger(JavaGeneratedTransformAction.class);

    public final ITransformGoal goal;
    public final TransformActionFlags flags;

    public JavaGeneratedTransformAction(ITransformGoal goal, TransformActionFlags flags) {
        this.goal = goal;
        this.flags = flags;
        logger.error("JavaGenerated is not supported in menus. ");
    }

    @Override public String name() {
        return "JavaGeneratedTransformAction";
    }

    @Override public ITransformGoal goal() {
        return goal;
    }

    @Override public TransformActionFlags flags() {
        return flags;
    }

    @Override public String toString() {
        return "JavaGeneratedTransformAction";
    }


    @Override public TransformResult transform(IContext context, FileObject source, FileObject location,
        ILanguageComponent component, IBuilderInput inputTerm) throws TransformException {
        logger.error("JavaGenerated is not supported in menus. Returning input term. ");
        return new TransformResult(0L, Collections.emptyList(), inputTerm);
    }
}
