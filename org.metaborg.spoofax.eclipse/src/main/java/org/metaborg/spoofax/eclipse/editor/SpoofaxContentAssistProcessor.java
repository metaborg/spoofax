package org.metaborg.spoofax.eclipse.editor;



import org.apache.commons.vfs2.FileObject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.completion.ICompletion;
import org.metaborg.spoofax.core.completion.ICompletionService;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.eclipse.processing.ParseResultProcessor;

import com.google.common.collect.Iterables;

public class SpoofaxContentAssistProcessor implements IContentAssistProcessor {
    private final ICompletionService completionService;

    private final ParseResultProcessor parseResultProcessor;

    private final FileObject resource;
    private final IDocument document;


    public SpoofaxContentAssistProcessor(ICompletionService completionService,
        ParseResultProcessor parseResultProcessor, FileObject resource, IDocument document) {
        this.completionService = completionService;

        this.parseResultProcessor = parseResultProcessor;

        this.resource = resource;
        this.document = document;
    }


    @Override public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        final ParseResult<?> parseResult = parseResultProcessor.get(resource);
        if(parseResult == null) {
            return null;
        }

        return proposals(parseResult, viewer, offset);
    }
    
    private ICompletionProposal[] proposals(ParseResult<?> parseResult, ITextViewer viewer, int offset) {
        final Iterable<ICompletion> completions;
        try {
            completions = completionService.get(parseResult, offset);
        } catch(SpoofaxException e) {
            return null;
        }

        final int numCompletions = Iterables.size(completions);
        final ICompletionProposal[] proposals = new ICompletionProposal[numCompletions];
        int i = 0;
        for(ICompletion completion : completions) {
            proposals[i] = new SpoofaxCompletionProposal(document, viewer, offset, completion);
            ++i;
        }
        return proposals;
    }

    @Override public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override public char[] getCompletionProposalAutoActivationCharacters() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override public char[] getContextInformationAutoActivationCharacters() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override public String getErrorMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override public IContextInformationValidator getContextInformationValidator() {
        // TODO Auto-generated method stub
        return null;
    }

}
