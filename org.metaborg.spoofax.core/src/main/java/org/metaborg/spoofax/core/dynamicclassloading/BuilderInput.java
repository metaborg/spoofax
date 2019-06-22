package org.metaborg.spoofax.core.dynamicclassloading;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.util.resource.ResourceUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.StrategoString;
import org.spoofax.terms.StrategoTuple;
import org.spoofax.terms.TermFactory;

public class BuilderInput extends StrategoTuple implements IBuilderInput<IStrategoTerm, IStrategoTerm> {
    private final IStrategoTerm selection;
    private final IStrategoTerm position;
    private final IStrategoTerm ast;
    private final @Nullable FileObject resource;
    private final @Nullable FileObject location;

    @SuppressWarnings("deprecation")
    public BuilderInput(IStrategoTerm selection, IStrategoTerm position, IStrategoTerm ast, @Nullable FileObject resource,
        @Nullable FileObject location) {
        super(new IStrategoTerm[] { selection, position, ast,
                    new StrategoString(resourceString(resource, location), TermFactory.EMPTY_LIST, IStrategoTerm.IMMUTABLE),
                    new StrategoString(locationString(location), TermFactory.EMPTY_LIST, IStrategoTerm.IMMUTABLE) }, 
                TermFactory.EMPTY_LIST,
                IStrategoTerm.IMMUTABLE);

        this.selection = selection;
        this.position = position;
        this.ast = ast;
        this.resource = resource;
        this.location = location;
    }

    private static String resourceString(@Nullable FileObject resource, @Nullable FileObject location) {
        if(resource != null && location != null) {
            return ResourceUtils.relativeName(resource.getName(), location.getName(), false);
        } else if(resource != null) {
            return resource.getName().getURI();
        } else {
            return "";
        }
    }

    private static String locationString(@Nullable FileObject location) {
        return location == null ? "" : location.getName().getURI();
    }

    /* (non-Javadoc)
     * @see org.metaborg.spoofax.core.dynamicclassloading.IBuilderInput#getSelection()
     */
    @Override
    public IStrategoTerm getSelection() {
        return selection;
    }

    /* (non-Javadoc)
     * @see org.metaborg.spoofax.core.dynamicclassloading.IBuilderInput#getPosition()
     */
    @Override
    public IStrategoTerm getPosition() {
        return position;
    }

    /* (non-Javadoc)
     * @see org.metaborg.spoofax.core.dynamicclassloading.IBuilderInput#getAst()
     */
    @Override
    public IStrategoTerm getAst() {
        return ast;
    }

    /* (non-Javadoc)
     * @see org.metaborg.spoofax.core.dynamicclassloading.IBuilderInput#getResource()
     */
    @Override
    public @Nullable FileObject getResource() {
        return resource;
    }

    /* (non-Javadoc)
     * @see org.metaborg.spoofax.core.dynamicclassloading.IBuilderInput#getLocation()
     */
    @Override
    public @Nullable FileObject getLocation() {
        return location;
    }
}
