package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.termAt;
import static org.spoofax.interpreter.terms.IStrategoTerm.LIST;
import static org.spoofax.interpreter.terms.IStrategoTerm.STRING;
import static org.spoofax.interpreter.terms.IStrategoTerm.TUPLE;
import static org.spoofax.terms.Term.tryGetConstructor;
import static org.strategoxt.imp.runtime.services.ContentProposerParser.COMPLETION_UNKNOWN;
import static org.strategoxt.imp.runtime.stratego.SourceAttachment.getResource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.editor.ErrorProposal;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IContentProposer;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.progress.UIJob;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.jsglr.client.incremental.IncrementalSortSet;
import org.spoofax.terms.TermFactory;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.ast.AstSortInspector;
import org.strategoxt.imp.runtime.stratego.CandidateSortsPrimitive;
import org.strategoxt.imp.runtime.stratego.StrategoConsole;

/**
 * Content completion.
 *
 * @author Lennart Kats <lennart add lclnet.nl>
 * @author Tobi Vollebregt
 */
public class ContentProposer implements IContentProposer {

	public static String COMPLETION_TOKEN = "CONTENTCOMPLETE" + Math.abs(new Random().nextInt());

	private static final boolean IGNORE_TEMPLATE_PREFIX_CASE = false;

	// cached mapping from sort to all sorts injecting into that sort
	private final Map<String, Set<String>> injections;

	private final StrategoObserver observer;

	private final String completionFunction;

	private final Pattern identifierLexical;

	private final Set<Completion> templates;

	private final ContentProposerParser parser; // mutable state

	// ensures only one completion job runs at a time
	private ISchedulingRule serializeJobs = new ISchedulingRule() {
		public boolean isConflicting(ISchedulingRule arg0) {
			return arg0 == serializeJobs;
		}
		public boolean contains(ISchedulingRule arg0) {
			return arg0 == serializeJobs;
		}
	};

	// protects results, lastDocument, and lastSelection
	private Object lock = new Object();

	private ICompletionProposal[] results;

	private String lastDocument;

	private Position lastSelection;

	private final BlockingQueue<ICompletionProposal[]> resultsQueue = new ArrayBlockingQueue<ICompletionProposal[]>(1);

	public ContentProposer(StrategoObserver observer, String completionFunction, Pattern identifierLexical, Set<Completion> templates) {
		this.injections = new HashMap<String, Set<String>>();
		this.observer = observer;
		this.completionFunction = completionFunction;
		this.identifierLexical = identifierLexical;
		this.templates = templates;
		this.parser = new ContentProposerParser(identifierLexical);
	}

	public ICompletionProposal[] getContentProposals(final IParseController controller, final int offset, final ITextViewer viewer) {
		final String document = viewer.getDocument().get();
		final Point selectedRange = viewer.getSelectedRange();
		final Position selection = new Position(selectedRange.x, selectedRange.y);

		synchronized(lock) {
			// Any past result waiting?
			if (results != null) {
				ICompletionProposal[] tmpResults = results;
				results = null;

				// Return that result when still valid.
				if (document.equals(lastDocument) && selection.equals(lastSelection)) {
					return tmpResults;
				}
			}

			// No => kick off a new calculation.
			lastDocument = document;
			lastSelection = selection;
		}

		if (!identifierLexical.matcher(COMPLETION_TOKEN).matches()) {
			return createErrorProposal("No proposals available - completion lexical must allow letters and numbers", selection);
		}

		Job job = new Job("Computing content proposals") {
			private boolean shouldBeCancelled(IProgressMonitor monitor) {
				// Job should be cancelled when lastDocument or lastSelection
				// has been updated by a new call to getContentProposals.
				return monitor.isCanceled()
						|| !document.equals(lastDocument)
						|| !selection.equals(lastSelection);
			}

			@Override
			public IStatus run(IProgressMonitor monitor) {
				// A new request for proposals may have come in before we even started...
				synchronized (lock) {
					if (shouldBeCancelled(monitor))
						return Status.CANCEL_STATUS;
				}

				// Parse (takes parse lock).
				// Parse might be skipped if ContentProposerParser figures it can reuse the previous AST.
				boolean avoidReparse = completionFunction == null && templates.size() == 0;
				IStrategoTerm ast = parser.parse(controller, selection, document, avoidReparse);
				int prefixLength = parser.getCompletionPrefix() == null ? 0 : parser.getCompletionPrefix().length();
				Set<String> sorts = new AstSortInspector(ast).getSortsAt(offset - prefixLength, offset + COMPLETION_TOKEN.length() - 1, parser.getCompletionNode());
				IStrategoTerm completionNode = parser.getCompletionNode();

				// Still continue?
				synchronized (lock) {
					if (shouldBeCancelled(monitor))
						return Status.CANCEL_STATUS;
				}

				// Invoke completion strategy (takes observer lock).
				ICompletionProposal[] tmpResults;

				if (completionNode == null) {
					tmpResults = getParseFailureProposals(
							controller, document, selection, sorts, viewer);
				} else {
					printCompletionTip(controller, sorts);

					tmpResults = computeAllCompletionProposals(
							invokeCompletionFunction(controller, completionNode, sorts), document,
							parser.getCompletionPrefix(), selection, sorts, viewer);
				}

				// Offer results to getContentProposals, it might still be waiting.
				// If the offer fails, don't re-trigger completion when we got no results.
				if (resultsQueue.offer(tmpResults) || tmpResults == null || tmpResults.length == 0) {
					return Status.OK_STATUS;
				}

				// Store results only if still not cancelled.
				synchronized(lock) {
					if (shouldBeCancelled(monitor)) {
						return Status.CANCEL_STATUS;
					} else {
						results = tmpResults;
					}
				}

				// Re-trigger completion.
				// Next invocation of getContentProposals will return the results.
				UIJob job = new UIJob("Re-triggering content assist") {
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						// Don't re-trigger if document/selection changed
						// between scheduling and execution of this UIJob.
						synchronized(lock) {
							if (shouldBeCancelled(monitor)
									|| viewer.getDocument() == null
									|| !viewer.getDocument().get().equals(document)
									|| !viewer.getSelectedRange().equals(selectedRange)) {
								return Status.CANCEL_STATUS;
							}
						}
						((ITextOperationTarget) viewer).doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
						return Status.OK_STATUS;
					}
				};
				job.setSystem(true);
				job.setPriority(Job.INTERACTIVE);
				job.schedule();

				return Status.OK_STATUS;
			}
		};

		job.setSystem(true);
		job.setPriority(Job.INTERACTIVE);
		job.setRule(serializeJobs);
		resultsQueue.clear();
		job.schedule();

		try {
			// Wait up to 500 ms, maybe the job finishes quickly.
			// This greatly reduces "flickering" of the completion popup
			// when it is open while typing text or moving the cursor.
			ICompletionProposal[] tmpResults = resultsQueue.poll(500, TimeUnit.MILLISECONDS);

			// Block the queue by putting a dummy element in it.
			// This will signal the job to re-trigger content assist.
			if (!resultsQueue.offer(new ICompletionProposal[0])) {
				// If offer fails then the job finished between the poll and the offer.
				tmpResults = resultsQueue.poll();
			}

			// If tmpResults == null, then popup goes away,
			// will pop up again when the job finishes.

			// Returning a "Be patient" ErrorProposal here doesn't work:
			// the completion re-trigger is ignored if the popup is open...

			return tmpResults;
		} catch (InterruptedException e) {
			return null;
		}
	}

	public ICompletionProposal[] getTemplateProposalsForSort(String wantedSort, ITextViewer viewer) {
		// Add templates for sorts injected into wantedSort.
		//  `sort -> wantedSort' => add templates for sort
		//  `sub -> sort' => add templates for sub too

		final Set<ICompletionProposal> results = new HashSet<ICompletionProposal>();
		Set<String> wantedSorts = injections.get(wantedSort);

		if (wantedSorts == null) {
			final ParseTable pt = parser.getParser().getParser().getParseTable();
			final IncrementalSortSet iss = IncrementalSortSet.create(pt, true, false, wantedSort);
			wantedSorts = iss.getIncrementalSorts();
			injections.put(wantedSort, wantedSorts);
		}

		for (Completion proposal : templates) {
			if (wantedSorts.contains(proposal.getSort())) {
				results.add(new ContentProposal(this, proposal, viewer));
			}
		}

		return toSortedArray(results);
	}

	private ICompletionProposal[] getParseFailureProposals(IParseController controller,
			String document, Position selection, Set<String> sorts, ITextViewer viewer) {

		String startSymbol = Environment.getDescriptor(controller.getLanguage()).getStartSymbol();

		if (startSymbol != null && document.trim().indexOf('\n') == -1) {
			// Empty document completion
			String prefix = parser.readPrefix(selection.getOffset(), document);
			sorts.add(startSymbol);
			printCompletionTip(controller, sorts);
			ICompletionProposal[] proposals = computeAllCompletionProposals(Environment.getTermFactory().makeList(), document, prefix, selection, sorts, viewer);
			if (proposals.length != 0) return proposals;
		}
		if (parser.isFatalSyntaxError()) {
			return createErrorProposal("No proposals available - syntax errors", selection);
		} else {
			return createErrorProposal("No proposals available - could not identify proposal context", selection);
		}
	}

	private void printCompletionTip(IParseController controller, Set<String> sorts) {
		if (Environment.getDescriptor(controller.getLanguage()).isDynamicallyLoaded()) {
			try {
				IStrategoTerm currentCompletionNode = parser.getCompletionNode();
				String parseErrorHelp = currentCompletionNode != null
					&& tryGetConstructor(currentCompletionNode) == COMPLETION_UNKNOWN
					? "\n   (context could not be parsed with this grammar; see the FAQ.)"
					: "";
				StrategoConsole.getOutputWriter().write(
					":: Completion triggered for: " + currentCompletionNode
					+ parseErrorHelp
					+ " (candidate sorts: " + sorts + ")" + "\n");

				// why would you want to do that?
				// StrategoConsole.activateConsole(true);
			} catch (IOException e) {
				Environment.logWarning("Could not write to console", e);
			}
		}
	}

	protected Pattern getCompletionLexical() {
		return identifierLexical;
	}

	protected ContentProposerParser getParser() {
		return parser;
	}

	protected void onProposalApplied() {
		observer.setRushNextUpdate(true);
		parser.getParser().getErrorHandler().setRushNextUpdate(true);
		parser.getParser().scheduleParserUpdate(0, false);
	}

	private IStrategoTerm invokeCompletionFunction(final IParseController controller, IStrategoTerm completionNode, final Set<String> sorts) {
		if (completionFunction == null) {
			return TermFactory.EMPTY_LIST;
		} else {
			IStrategoTerm result;
			observer.getLock().lock();
			try {
				CandidateSortsPrimitive.setCandidateSorts(sorts);
				if (!observer.isUpdateScheduled()) {
					observer.update(controller, new NullProgressMonitor());
				}
				IStrategoTerm input = observer.getInputBuilder().makeInputTerm(completionNode, true, false);
				result = observer.invokeSilent(completionFunction, input, getResource(completionNode));
				if (result == null) {
					observer.reportRewritingFailed();
					result = TermFactory.EMPTY_LIST;
				}
			} finally {
				observer.getLock().unlock();
			}
			return result;
		}
	}

	private ICompletionProposal[] computeAllCompletionProposals(IStrategoTerm proposals, String document, String prefix, Position selection, Set<String> sorts, ITextViewer viewer) {

		// dynamically computed blueprints, i.e. from semantic analysis
		Set<Completion> results = toCompletions(proposals, document, prefix, selection, sorts);

		if (results == null) {
			String error = "No proposals available - '" + completionFunction
					+ "' did not return a ([proposal], description) list";

			return createErrorProposal(error, selection);
		}

		// static blueprints, i.e. keywords and templates
		results.addAll(templates);

		return filterCompletions(results, document, prefix, selection, sorts, viewer);
	}

	private Set<Completion> toCompletions(IStrategoTerm proposals, String document, String prefix, Position selection, Set<String> sorts) {

		if (proposals.getTermType() != LIST)
			return null;

		final ITermFactory factory = Environment.getTermFactory();
		final IStrategoString emptyString = factory.makeString("");
		final Set<Completion> results = new HashSet<Completion>();

		for (IStrategoList cons = (IStrategoList) proposals; !cons.isEmpty(); cons = cons.tail()) {
			IStrategoTerm proposal = cons.head();
			IStrategoList newTextParts;
			IStrategoString description;

			switch (proposal.getTermType()) {
				case STRING:
					newTextParts = factory.makeList(proposal);
					description = emptyString;
					break;

				case LIST:
					newTextParts = (IStrategoList) proposal;
					description = emptyString;
					break;

				case TUPLE:
					if (proposal.getSubtermCount() != 2)
						return null;
					IStrategoTerm newTextTerm = termAt(proposal, 0);
					switch (newTextTerm.getTermType()) {
						case STRING: newTextParts = factory.makeList(newTextTerm); break;
						case LIST:   newTextParts = (IStrategoList) newTextTerm;   break;
						default:     return null;
					}
					description = termAt(proposal, 1);
					break;

				default:
					return null;
			}

			// empty list of new text parts is wrong
			if (newTextParts.isEmpty() || termAt(newTextParts, 0).getTermType() != STRING) {
				return null;
			}

			// description must be a string
			if (description.getTermType() != STRING) {
				return null;
			}

			results.add(Completion.makeSemantic(newTextParts, description.stringValue()));
		}

		return results;
	}

	private static ICompletionProposal[] toSortedArray(Set<ICompletionProposal> results) {
		ICompletionProposal[] resultArray = results.toArray(new ICompletionProposal[results.size()]);

		Arrays.sort(resultArray, new Comparator<ICompletionProposal>() {
			public int compare(ICompletionProposal o1, ICompletionProposal o2) {
				return o1.getDisplayString().compareToIgnoreCase(o2.getDisplayString());
			}
		});
		return resultArray;
	}

	ICompletionProposal[] filterCompletions(Set<Completion> completions, String document, String prefix,
			Position offsetPosition, Set<String> sorts, ITextViewer viewer) {

		final Set<ICompletionProposal> results = new HashSet<ICompletionProposal>();
		final int offset = offsetPosition.getOffset();
		boolean backTrackResultsOnly = false;

		for (Completion proposal : completions) {
			String proposalPrefix = proposal.getPrefix();
			if (proposal.getSort() != null && !sorts.contains(proposal.getSort()))
				continue;
			if (!backTrackResultsOnly && proposalPrefix.regionMatches(IGNORE_TEMPLATE_PREFIX_CASE, 0, prefix, 0, prefix.length())) {
				if (!proposal.isBlankLineRequired() || isBlankBeforeOffset(document, offset - prefix.length()))
					if (proposal.isSemantic() || prefix.length() > 0 || proposalPrefix.length() == 0 || identifierLexical.matcher(proposalPrefix).lookingAt())
						results.add(new ContentProposal(this, proposal, prefix, offsetPosition, viewer));
			}
			if (prefix.length() == 0) {
				// find longest match of proposal in document
				for (int i = proposalPrefix.length() - 1; i > 0; i--) {
					if (document.regionMatches(true, offset - i, proposalPrefix, 0, i)) {
						if (!backTrackResultsOnly) results.clear();
						backTrackResultsOnly = true;
						results.add(new ContentProposal(this, proposal, proposalPrefix.substring(0, i), offsetPosition, viewer));
						break;
					}
				}
//				Matcher matcher = identifierLexical.matcher(proposalPrefix);
//				if (matcher.find() && (matcher.start() > 0 || matcher.end() < proposalPrefix.length())) {
//					// Handle completion literals with special characters, like "(disabled)"
//					if (matcher.start() == 0 && !matcher.find(matcher.end()))
//						continue;
//					do {
//						if (document.regionMatches(offset - matcher.start() - prefix.length(), proposalPrefix, 0, matcher.start())
//								&& proposalPrefix.regionMatches(matcher.start(), prefix, 0, prefix.length())) {
//
//							// TODO: respect proposal.isBlankLineRequired() here?
//							String bigPrefix = proposalPrefix.substring(0, matcher.start() + prefix.length());
//							if (!backTrackResultsOnly) results.clear();
//							backTrackResultsOnly = true;
//							results.add(new ContentProposal(this, proposal, bigPrefix, offsetPosition, viewer));
//							break;
//						}
//					} while (matcher.find(matcher.end()));
//				}
			}
		}

		return toSortedArray(results);
	}

	private static boolean isBlankBeforeOffset(String document, int offset) {
		for (int i = offset - 1; i > 0; i--) {
			switch (document.charAt(i)) {
				case ' ': case '\t': continue;
				case '\n': return true;
				default: return false;
			}
		}
		return true;
	}

	private ICompletionProposal[] createErrorProposal(String error, Position selection) {
		return new ICompletionProposal[] { new ErrorProposal(error, selection.getOffset()) };
	}

}
