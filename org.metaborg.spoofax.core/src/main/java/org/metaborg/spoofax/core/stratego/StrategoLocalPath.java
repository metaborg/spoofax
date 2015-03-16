package org.metaborg.spoofax.core.stratego;

import java.io.File;

import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Inject;

/**
 * Utility class for creating local paths which are usable in Stratego.
 */
public class StrategoLocalPath {
    private final ITermFactoryService termFactoryService;


    @Inject public StrategoLocalPath(ITermFactoryService termFactoryService) {
        this.termFactoryService = termFactoryService;
    }


    public IStrategoString localLocationTerm(File localLocation) {
        final ITermFactory termFactory = termFactoryService.getGeneric();
        final String locationPath = localLocation.getAbsolutePath();
        final IStrategoString locationPathTerm = termFactory.makeString(locationPath);
        return locationPathTerm;
    }

    public IStrategoString localResourceTerm(File localResource, File localLocation) {
        final ITermFactory termFactory = termFactoryService.getGeneric();
        final String resourcePath = localLocation.toURI().relativize(localResource.toURI()).getPath();
        final IStrategoString resourcePathTerm = termFactory.makeString(resourcePath);
        return resourcePathTerm;
    }
}
