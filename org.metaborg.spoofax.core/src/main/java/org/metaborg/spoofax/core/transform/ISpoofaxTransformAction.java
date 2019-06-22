package org.metaborg.spoofax.core.transform;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.transform.TransformException;
import org.metaborg.spoofax.core.dynamicclassloading.BuilderInput;

public interface ISpoofaxTransformAction extends ITransformAction {
    TransformResult transform(IContext context, FileObject source, FileObject location,
        ILanguageComponent component, BuilderInput inputTerm)
        throws TransformException;
}
