package org.metaborg.spoofax.core.language.dialect;

import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.ParseError;
import org.spoofax.terms.io.binary.TermReader;

import rx.functions.Func1;

public class ResourceDialectIdentifier implements Func1<FileObject, Boolean> {
    private final ITermFactoryService termFactoryService;
    private final String dialectName;


    public ResourceDialectIdentifier(ITermFactoryService termFactoryService, String dialectName) {
        this.termFactoryService = termFactoryService;
        this.dialectName = dialectName;
    }


    @Override public Boolean call(FileObject resource) {
        try {
            final FileObject metaResource = metaResource(resource);
            if(metaResource == null) {
                return false;
            }
            final TermReader termReader = new TermReader(termFactoryService.getGeneric());
            final IStrategoTerm term = termReader.parseFromStream(metaResource.getContent().getInputStream());
            final String name = getSyntaxName(term.getSubterm(0));
            if(name == null) {
                return false;
            }
            return dialectName.equals(name);
        } catch(ParseError | IOException e) {

        }
        return false;
    }


    public static FileObject metaResource(FileObject resource) {
        try {
            final String path = resource.getName().getPath();
            final String fileName = FilenameUtils.getBaseName(path);
            if(fileName.isEmpty()) {
                return null;
            }
            final String metaResourceName = fileName + ".meta";
            final FileObject parent = resource.getParent();
            if(parent == null) {
                return null;
            }
            final FileObject metaResource = parent.getChild(metaResourceName);
            if(metaResource == null || !metaResource.exists()) {
                return null;
            }
            return metaResource;
        } catch(FileSystemException e) {
            return null;
        }
    }


    private static String getSyntaxName(IStrategoTerm entries) {
        for(IStrategoTerm entry : entries.getAllSubterms()) {
            final String cons = ((IStrategoAppl) entry).getConstructor().getName();
            if(cons.equals("Syntax")) {
                return Tools.asJavaString(entry.getSubterm(0));
            }
        }
        return null;
    }

}
