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
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.imp.editor.ErrorProposal;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IContentProposer;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Point;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.ast.AstSortInspector;
import org.strategoxt.imp.runtime.stratego.CandidateSortsPrimitive;
import org.strategoxt.imp.runtime.stratego.StrategoConsole;

/**
 * Content completion.
 *
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class ContentProposer implements IContentProposer {

	public static String COMPLETION_TOKEN = "CONTENTCOMPLETE" + Math.abs(new Random().nextInt());

	private static final boolean IGNORE_TEMPLATE_PREFIX_CASE = false;

	private final StrategoObserver observer;

	private final String completionFunction;

	private final Pattern identifierLexical;

	private final Set<Completion> templates;

	private final ContentProposerParser parser; // mutable state

	public ContentProposer(StrategoObserver observer, String completionFunction, Pattern identifierLexical, Set<Completion> templates) {
		this.observer = observer;
		this.completionFunction = completionFunction;
		this.identifierLexical = identifierLexical;
		this.templates = templates;
		this.parser = new ContentProposerParser(identifierLexical);
	}

	public ICompletionProposal[] getContentProposals(IParseController controller, int offset, ITextViewer viewer) {
		String document = viewer.getDocument().get();
		Point selectedRange = viewer.getSelectedRange();
		Position selection = new Position(selectedRange.x, selectedRange.y);

		if (!identifierLexical.matcher(COMPLETION_TOKEN).matches())
			return createErrorProposal("No proposals available - completion lexical must allow letters and numbers", offset);

		boolean avoidReparse = completionFunction == null && templates.size() == 0;
		IStrategoTerm ast = parser.parse(controller, selection, document, avoidReparse);
		int prefixLength = parser.getCompletionPrefix() == null ? 0 : parser.getCompletionPrefix().length();
		Set<String> sorts = new AstSortInspector(ast).getSortsAtOffset(offset - prefixLength, offset);
		if (parser.getCompletionNode() == null)
			return getParseFailureProposals(controller, document, offset, sorts, viewer);

		printCompletionTip(controller, sorts);

		ICompletionProposal[] results =
			computeAllCompletionProposals(invokeCompletionFunction(controller, sorts), document,
					parser.getCompletionPrefix(), offset, sorts, viewer);

		// TVO: there is an interface for this AFAIK
		/* UNDONE: automatic proposal insertion
		if (results.length == 1 && results[0] instanceof SourceProposal) {
			results[0].apply(viewer.getDocument());
			Point selection = ((SourceProposal) results[0]).getSelection(viewer.getDocument());
			viewer.setSelectedRange(selection.x, selection.y);
			insertImmediatelyOnce = false;
			return null;
		}
		*/

		return results;
	}

	public ICompletionProposal[] getTemplateProposalsForSort(String wantedSort, ITextViewer viewer) {
		Set<ICompletionProposal> results = new HashSet<ICompletionProposal>();
		for (Completion proposal : templates) {
			if (wantedSort.equals(proposal.getSort())) {
				results.add(new ContentProposal(this, proposal, viewer));
			}
		}
		return toSortedArray(results);
	}

	private ICompletionProposal[] getParseFailureProposals(IParseController controller,
			String document, int offset, Set<String> sorts, ITextViewer viewer) {

		String startSymbol = Environment.getDescriptor(controller.getLanguage()).getStartSymbol();

		if (startSymbol != null && document.trim().indexOf('\n') == -1) {
			// Empty document completion
			String prefix = parser.readPrefix(offset, document);
			sorts.add(startSymbol);
			printCompletionTip(controller, sorts);
			ICompletionProposal[] proposals = computeAllCompletionProposals(Environment.getTermFactory().makeList(), document, prefix, offset, sorts, viewer);
			if (proposals.length != 0) return proposals;
		}
		if (parser.isFatalSyntaxError()) {
			return createErrorProposal("No proposals available - syntax errors", offset);
		} else {
			return createErrorProposal("No proposals available - could not identify proposal context", offset);
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
				StrategoConsole.activateConsole(true);
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

	private IStrategoTerm invokeCompletionFunction(final IParseController controller, final Set<String> sorts) {
		if (completionFunction == null) {
			return Environment.getTermFactory().makeList();
		} else {
			// FIXME: Using the environment lock for content completion
			//        The problem here is really that content completion shouldn't
			//        run in the main thread. It might be possible to spawn a new
			//        thread and then invoke content completion again when it's done.
			class Runner implements Runnable {
				IStrategoTerm result;
				public void run() {
					observer.getLock().lock();
					try {
						CandidateSortsPrimitive.setCandidateSorts(sorts);
						if (!observer.isUpdateScheduled()) {
							observer.update(controller, new NullProgressMonitor());
						}
						IStrategoTerm input = observer.getInputBuilder().makeInputTerm(parser.getCompletionNode(), true, false);
						result = observer.invokeSilent(completionFunction, input, getResource(parser.getCompletionNode()));
						if (result == null) {
							observer.reportRewritingFailed();
							result = TermFactory.EMPTY_LIST;
						}
					} finally {
						observer.getLock().unlock();
					}
				}
			}
			Runner runner = new Runner();

			// UNDONE: causes deadlock with updater thread
			//         (which acquires the display lock to display monitor updates)
			//if (EditorState.isUIThread()) {
			//	Display.getCurrent().syncExec(runner);
			//} else {
				runner.run();
			//}
			return runner.result;
		}
	}

	private ICompletionProposal[] computeAllCompletionProposals(IStrategoTerm proposals, String document, String prefix, int offset, Set<String> sorts, ITextViewer viewer) {

		// dynamically computed blueprints, i.e. from semantic analysis
		Set<Completion> results = toCompletions(proposals, document, prefix, offset, sorts);

		if (results == null) {
			String error = "No proposals available - '" + completionFunction
					+ "' did not return a ([proposal], description) list";

			return createErrorProposal(error, offset);
		}

		// static blueprints, i.e. keywords and templates
		results.addAll(templates);

		Point selection = viewer.getSelectedRange();
		Position offsetPosition = new Position(selection.x, selection.y);

		return filterCompletions(results, document, prefix, offsetPosition, sorts, viewer);
	}

	private Set<Completion> toCompletions(IStrategoTerm proposals, String document, String prefix, int offset, Set<String> sorts) {

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

	private ICompletionProposal[] createErrorProposal(String error, int offset) {
		return new ICompletionProposal[] { new ErrorProposal(error, offset) };
	}

}
