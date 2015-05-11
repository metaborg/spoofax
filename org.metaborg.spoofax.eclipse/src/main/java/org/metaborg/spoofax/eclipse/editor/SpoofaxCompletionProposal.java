package org.metaborg.spoofax.eclipse.editor;

import java.util.Collection;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.metaborg.spoofax.core.completion.ICompletion;
import org.metaborg.spoofax.core.completion.ICompletionItem;
import org.metaborg.spoofax.core.completion.ICursorCompletionItem;
import org.metaborg.spoofax.core.completion.IPlaceholderCompletionItem;
import org.metaborg.spoofax.core.completion.ITextCompletionItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class SpoofaxCompletionProposal implements ICompletionProposal {
    private static class CompletionData {
        public final String text;
        public final Multimap<String, LinkedPosition> placeholders;
        public final int cursorPosition;
        public final int cursorSequence;


        public CompletionData(String text, Multimap<String, LinkedPosition> placeholders, int cursorPosition,
            int cursorSequence) {
            this.text = text;
            this.cursorPosition = cursorPosition;
            this.cursorSequence = cursorSequence;
            this.placeholders = placeholders;
        }
    }


    private static final Logger logger = LoggerFactory.getLogger(SpoofaxCompletionProposal.class);

    private final ITextViewer textViewer;
    private final int offset;
    private final CompletionData data;


    public SpoofaxCompletionProposal(IDocument document, ITextViewer textViewer, int offset, ICompletion completion) {
        this.textViewer = textViewer;
        this.offset = offset;
        this.data = completionData(document, offset, completion);
    }


    @Override public void apply(IDocument document) {
        try {
            final Point selection = textViewer.getSelectedRange();
            document.replace(offset, selection.y, data.text);

            if(!data.placeholders.isEmpty()) {
                final LinkedModeModel model = new LinkedModeModel();
                for(Collection<LinkedPosition> positions : data.placeholders.asMap().values()) {
                    final LinkedPositionGroup group = new LinkedPositionGroup();
                    for(LinkedPosition position : positions) {
                        group.addPosition(position);
                    }
                    model.addGroup(group);
                }
                model.forceInstall();
                final LinkedModeUI ui = new LinkedModeUI(model, textViewer);
                ui.setExitPosition(textViewer, data.cursorPosition, 0, data.cursorSequence);
                ui.enter();
            }
        } catch(BadLocationException e) {
            final String message =
                String.format("Cannot apply completion at offset %s, length %s", offset, data.text.length());
            logger.error(message, e);
        }
    }

    @Override public Point getSelection(IDocument document) {
        if(data.placeholders.isEmpty()) {
            return new Point(data.cursorPosition, 0);
        }

        // There are placeholders, let linked mode take care of moving the cursor to the first placeholder. Returning
        // null signals no selection change should happen.
        return null;
    }

    @Override public String getAdditionalProposalInfo() {
        return null;
    }

    @Override public String getDisplayString() {
        return data.text;
    }

    @Override public Image getImage() {
        return null;
    }

    @Override public IContextInformation getContextInformation() {
        return null;
    }


    private static CompletionData completionData(IDocument document, int offset, ICompletion completion) {
        final StringBuilder stringBuilder = new StringBuilder();
        final Multimap<String, LinkedPosition> placeholders = ArrayListMultimap.create();
        int sequence = 0;
        int textOffset = offset;
        int cursorOffset = -1;
        int cursorSequence = -1;
        for(ICompletionItem item : completion.items()) {
            if(item instanceof ITextCompletionItem) {
                final ITextCompletionItem textItem = (ITextCompletionItem) item;
                final String text = textItem.text();
                stringBuilder.append(text);
                textOffset += text.length();
            } else if(item instanceof IPlaceholderCompletionItem) {
                final IPlaceholderCompletionItem placeholderItem = (IPlaceholderCompletionItem) item;
                final String text = placeholderItem.placeholderText();
                final int textLength = text.length();
                final String name = placeholderItem.name();
                stringBuilder.append(text);
                final LinkedPosition position = new LinkedPosition(document, textOffset, textLength, sequence++);
                placeholders.put(name, position);
                textOffset += text.length();
            } else if(item instanceof ICursorCompletionItem) {
                cursorOffset = textOffset;
                cursorSequence = sequence++;
            }
        }
        final String text = stringBuilder.toString();
        if(cursorOffset == -1) {
            cursorOffset = textOffset;
            cursorSequence = sequence++;
        }
        return new CompletionData(text, placeholders, cursorOffset, cursorSequence);
    }
}
