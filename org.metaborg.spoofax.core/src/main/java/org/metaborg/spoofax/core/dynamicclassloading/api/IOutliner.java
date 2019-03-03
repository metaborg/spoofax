package org.metaborg.spoofax.core.dynamicclassloading.api;

import javax.annotation.Nullable;

import org.metaborg.core.context.IContext;
import org.metaborg.core.outline.IOutlineNode;
import org.metaborg.spoofax.core.dynamicclassloading.IBuilderInput;

public interface IOutliner {
    @Nullable Iterable<IOutlineNode> createOutline(IContext env, IBuilderInput input);
}
