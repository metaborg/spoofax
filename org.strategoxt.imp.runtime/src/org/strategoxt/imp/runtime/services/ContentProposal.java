package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.termAt;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.cons;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.termContents;

import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.editor.SourceProposal;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.viewers.StyledString;
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
public class ContentProposal extends SourceProposal implements ICompletionProposalExtension6 {

	private static volatile boolean justApplied;

	private final ContentProposer proposer;

	private final IStrategoList newTextParts;

	private final Completion completion;

	private final ITextViewer viewer;

	public ContentProposal(ContentProposer proposer, Completion completion, String prefix, Region region, ITextViewer viewer) {
		super(completion.getName(), completion.getPrefix(), prefix, region, null);
		this.proposer = proposer;
		this.newTextParts = completion.getNewTextParts();
		this.completion = completion;
		this.viewer = viewer;
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
		return completion.getDescription();
	}

	private Point proposalPartsToSelection(IDocument document, IStrategoList proposalParts, int offset) {
		int i = termContents(proposalParts.head()).length();
		for (IStrategoList cons = proposalParts.tail(); !cons.isEmpty(); cons = cons.tail()) {
			IStrategoTerm partTerm = cons.head();
			String part = proposalPartToString(document, partTerm);
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
			if ("Placeholder".equals(cons(part))) {
				IStrategoString placeholder = termAt(part, 0);
				String contents = placeholder.stringValue();
				contents = contents.substring(1, contents.length() - 1); // strip < >
				return AutoEditStrategy.formatInsertedText(contents, lineStart);
			} else {
				return AutoEditStrategy.formatInsertedText(termContents(part), lineStart);
			}
		} catch (BadLocationException e) {
			Environment.logException("Could not format completion fragment", e);
			return termContents(part);
		}
	}

	private LinkedModeModel buildLinkedModeModel(IDocument document, int offset, IStrategoList proposalParts) throws BadLocationException {
		HashMap<String, LinkedPositionGroup> groups = new HashMap<String, LinkedPositionGroup>();
		int i = 0;
		for (IStrategoList cons = proposalParts; !cons.isEmpty(); cons = cons.tail()) {
			IStrategoTerm partTerm = cons.head();
			String part = proposalPartToString(document, partTerm);
			if ("Placeholder".equals(cons(partTerm))
					// HACK: we should migrate to semantic completion returning Placeholder cons too when it wants placeholders
					|| (cons != proposalParts && proposer.getCompletionLexical().matcher(part).matches())) {
				LinkedPositionGroup group = groups.get(part);
				if (group == null) {
					group = new LinkedPositionGroup();
					groups.put(part, group);
				}
				group.addPosition(new LinkedPosition(document, offset + i, part.length(), 0));
			}
			i += part.length();
		}
		if (!groups.isEmpty()) {
			LinkedModeModel model = new LinkedModeModel();
			for (LinkedPositionGroup group : groups.values()) {
				model.addGroup(group);
			}
			return model;
		}
		return null;
	}

	private void goToLinkedMode(ITextViewer viewer, int offset, IDocument doc, int exitPos, IStrategoList proposalParts) throws BadLocationException {
		final LinkedModeModel model = buildLinkedModeModel(doc, offset, proposalParts);
		if (model != null) {
			model.forceInstall();

			final LinkedModeUI ui = new EditorLinkedModeUI(model, viewer);
			ui.setExitPosition(viewer, exitPos, 0, Integer.MAX_VALUE);

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

	@Override
	public void apply(IDocument document) {
		try {
	        Region range = getRange();
			String newText = newTextParts == null
					? getNewText()
					: proposalPartsToString(document, newTextParts);
			justApplied = true;
			document.replace(range.getOffset(), range.getLength(), newText.substring(getPrefix().length()));

			if (newTextParts != null) {
				Point selection = proposalPartsToSelection(document, newTextParts, range.getOffset() - getPrefix().length());
				goToLinkedMode(viewer, range.getOffset() - getPrefix().length(), document, selection.x, newTextParts);
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

	@Override
	public String getNewText() {
		assert newTextParts == null : "Don't use me if newTextParts != null";
		return super.getNewText();
	}

	public StyledString getStyledDisplayString() {
		return completion.getStyledName();
	}
}
