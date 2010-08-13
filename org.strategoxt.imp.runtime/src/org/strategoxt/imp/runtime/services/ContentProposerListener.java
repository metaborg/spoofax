package org.strategoxt.imp.runtime.services;

import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.ISourceViewer;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.WeakWeakMap;

/**
 * Activates the content proposer based on text events.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class ContentProposerListener implements ITextListener {
	
	private static final WeakWeakMap<ISourceViewer, ContentProposerListener> asyncListeners =
		new WeakWeakMap<ISourceViewer, ContentProposerListener>();
	
	private final Set<Pattern> patterns;
	
	private final ISourceViewer viewer;
	
	private ContentProposerListener(Set<Pattern> patterns, ISourceViewer viewer) {
		this.patterns = patterns;
		this.viewer = viewer;
	}

	public static void register(Set<Pattern> triggers, ISourceViewer viewer) {
		synchronized (asyncListeners) {
			ContentProposerListener oldListener = asyncListeners.remove(viewer);
			if (oldListener != null)
				viewer.removeTextListener(oldListener);
			
			if (viewer instanceof ITextOperationTarget) {
				if (!triggers.isEmpty()) {
					ContentProposerListener listener = new ContentProposerListener(triggers, viewer);
					viewer.addTextListener(listener);
					asyncListeners.put(viewer, listener);
				}
			} else {
				Environment.logWarning("Source viewer is not an ITextOperationTarget; could not register content proposer triggers");
			}
		}
	}

	public void textChanged(TextEvent event) {
		try {
			boolean keyPressEvent = AutoEditStrategy.pollJustProcessedKeyEvent();
			boolean insertionEvent = ContentProposal.pollJustApplied();
			int eventLength = event.getText() == null || !insertionEvent ? 1 : event.getText().length();
			if (event.getDocumentEvent() != null // not just a visual change
					&& event.getText() != null
					&& (event.getText().length() == 1 // single keypress
							|| keyPressEvent || insertionEvent)
					&& matchesPatterns(event.getDocumentEvent().getDocument(), event.getOffset() + eventLength)) {
				viewer.setSelectedRange(event.getOffset() + eventLength, 0);
				((ITextOperationTarget) viewer).doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
			}
		} catch (BadLocationException e) {
			Environment.logException("Exception when checking for content proposal triggers", e);
		} catch (RuntimeException e) {
			Environment.logException("Exception when checking for content proposal triggers", e);
		}
	}
	
	private boolean matchesPatterns(IDocument document, int offset) throws BadLocationException {
		for (Pattern pattern : patterns) {
			boolean foundNewLine = false;
			for (int startOffset = offset - 1; startOffset >= 0; startOffset--) {
				String substring = document.get(startOffset, offset - startOffset);
				if (pattern.matcher(substring).matches()) {
					return true;
				}
				char c = substring.charAt(0);
				if (c == '\n' /*|| c == '\r'*/) {
					if (foundNewLine) break; // looked back far enough
					foundNewLine = true;
				}
			}
		}
		return false;
	}
}
