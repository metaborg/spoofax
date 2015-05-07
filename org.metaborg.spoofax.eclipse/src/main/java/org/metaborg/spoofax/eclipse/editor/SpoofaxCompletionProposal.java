package org.metaborg.spoofax.eclipse.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.metaborg.spoofax.core.completion.ICompletion;
import org.metaborg.spoofax.core.completion.ICompletionItem;
import org.metaborg.spoofax.core.completion.ICursorCompletionItem;
import org.metaborg.spoofax.core.completion.ITextCompletionItem;

public class SpoofaxCompletionProposal implements ICompletionProposal {
    private final int offset;
    private final ICompletion completion;


    public SpoofaxCompletionProposal(int offset, ICompletion completion) {
        this.offset = offset;
        this.completion = completion;
    }


    @Override public void apply(IDocument document) {
        try {
            final String text = toText();
            document.replace(offset, 0, text);
        } catch(BadLocationException e) {
            // TODO: log
        }
    }

    @Override public Point getSelection(IDocument document) {
        return new Point(cursorPosition(), 0);
    }

    @Override public String getAdditionalProposalInfo() {
        return null;
    }

    @Override public String getDisplayString() {
        return toText();
    }

    @Override public Image getImage() {
        return null;
    }

    @Override public IContextInformation getContextInformation() {
        return null;
    }
    
    
    private String toText() {
        final StringBuilder stringBuilder = new StringBuilder();
        for(ICompletionItem item : completion.items()) {
            if(item instanceof ITextCompletionItem) {
                final ITextCompletionItem textItem = (ITextCompletionItem) item;
                final String text = textItem.text();
                stringBuilder.append(text);
            }
        }
        return stringBuilder.toString();
    }
    
    private int cursorPosition() {
        int cursorOffset = offset;
        for(ICompletionItem item : completion.items()) {
            if(item instanceof ITextCompletionItem) {
                final ITextCompletionItem textItem = (ITextCompletionItem) item;
                final String text = textItem.text();
                cursorOffset += text.length();
            } else if(item instanceof ICursorCompletionItem) {
                return cursorOffset;
            }
        }
        return cursorOffset;
    }
}
