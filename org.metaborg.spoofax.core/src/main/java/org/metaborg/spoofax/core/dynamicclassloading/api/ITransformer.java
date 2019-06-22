package org.metaborg.spoofax.core.dynamicclassloading.api;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.context.IContext;
import org.metaborg.spoofax.core.dynamicclassloading.BuilderInput;
import org.metaborg.spoofax.core.unit.TransformOutput;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.List;

public interface ITransformer {
    IStrategoTerm transform(IContext context, BuilderInput inputTerm, FileObject location, List<TransformOutput> outputs);
    interface Generated extends ITransformer {}
}
