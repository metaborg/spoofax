package org.metaborg.spoofax.core.unit;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class TransformOutput implements ISpoofaxTransformOutput {
    public final String name;
    public final @Nullable FileObject resource;
    public final IStrategoTerm ast;

    public TransformOutput(String name, @Nullable FileObject output, IStrategoTerm ast) {
        this.name = name;
        this.resource = output;
        this.ast = ast;
    }
    
    @Override
    public String name() {
        return name;
    }

    @Override
    public FileObject output() {
        return resource;
    }

    @Override
    public IStrategoTerm ast() {
      return ast;
    }
 
}