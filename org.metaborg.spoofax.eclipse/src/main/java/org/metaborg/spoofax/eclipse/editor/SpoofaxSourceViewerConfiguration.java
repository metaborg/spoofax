package org.metaborg.spoofax.eclipse.editor;

import org.eclipse.jface.text.DefaultTextHover;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.Iterables;

public class SpoofaxSourceViewerConfiguration extends SourceViewerConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(SourceViewerConfiguration.class);

    private final SpoofaxEditor editor;
    private final ISyntaxService<IStrategoTerm> syntaxService;


    public SpoofaxSourceViewerConfiguration(SpoofaxEditor editor, ISyntaxService<IStrategoTerm> syntaxService) {
        super();

        this.editor = editor;
        this.syntaxService = syntaxService;
    }


    @Override public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
        return new DefaultAnnotationHover();
    }

    @Override public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
        return new DefaultTextHover(sourceViewer);
    }

    @Override public String[] getDefaultPrefixes(ISourceViewer sourceViewer, String contentType) {
        final ILanguage language = editor.language();
        if(language == null) {
            logger.warn("Cannot get language-specific single line comment prefix, identified language for {} is null, "
                + "toggle comment is disabled", editor.resource());
            return new String[0];
        }
        return Iterables.toArray(syntaxService.singleLineCommentPrefixes(language), String.class);
    }
}
