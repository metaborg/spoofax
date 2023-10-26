package org.metaborg.spoofax.core.dynamicclassloading;

import jakarta.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.util.resource.ResourceUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.StrategoString;
import org.spoofax.terms.StrategoTuple;

public class BuilderInput extends StrategoTuple implements IBuilderInput<IStrategoTerm, IStrategoTerm> {
    private final IStrategoTerm selection;
    private final IStrategoTerm position;
    private final IStrategoTerm ast;
    private final @Nullable FileObject resource;
    private final @Nullable FileObject location;

    public BuilderInput(ITermFactory termFactory, IStrategoTerm selection, IStrategoTerm position, IStrategoTerm ast, @Nullable FileObject resource,
        @Nullable FileObject location) {
        super(new IStrategoTerm[] { selection, position, ast,
                    new StrategoString(resourceString(resource, location), termFactory.makeList()),
                    new StrategoString(locationString(location), termFactory.makeList()) }, 
                termFactory.makeList());

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

    @Override
    public IStrategoTerm getSelection() {
        return selection;
    }

    @Override
    public IStrategoTerm getPosition() {
        return position;
    }

    @Override
    public IStrategoTerm getAst() {
        return ast;
    }

    @Override
    public @Nullable FileObject getResource() {
        return resource;
    }

    @Override
    public @Nullable FileObject getLocation() {
        return location;
    }
}
