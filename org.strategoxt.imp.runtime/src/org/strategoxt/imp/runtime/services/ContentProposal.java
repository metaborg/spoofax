package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.termAt;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.cons;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.termContents;

import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.link.ProposalPosition;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;

/**
 * A content proposal that selects the lexical at the cursor location.
 *
 * @author Lennart Kats <lennart add lclnet.nl>
 * @author Tobi Vollebregt
 */
public class ContentProposal implements ICompletionProposal, ICompletionProposalExtension6 {

	private static volatile boolean justApplied;

	private final ContentProposer proposer;

	private final Completion completion;

	private final String prefix;

	private final ITextViewer viewer;

	private Position position;

	// Must be settable to get rid of constructor circular dependency with ProposalPosition
	public void setPosition(Position position) {
		this.position = position;
	}

	public ContentProposal(ContentProposer proposer, Completion completion, String prefix, Position position, ITextViewer viewer) {
		this.proposer = proposer;
		this.completion = completion;
		this.prefix = prefix;
		this.viewer = viewer;
		this.position = position;
	}

	public ContentProposal(ContentProposer proposer, Completion completion, ITextViewer viewer) {
		this(proposer, completion, "", null, viewer);
	}

	public Point getSelection(IDocument document) {
		final IStrategoList newTextParts = completion.getNewTextParts();
		if (newTextParts == null) {
			return new Point(position.getOffset() + position.getLength() - prefix.length(), 0);
		} else {
			return proposalPartsToSelection(document, newTextParts, position.getOffset() - prefix.length());
		}
	}

	public String getAdditionalProposalInfo() {
		// TODO: support newlines and tabs in proposal descriptions?
		return escapeHtml(completion.getDescription());
	}
	
	private String escapeHtml(String input) {
		if (input == null) return null;
		return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	private Point proposalPartsToSelection(IDocument document, IStrategoList proposalParts, int offset) {
		int i = 0;
		for (IStrategoList cons = proposalParts; !cons.isEmpty(); cons = cons.tail()) {
			IStrategoTerm partTerm = cons.head();
			String part = proposalPartToString(document, partTerm);
			if ("Cursor".equals(cons(partTerm))) {
				return new Point(offset + i, 0);
			}
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
			String lineStart = AutoEditStrategy.getLineBeforeOffset(document, position.getOffset());
			if ("Placeholder".equals(cons(part)) || "PlaceholderWithSort".equals(cons(part))) {
				IStrategoString placeholder = termAt(part, 0);
				String contents = placeholder.stringValue();
				contents = contents.substring(1, contents.length() - 1); // strip < >
				return AutoEditStrategy.formatInsertedText(contents, lineStart);
			}
			else if ("Cursor".equals(cons(part))) {
				return "";
			}
			else {
				return AutoEditStrategy.formatInsertedText(termContents(part), lineStart);
			}
		} catch (BadLocationException e) {
			Environment.logException("Could not format completion fragment", e);
			return termContents(part);
		}
	}

	private static class LinkedModeModelAndExitPos {
		public LinkedModeModel model;
		public int exitPos;
	}

	private LinkedModeModelAndExitPos buildLinkedModeModel(IDocument document, int offset, IStrategoList proposalParts) throws BadLocationException {
		boolean shouldLinkPlaceholders = completion.shouldLinkPlaceholders();
		LinkedModeModelAndExitPos result = new LinkedModeModelAndExitPos();
		HashMap<Object, LinkedPositionGroup> groups = new HashMap<Object, LinkedPositionGroup>();
		int i = 0;

		for (IStrategoList cons = proposalParts; !cons.isEmpty(); cons = cons.tail()) {
			IStrategoTerm partTerm = cons.head();
			String part = proposalPartToString(document, partTerm);

			if ("Placeholder".equals(cons(partTerm)) || "PlaceholderWithSort".equals(cons(partTerm))
					// HACK: we should migrate to semantic completion returning Placeholder cons too when it wants placeholders
					|| (cons != proposalParts && proposer.getCompletionLexical().matcher(part).matches())) {
				LinkedPositionGroup group = groups.get(part);

				if (group == null || !shouldLinkPlaceholders) {
					group = new LinkedPositionGroup();
					groups.put(shouldLinkPlaceholders ? part : new Object(), group);
				}

				if (partTerm.getSubtermCount() == 2 && group.isEmpty()) {
					IStrategoString sortTerm = termAt(partTerm, 1);
					ICompletionProposal[] choices = proposer.getTemplateProposalsForSort(sortTerm.stringValue(), viewer);
					LinkedPosition position = new ProposalPosition(document, offset + i, part.length(), 0, choices);
					for (ICompletionProposal proposal : choices) {
						((ContentProposal)proposal).setPosition(position);
					}
					group.addPosition(position);
				}
				else {
					group.addPosition(new LinkedPosition(document, offset + i, part.length(), group.isEmpty() ? 0 : LinkedPositionGroup.NO_STOP));
				}
			}
			else if ("Cursor".equals(cons(partTerm))) {
				result.exitPos = offset + i;
			}

			i += part.length();
		}

		if (result.exitPos == 0) {
			result.exitPos = offset + i;
		}

		if (!groups.isEmpty()) {
			LinkedModeModel model = new LinkedModeModel();
			for (LinkedPositionGroup group : groups.values()) {
				model.addGroup(group);
			}
			result.model = model;
		}

		return result;
	}

	private void goToLinkedMode(ITextViewer viewer, int offset, IDocument doc, IStrategoList proposalParts) throws BadLocationException {
		final LinkedModeModelAndExitPos result = buildLinkedModeModel(doc, offset, proposalParts);
		final LinkedModeModel model = result.model;
		if (model != null) {
			model.forceInstall();

			final LinkedModeUI ui = new EditorLinkedModeUI(model, viewer);
			ui.setExitPosition(viewer, result.exitPos, 0, Integer.MAX_VALUE);

			final Job job = new UIJob("going into linked mode") {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					ui.enter();
					return Status.OK_STATUS;
				}
			};
			job.setSystem(true);
			job.schedule();
		}
	}

	public void apply(IDocument document) {
		try {
			final IStrategoList newTextParts = completion.getNewTextParts();
			final String newText = newTextParts == null
					? completion.getPrefix()
					: proposalPartsToString(document, newTextParts);
			justApplied = true;
			document.replace(position.getOffset() - prefix.length(), position.getLength() + prefix.length(), newText);

			if (newTextParts != null) {
				goToLinkedMode(viewer, position.getOffset() - prefix.length(), document, newTextParts);
			}

		} catch (BadLocationException e) {
			Environment.logException("Could not apply content proposal", e);
		}
		proposer.onProposalApplied();
	}

	protected static boolean pollJustApplied() {
		boolean result = justApplied;
		justApplied = false;
		return result;
	}

	public String getDisplayString() {
		return completion.getName();
	}

	public StyledString getStyledDisplayString() {
		return completion.getStyledName();
	}

	public Image getImage() {
		return null;
	}

	public IContextInformation getContextInformation() {
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof ContentProposal)) return false;
		final ContentProposal that = (ContentProposal) obj;
		return equals(this.getDisplayString(), that.getDisplayString())
			&& equals(this.completion.getNewTextParts(), that.completion.getNewTextParts());
	}

	// TODO: move to some utility class/library
	private static final boolean equals(Object a, Object b) {
		return (a == null && b == null) || (a != null && a.equals(b));
	}

	@Override
	public int hashCode() {
		return this.getDisplayString().hashCode();
	}
}
