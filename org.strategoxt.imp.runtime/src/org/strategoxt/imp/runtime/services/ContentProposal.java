/**
 * 
 */
package org.strategoxt.imp.runtime.services;

import static org.eclipse.core.runtime.Platform.OS_MACOSX;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.termContents;

import org.eclipse.core.runtime.Platform;
import org.eclipse.imp.editor.SourceProposal;
import org.eclipse.jface.text.BadLocationException;
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
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.TermReader;

/**
 * A content proposal that selects the lexical at the cursor location.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class ContentProposal extends SourceProposal implements ICompletionProposalExtension6 {
	
	private static final boolean USE_BIG_FONT = Platform.getOS() == OS_MACOSX;

	private static Color identifierColor;
	
	private static Color keywordColor;
	
	private static Font keywordFont;
	
	private final ContentProposer proposer;
	
	private final IStrategoList newTextParts;
	
	private final ContentProposalTemplate template;
	
	/**
	 * Creates a new keyword-based content proposal.
	 */
	public ContentProposal(ContentProposer proposer, String proposal, String newText, String prefix, Region region,
			int cursorLoc, String addlInfo) {
		
		super(proposal, newText, prefix, region, cursorLoc, addlInfo);
		this.proposer = proposer;
		this.newTextParts = null;
		this.template = null;
	}
	
	/**
	 * Creates a new template-based content proposal.
	 */
	public ContentProposal(ContentProposer proposer, String proposalPrefix,
			ContentProposalTemplate proposal, String prefix, Region region) {
		
		super(proposal.getName(), proposalPrefix, prefix, region, null);
		this.proposer = proposer;
		this.newTextParts = proposal.getCompletionParts();
		this.template = proposal;
	}

	/**
	 * Creates a new semantic content proposal.
	 */
	public ContentProposal(ContentProposer proposer, String proposal, String newText, String prefix, Region region, 
			IStrategoList newTextParts, String addlInfo) {
		
		super(proposal, newText, prefix, region, addlInfo);
		assert newTextParts != null;
		this.proposer = proposer;
		this.newTextParts = newTextParts;
		this.template = null;
	}

	private boolean isKeywordProposal() {
		return newTextParts == null;
	}

	private boolean isTemplateProposal() {
		return template != null;
	}

	@Override
	public Point getSelection(IDocument document) {
		if (newTextParts == null) {
			return super.getSelection(document);
		} else {
			return proposalPartsToSelection(document, newTextParts, getRange().getOffset() - getPrefix().length());
		}
	}
	
	@Override
	public String getAdditionalProposalInfo() {
		// TODO: support newlines and tabs in proposal descriptions?
		return template != null ? template.getDescription() : super.getAdditionalProposalInfo();
	}

	private Point proposalPartsToSelection(IDocument document, IStrategoList proposalParts, int offset) {
		int i = termContents(proposalParts.head()).length();
		for (IStrategoList cons = proposalParts.tail(); !cons.isEmpty(); cons = cons.tail()) {
			IStrategoTerm partTerm = cons.head();
			String part = proposalPartToString(document, partTerm);
			if ("Placeholder".equals(TermReader.cons(partTerm)) 
					|| proposer.getCompletionLexical().matcher(part).matches())
				return new Point(offset + i, part.length());
			i += part.length();
		}
		return new Point(offset + i, 0);
	}
	
	private String proposalPartsToString(IDocument document, IStrategoList proposalParts) {
		StringBuilder result = new StringBuilder();
		for (IStrategoList cons = proposalParts; !cons.isEmpty(); cons = cons.tail()) {
			result.append(proposalPartToString(document, cons.head()));
		}
		return result.toString();
	}
	
	private String proposalPartToString(IDocument document, IStrategoTerm part) {
		try {
			String lineStart = AutoEditStrategy.getLineBeforeOffset(document, getRange().getOffset());
			return AutoEditStrategy.formatInsertedText(termContents(part), lineStart);
		} catch (BadLocationException e) {
			Environment.logException("Could not format completion fragment", e);
			return termContents(part);
		}
	}
	
	@Override
	public void apply(IDocument document) {
		try {
	        Region range = getRange();
			String newText = newTextParts == null
					? getNewText()
					: proposalPartsToString(document, newTextParts);
			document.replace(range.getOffset(), range.getLength(), newText.substring(getPrefix().length()));
		} catch (BadLocationException e) {
			Environment.logException("Could not apply content proposal", e);
		}
		proposer.getObserver().setRushNextUpdate(true);
		proposer.getParser().getErrorHandler().setRushNextUpdate(true);
		proposer.getParser().scheduleParserUpdate(0, false);
	}
	
	@Override
	public String getNewText() {
		assert newTextParts == null : "Don't use me if newTextParts != null";
		return super.getNewText();
	}

	public StyledString getStyledDisplayString() {
		return new StyledString(getDisplayString(), new ContentProposalStyler());
	}
	
	private class ContentProposalStyler extends Styler {

		@Override
		public void applyStyles(TextStyle style) {
			if (isKeywordProposal() || isTemplateProposal()) {
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
			// HACK: hardcoded font size
			// TODO: use non-hard coded font?
	        //       e.g. FontData[] fontData= JFaceResources.getFontDescriptor("org.eclipse.jdt.ui.editors.textfont").getFontData();
			//            if (fontData != null && fontData.length > 0) ...
			if (keywordFont == null)
				keywordFont = new Font(Display.getCurrent(), "Courier new", USE_BIG_FONT ? 13 : 12, SWT.BOLD);
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
