/**
 * 
 */
package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.asJavaString;

import org.eclipse.imp.editor.SourceProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.spoofax.interpreter.terms.IStrategoList;

/**
 * A content proposal that selects the lexical at the cursor location.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class ContentProposal extends SourceProposal implements ICompletionProposalExtension6 {

	private static Color identifierColor;
	
	private static Color keywordColor;
	
	private static Font keywordFont;
	
	private final ContentProposer proposer;
	
	private final IStrategoList newTextParts;
	
	public ContentProposal(ContentProposer proposer, String proposal, String newText, String prefix, Region region,
			int cursorLoc, String addlInfo) {
		super(proposal, newText, prefix, region, cursorLoc, addlInfo);
		this.proposer = proposer;
		this.newTextParts = null;
	}

	public ContentProposal(ContentProposer proposer, String proposal, String newText, String prefix, Region region, 
			IStrategoList newTextParts, String addlInfo) {
		super(proposal, newText, prefix, region, addlInfo);
		this.proposer = proposer;
		this.newTextParts = newTextParts;
	}

	private boolean isKeywordProposal() {
		return newTextParts == null;
	}

	@Override
	public Point getSelection(IDocument document) {
		if (newTextParts == null)
			return super.getSelection(document);
		else
			return proposalPartsToSelection(newTextParts, getRange().getOffset() - getPrefix().length());
	}

	private Point proposalPartsToSelection(IStrategoList proposalParts, int offset) {
		int i = asJavaString(proposalParts.head()).length();
		for (IStrategoList cons = proposalParts.tail(); !cons.isEmpty(); cons = cons.tail()) {
			String part = asJavaString(cons.head());
			if (proposer.getCompletionLexical().matcher(part).matches())
				return new Point(offset + i, part.length());
			i += part.length();
		}
		return new Point(offset + i, 0);
	}
	
	@Override
	public void apply(IDocument document) {
		super.apply(document);
		proposer.getObserver().setRushNextUpdate(true);
		proposer.getParser().getErrorHandler().setRushNextUpdate(true);
		proposer.getParser().scheduleParserUpdate(0, false);
	}

	public StyledString getStyledDisplayString() {
		return new StyledString(getDisplayString(), new ContentProposalStyler());
	}
	
	private class ContentProposalStyler extends Styler {

		@Override
		public void applyStyles(TextStyle style) {
			if (isKeywordProposal()) { // keyword proposal
				style.font = getKeywordFont();
				style.foreground = getKeywordColor();
			} else if (newTextParts.size() == 1) { // identifier proposal
				style.foreground = getIdentifierColor();
			}
		}
		
		private Color getIdentifierColor() {
			if (identifierColor == null)
				identifierColor = new Color(Display.getCurrent(), 64, 64, 255);
			return identifierColor;
		}
		
		private Color getKeywordColor() {
			if (keywordColor == null)
				keywordColor = new Color(Display.getCurrent(), 127, 0, 85);
			return keywordColor;
		}
		
		private Font getKeywordFont() {
			if (keywordFont == null)
				keywordFont = new Font(Display.getCurrent(), "Courier new", 13, SWT.BOLD);
			return keywordFont;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ContentProposal))
			return false;
		ContentProposal other = (ContentProposal) obj;
		return other.getDisplayString().equals(getDisplayString()) && isKeywordProposal() == other.isKeywordProposal();
	}
	
	@Override
	public int hashCode() {
		return getDisplayString().hashCode();
	}
}
