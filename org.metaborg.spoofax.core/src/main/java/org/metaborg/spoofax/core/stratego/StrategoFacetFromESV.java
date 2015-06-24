package org.metaborg.spoofax.core.stratego;

import static org.spoofax.interpreter.core.Tools.termAt;

import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.spoofax.core.analysis.stratego.StrategoAnalysisMode;
import org.metaborg.spoofax.core.esv.ESVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.Sets;

public class StrategoFacetFromESV {
    private static final Logger logger = LoggerFactory.getLogger(StrategoFacetFromESV.class);


    public static StrategoFacet create(IStrategoAppl esv, FileObject location) throws FileSystemException {
        final Set<FileObject> strategoFiles = providerResources(esv, location);
        // Use LinkedHashSet to maintain ordering.
        final Set<FileObject> ctreeFiles = Sets.newLinkedHashSet();
        final Set<FileObject> jarFiles = Sets.newLinkedHashSet();
        for(FileObject strategoFile : strategoFiles) {
            final String extension = strategoFile.getName().getExtension();
            if(extension.equals("jar")) {
                jarFiles.add(strategoFile);
            } else if(extension.equals("ctree")) {
                ctreeFiles.add(strategoFile);
            } else {
                logger.warn("Stratego provider file {} has unknown extension {}, ignoring", strategoFile, extension);
            }
        }
        final String analysisStrategy = analysisStrategy(esv);
        final StrategoAnalysisMode analysisMode = analysisMode(esv);
        final String onSaveStrategy = onSaveStrategy(esv);
        final String resolverStrategy = resolverStrategy(esv);
        final String hoverStrategy = hoverStrategy(esv);
        final String completionStrategy = completionStrategy(esv);
        final StrategoFacet strategoFacet =
            new StrategoFacet(ctreeFiles, jarFiles, analysisStrategy, analysisMode, onSaveStrategy, resolverStrategy,
                hoverStrategy, completionStrategy);
        return strategoFacet;
    }


    private static @Nullable String analysisStrategy(IStrategoAppl esv) {
        final IStrategoAppl strategy = ESVReader.findTerm(esv, "SemanticObserver");
        if(strategy == null) {
            return null;
        }
        final String observerFunction = ESVReader.termContents(termAt(strategy, 0));
        return observerFunction;
    }

    private static @Nullable StrategoAnalysisMode analysisMode(IStrategoAppl esv) {
        final IStrategoAppl strategy = ESVReader.findTerm(esv, "SemanticObserver");
        if(strategy == null) {
            return null;
        }
        final IStrategoTerm annotations = strategy.getSubterm(1);
        boolean multifile = false;
        for(IStrategoTerm annotation : annotations) {
            multifile |= Tools.hasConstructor((IStrategoAppl) annotation, "MultiFile", 0);
        }
        if(multifile) {
            return StrategoAnalysisMode.MultiAST;
        }
        return StrategoAnalysisMode.SingleAST;
    }

    private static @Nullable String onSaveStrategy(IStrategoAppl esv) {
        IStrategoAppl onsave = ESVReader.findTerm(esv, "OnSave");
        onsave = onsave == null ? ESVReader.findTerm(esv, "OnSaveDeprecated") : onsave;
        if(onsave != null) {
            String function = ((IStrategoString) onsave.getSubterm(0).getSubterm(0)).stringValue();
            return function;
        }
        return null;
    }

    private static @Nullable String resolverStrategy(IStrategoAppl esv) {
        final IStrategoAppl resolver = ESVReader.findTerm(esv, "ReferenceRule");
        if(resolver == null)
            return null;
        return ESVReader.termContents(termAt(resolver, 1));
    }

    private static @Nullable String hoverStrategy(IStrategoAppl esv) {
        final IStrategoAppl hover = ESVReader.findTerm(esv, "HoverRule");
        if(hover == null)
            return null;
        return ESVReader.termContents(termAt(hover, 1));
    }

    private static @Nullable String completionStrategy(IStrategoAppl esv) {
        final IStrategoAppl completer = ESVReader.findTerm(esv, "CompletionProposer");
        if(completer == null)
            return null;
        return ESVReader.termContents(termAt(completer, 1));
    }

    private static Set<FileObject> providerResources(IStrategoAppl esv, FileObject location) throws FileSystemException {
        // Use LinkedHashSet to maintain ordering.
        final Set<FileObject> attachedFiles = Sets.newLinkedHashSet();
        for(IStrategoAppl s : ESVReader.collectTerms(esv, "SemanticProvider")) {
            attachedFiles.add(location.resolveFile(ESVReader.termContents(s)));
        }
        return attachedFiles;
    }
}
