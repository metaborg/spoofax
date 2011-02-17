package org.strategoxt.imp.runtime.services;

import static java.util.Collections.synchronizedMap;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyleRange;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

/**
 * This class cancels any presentation update events
 * that occur before the parser completes.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class TokenColorerHelper implements ITextPresentationListener {
	
	private static Map<ISourceViewer, TokenColorerHelper> helpers =
		synchronizedMap(new WeakHashMap<ISourceViewer, TokenColorerHelper>());
	
	private TokenColorerHelper(SGLRParseController controller) {
		// Not using controller atm
	}
	
	public static void register(SGLRParseController controller, EditorState editor) {
		ISourceViewer viewer = editor.getEditor().getServiceControllerManager().getSourceViewer();
		if (helpers.get(viewer) == null && viewer instanceof ITextViewerExtension4) {
			TokenColorerHelper listener = new TokenColorerHelper(controller);
			((ITextViewerExtension4) viewer).addTextPresentationListener(listener);
			helpers.put(viewer, listener);
		}
	}
	
	public static void unregister(EditorState editor) {
		ISourceViewer viewer = editor.getEditor().getServiceControllerManager().getSourceViewer();
		ITextPresentationListener listener = helpers.remove(viewer);
		if (listener != null)
			((ITextViewerExtension4) viewer).removeTextPresentationListener(listener);
	}
	
	public void applyTextPresentation(TextPresentation presentation) {
		if (!isParserBasedPresentation(presentation))
			presentation.clear();
	}

	/**
	 * Determines if a presentation is based on the result of the parser,
	 * or if it is just an intermediate presentation before the parser runs.
	 */
	private static boolean isParserBasedPresentation(TextPresentation presentation) {
		if (presentation.isEmpty())
			return false;
		StyleRange last = presentation.getLastStyleRange();
		StyleRange first = presentation.getFirstStyleRange();
		return first.start == 0 && last.start + last.length >= presentation.getExtent().getLength();
	}
}
