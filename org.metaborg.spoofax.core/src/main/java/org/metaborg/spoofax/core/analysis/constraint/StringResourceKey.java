package org.metaborg.spoofax.core.analysis.constraint;

import static mb.nabl2.terms.build.TermBuild.B;
import static mb.nabl2.terms.matching.TermMatch.M;

import java.io.Serializable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.util.resource.ResourceUtils;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import mb.nabl2.terms.ITerm;

public class StringResourceKey implements IResourceKey, Serializable {
    private static final long serialVersionUID = 1L;

    private final String resource;

    public StringResourceKey(String resource) {
        this.resource = resource;
    }

    @Override public ITerm toTerm() {
        return B.newString(resource);
    }

    @Override public IStrategoTerm toStrategoTerm(ITermFactory termFactory) {
        return termFactory.makeString(resource);
    }

    @Override public FileObject toFileObject(FileObject location) throws FileSystemException {
        return location.resolveFile(resource);
    }

    @Override public int hashCode() {
        return resource.hashCode();
    }

    @Override public boolean equals(Object obj) {
        return resource.equals(obj);
    }

    @Override public String toString() {
        return resource;
    }

    public static StringResourceKey fromTerm(ITerm term) {
        return M.stringValue().match(term).map(resource -> new StringResourceKey(resource))
                .orElseThrow(() -> new IllegalArgumentException("Expected resource string, got " + term));
    }

    public static StringResourceKey fromStrategoTerm(IStrategoTerm term) {
        if(!Tools.isTermString(term)) {
            throw new IllegalArgumentException("Expected resource string, got " + term);
        }
        return new StringResourceKey(Tools.asJavaString(term));
    }

    public static StringResourceKey fromFileObject(FileObject resource, FileObject location) {
        final String resourceKey = ResourceUtils.relativeName(resource.getName(), location.getName(), true);
        return new StringResourceKey(resourceKey);
    }

}