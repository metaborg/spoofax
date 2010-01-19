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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;
import org.spoofax.interpreter.terms.IStrategoList;

/**
 * A content proposal that selects the lexical at the cursor location.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class ContentProposal extends SourceProposal implements ICompletionProposalExtension6 {
	
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
			// TODO: styles for keywords, identifiers, and other
			style.foreground = style.foreground;
		}
		
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof ContentProposal && ((ContentProposal) obj).getDisplayString().equals(getDisplayString());
	}
	
	@Override
	public int hashCode() {
		return getDisplayString().hashCode();
	}
}
