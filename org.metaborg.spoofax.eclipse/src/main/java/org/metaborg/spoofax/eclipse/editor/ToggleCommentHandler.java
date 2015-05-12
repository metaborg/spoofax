package org.metaborg.spoofax.eclipse.editor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.metaborg.spoofax.core.SpoofaxRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copied from {@link org.eclipse.jdt.internal.ui.javaeditor.ToggleCommentAction}.
 */
public class ToggleCommentHandler extends AbstractHandler {
    private static final Logger logger = LoggerFactory.getLogger(ToggleCommentHandler.class);

    @Override public Object execute(ExecutionEvent event) throws ExecutionException {
        final IEditorPart part = HandlerUtil.getActiveEditor(event);
        if(!(part instanceof ISpoofaxEditor)) {
            throw new SpoofaxRuntimeException("Editor is not a Spoofax editor");
        }
        final SpoofaxEditor editor = (SpoofaxEditor) part;
        final IDocument document = editor.document();
        final ISelection selection = editor.getSelectionProvider().getSelection();

        final SourceViewerConfiguration configuration = editor.configuration();
        final ISourceViewer sourceViewer = editor.sourceViewer();
        final String partitioning = configuration.getConfiguredDocumentPartitioning(sourceViewer);
        final String[] types = configuration.getConfiguredContentTypes(sourceViewer);
        final Map<String, String[]> prefixesMap = new HashMap<String, String[]>(types.length);
        for(int i = 0; i < types.length; i++) {
            String type = types[i];
            String[] prefixes = configuration.getDefaultPrefixes(sourceViewer, type);
            if(prefixes != null && prefixes.length > 0) {
                int emptyPrefixes = 0;
                for(int j = 0; j < prefixes.length; j++)
                    if(prefixes[j].length() == 0)
                        emptyPrefixes++;

                if(emptyPrefixes > 0) {
                    String[] nonemptyPrefixes = new String[prefixes.length - emptyPrefixes];
                    for(int j = 0, k = 0; j < prefixes.length; j++) {
                        String prefix = prefixes[j];
                        if(prefix.length() != 0) {
                            nonemptyPrefixes[k] = prefix;
                            k++;
                        }
                    }
                    prefixes = nonemptyPrefixes;
                }

                prefixesMap.put(type, prefixes);
            }
        }

        final boolean commented = isSelectionCommented(document, selection, partitioning, prefixesMap);

        final int operationCode;
        if(commented)
            operationCode = ITextOperationTarget.STRIP_PREFIX;
        else
            operationCode = ITextOperationTarget.PREFIX;

        final ITextOperationTarget textOperationTarget =
            (ITextOperationTarget) editor.getAdapter(ITextOperationTarget.class);
        textOperationTarget.doOperation(operationCode);

        return null;
    }

    /**
     * Is the given selection single-line commented?
     *
     * @param selection
     *            Selection to check
     * @return <code>true</code> iff all selected lines are commented
     */
    private boolean isSelectionCommented(IDocument document, ISelection selection, String partitioning,
        Map<String, String[]> prefixesMap) {
        if(!(selection instanceof ITextSelection))
            return false;

        ITextSelection textSelection = (ITextSelection) selection;
        if(textSelection.getStartLine() < 0 || textSelection.getEndLine() < 0) {
            return false;
        }

        try {
            IRegion block = getTextBlockFromSelection(textSelection, document);
            ITypedRegion[] regions =
                TextUtilities.computePartitioning(document, partitioning, block.getOffset(), block.getLength(), false);

            int[] lines = new int[regions.length * 2]; // [startline, endline, startline, endline, ...]
            for(int i = 0, j = 0; i < regions.length; i++, j += 2) {
                // start line of region
                lines[j] = getFirstCompleteLineOfRegion(regions[i], document);
                // end line of region
                int length = regions[i].getLength();
                int offset = regions[i].getOffset() + length;
                if(length > 0) {
                    offset--;
                }
                lines[j + 1] = (lines[j] == -1 ? -1 : document.getLineOfOffset(offset));
            }

            // Perform the check
            for(int i = 0, j = 0; i < regions.length; i++, j += 2) {
                String[] prefixes = prefixesMap.get(regions[i].getType());
                if(prefixes != null && prefixes.length > 0 && lines[j] >= 0 && lines[j + 1] >= 0)
                    if(!isBlockCommented(lines[j], lines[j + 1], prefixes, document))
                        return false;
            }

            return true;
        } catch(BadLocationException e) {
            logger.error("Toggling comment failed", e);
        }

        return false;
    }

    /**
     * Creates a region describing the text block (something that starts at the beginning of a line) completely
     * containing the current selection.
     *
     * @param selection
     *            The selection to use
     * @param document
     *            The document
     * @return the region describing the text block comprising the given selection
     */
    private IRegion getTextBlockFromSelection(ITextSelection selection, IDocument document) {
        try {
            IRegion line = document.getLineInformationOfOffset(selection.getOffset());
            int length =
                selection.getLength() == 0 ? line.getLength() : selection.getLength()
                    + (selection.getOffset() - line.getOffset());
            return new Region(line.getOffset(), length);

        } catch(BadLocationException e) {
            logger.error("Toggling comment failed", e);
        }

        return null;
    }

    /**
     * Returns the index of the first line whose start offset is in the given text range.
     *
     * @param region
     *            the text range in characters where to find the line
     * @param document
     *            The document
     * @return the first line whose start index is in the given range, -1 if there is no such line
     */
    private int getFirstCompleteLineOfRegion(IRegion region, IDocument document) {
        try {

            final int startLine = document.getLineOfOffset(region.getOffset());

            int offset = document.getLineOffset(startLine);
            if(offset >= region.getOffset()) {
                return startLine;
            }

            final int nextLine = startLine + 1;
            if(nextLine == document.getNumberOfLines()) {
                return -1;
            }

            offset = document.getLineOffset(nextLine);
            return(offset > region.getOffset() + region.getLength() ? -1 : nextLine);

        } catch(BadLocationException e) {
            logger.error("Toggling comment failed", e);
        }

        return -1;
    }

    /**
     * Determines whether each line is prefixed by one of the prefixes.
     *
     * @param startLine
     *            Start line in document
     * @param endLine
     *            End line in document
     * @param prefixes
     *            Possible comment prefixes
     * @param document
     *            The document
     * @return <code>true</code> iff each line from <code>startLine</code> to and including <code>endLine</code> is
     *         prepended by one of the <code>prefixes</code>, ignoring whitespace at the begin of line
     */
    private boolean isBlockCommented(int startLine, int endLine, String[] prefixes, IDocument document) {
        try {
            // check for occurrences of prefixes in the given lines
            for(int i = startLine; i <= endLine; i++) {
                IRegion line = document.getLineInformation(i);
                String text = document.get(line.getOffset(), line.getLength());

                int[] found = TextUtilities.indexOf(prefixes, text, 0);

                if(found[0] == -1) {
                    // found a line which is not commented
                    return false;
                }

                String s = document.get(line.getOffset(), found[0]);
                s = s.trim();
                if(s.length() != 0) {
                    // found a line which is not commented
                    return false;
                }

            }

            return true;
        } catch(BadLocationException e) {
            logger.error("Toggling comment failed", e);
        }

        return false;
    }
}
