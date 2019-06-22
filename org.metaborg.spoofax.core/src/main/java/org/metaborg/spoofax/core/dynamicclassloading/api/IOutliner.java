package org.metaborg.spoofax.core.dynamicclassloading.api;

import org.metaborg.core.context.IContext;
import org.metaborg.core.outline.IOutlineNode;
import org.metaborg.spoofax.core.dynamicclassloading.BuilderInput;

import javax.annotation.Nullable;

public interface IOutliner {
    @Nullable Iterable<IOutlineNode> createOutline(IContext env, BuilderInput input);
    interface Generated extends IOutliner {}
}
