package org.metaborg.spoofax.core.user_definable;

import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.context.IContext;
import org.metaborg.spoofax.core.semantic_provider.IBuilderInput;
import org.metaborg.spoofax.core.unit.TransformOutput;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface ITransformer {

    IStrategoTerm transform(IContext context, IBuilderInput inputTerm, FileObject location, List<TransformOutput> outputs);
}
