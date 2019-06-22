package org.metaborg.spoofax.core.outline;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.IFacet;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.outline.IOutline;
import org.metaborg.spoofax.core.dynamicclassloading.BuilderInput;

public interface IOutlineFacet extends IFacet {
    int getExpansionLevel();

    @Override default Class<? extends IOutlineFacet> getKey() {
        return IOutlineFacet.class;
    }

    IOutline createOutline(FileObject source, IContext context, ILanguageComponent contributor, BuilderInput input)
        throws MetaborgException;
}