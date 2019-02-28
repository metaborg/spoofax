package org.metaborg.spoofax.core.dynamicclassloading;

import org.apache.commons.vfs2.FileObject;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.StrategoString;
import org.spoofax.terms.StrategoTuple;
import org.spoofax.terms.TermFactory;

public class BuilderInput extends StrategoTuple implements IBuilderInput {
    private final IStrategoTerm selection;
    private final IStrategoTerm position;
    private final IStrategoTerm ast;
    private final String resource;
    private final FileObject location;

    @SuppressWarnings("deprecation")
    public BuilderInput(IStrategoTerm selection, IStrategoTerm position, IStrategoTerm ast, String resource,
            FileObject location) {
        super(new IStrategoTerm[] { selection, position, ast,
                    new StrategoString(resource, TermFactory.EMPTY_LIST, IStrategoTerm.IMMUTABLE),
                    new StrategoString(location.getName().getURI(), TermFactory.EMPTY_LIST, IStrategoTerm.IMMUTABLE) }, 
                TermFactory.EMPTY_LIST,
                IStrategoTerm.IMMUTABLE);

        this.selection = selection;
        this.position = position;
        this.ast = ast;
        this.resource = resource;
        this.location = location;
    }

    /* (non-Javadoc)
     * @see org.metaborg.spoofax.core.semantic_provider.IBuilderInput#getSelection()
     */
    @Override
    public IStrategoTerm getSelection() {
        return selection;
    }

    /* (non-Javadoc)
     * @see org.metaborg.spoofax.core.semantic_provider.IBuilderInput#getPosition()
     */
    @Override
    public IStrategoTerm getPosition() {
        return position;
    }

    /* (non-Javadoc)
     * @see org.metaborg.spoofax.core.semantic_provider.IBuilderInput#getAst()
     */
    @Override
    public IStrategoTerm getAst() {
        return ast;
    }

    /* (non-Javadoc)
     * @see org.metaborg.spoofax.core.semantic_provider.IBuilderInput#getResource()
     */
    @Override
    public String getResource() {
        return resource;
    }

    /* (non-Javadoc)
     * @see org.metaborg.spoofax.core.semantic_provider.IBuilderInput#getLocation()
     */
    @Override
    public FileObject getLocation() {
        return location;
    }
}
