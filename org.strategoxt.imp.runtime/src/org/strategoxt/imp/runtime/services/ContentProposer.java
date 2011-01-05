package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.asJavaString;
import static org.spoofax.interpreter.core.Tools.termAt;
import static org.spoofax.interpreter.terms.IStrategoTerm.LIST;
import static org.spoofax.interpreter.terms.IStrategoTerm.STRING;
import static org.spoofax.interpreter.terms.IStrategoTerm.TUPLE;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getSort;
import static org.spoofax.terms.Term.isTermList;
import static org.spoofax.terms.Term.isTermString;
import static org.spoofax.terms.Term.tryGetConstructor;
import static org.spoofax.terms.attachments.ParentAttachment.getParent;
import static org.spoofax.terms.attachments.ParentAttachment.setParent;
import static org.spoofax.terms.attachments.ParentAttachment.getRoot;
import static org.strategoxt.imp.runtime.Environment.getTermFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.imp.editor.ErrorProposal;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IContentProposer;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.shared.SGLRException;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.TermVisitor;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.DynamicParseController;
import org.strategoxt.imp.runtime.parser.JSGLRI;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.parser.ast.AstSortInspector;
import org.strategoxt.imp.runtime.stratego.CandidateSortsPrimitive;
import org.strategoxt.imp.runtime.stratego.SourceAttachment;
import org.strategoxt.imp.runtime.stratego.StrategoConsole;

/**
 * Content completion.
 * 
 * Works in 6 or so steps:
 * 
 * 1. control-space/completion token event
 * 2. create source text with "_CONTENT_COMPLETE_a124142_" dummy literal
 * 3a parse to AST (only use it for content completion because of dummy literal)
 * 3b force-insert dummy literal if not found in ast
 * 3c if parsing fails, use an old AST and apply 3b
 * 4. analyze complete file:
 *    store completion suggestions for dummy literal in dyn rule and return it
 * 5. present completion suggestions to user
 * 6. reparse and reanalyze to fix position info
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class ContentProposer implements IContentProposer {

	public static String COMPLETION_TOKEN = "CONTENTCOMPLETE" + Math.abs(new Random().nextInt());
	
	private static final boolean IGNORE_TEMPLATE_PREFIX_CASE = false;
	
	private static final IStrategoConstructor COMPLETION_CONSTRUCTOR =
		getTermFactory().makeConstructor("COMPLETION", 0);
	
	private static final IStrategoConstructor COMPLETION_UNKNOWN =
		getTermFactory().makeConstructor("NOCONTEXT", 0);

	private static final long REINIT_PARSE_DELAY = 4000;
	
	private final StringBuilder proposalBuilder = new StringBuilder();
	
	private final StrategoObserver observer;
	
	private final String completionFunction;
	
	private final Pattern identifierLexical;
	
	private final String[] keywords;
	
	private final ContentProposalTemplate[] templates;
	
	private SGLRParseController parser;
	
	private IStrategoTerm lastParserAst;
	
	private IStrategoTerm lastCompletionAst;
	
	private IStrategoTerm lastCompletionNode;
	
	private String lastDocument;
	
	private int lastOffset;
	
	private IStrategoTerm currentCompletionNode;
	
	private String currentCompletionPrefix;
	
	private String lastCompletionPrefix;
	
	public ContentProposer(StrategoObserver observer, String completionFunction, Pattern identifierLexical, String[] keywords, ContentProposalTemplate[] templates) {
		this.observer = observer;
		this.completionFunction = completionFunction;
		this.identifierLexical = identifierLexical;
		this.keywords = keywords;
		this.templates = templates;
	}
	
	public ICompletionProposal[] getContentProposals(IParseController controller, int offset, ITextViewer viewer) {
		lastCompletionNode = currentCompletionNode;
		lastCompletionPrefix = currentCompletionPrefix;
		currentCompletionNode = null;
		String document = viewer.getDocument().get();
		
		if (!identifierLexical.matcher(COMPLETION_TOKEN).matches())
			return createErrorProposal("No proposals available - completion lexical must allow letters and numbers", offset);
		
		IStrategoTerm ast = constructAst(getParser(controller), offset, document);
		int prefixLength = currentCompletionPrefix == null ? 0 : currentCompletionPrefix.length();
		Set<String> sorts = new AstSortInspector(ast).getSortsAtOffset(offset - prefixLength, offset);
		if (currentCompletionNode == null)
			return getParseFailureProposals(controller, document, offset, sorts);

		printCompletionTip(controller, sorts);

		ICompletionProposal[] results = toProposals(invokeCompletionFunction(controller, sorts), document, offset, sorts);
		
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

	private ICompletionProposal[] getParseFailureProposals(IParseController controller,
			String document, int offset, Set<String> sorts) {
		
		String startSymbol = Environment.getDescriptor(controller.getLanguage()).getStartSymbol();
		
		if (startSymbol != null && document.trim().indexOf('\n') == -1) {
			currentCompletionPrefix = getPrefix(offset, document);
			sorts.add(startSymbol);
			printCompletionTip(controller, sorts);
			ICompletionProposal[] proposals = toProposals(TermFactory.EMPTY_LIST, document, offset, sorts);
			if (proposals.length != 0) return proposals;
		}
		if (lastCompletionAst == null && lastParserAst == null) {
			return createErrorProposal("No proposals available - syntax errors", offset);
		} else {
			return createErrorProposal("No proposals available - could not identify proposal context", offset);
		}
	}

	private void printCompletionTip(IParseController controller, Set<String> sorts) {
		if (Environment.getDescriptor(controller.getLanguage()).isDynamicallyLoaded()) {
			try {
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
	
	protected SGLRParseController getParser() {
		return parser;
	}
	
	protected StrategoObserver getObserver() {
		return observer;
	}

	private IStrategoTerm constructAst(SGLRParseController controller, int offset, String document) {
		this.parser = controller;
		IStrategoTerm ast = tryReusePreviousAst(offset, document);
		if (ast != null) return ast;

		String documentWithToken = document.substring(0, offset) + COMPLETION_TOKEN + document.substring(offset);
		ast = parse(controller, offset, documentWithToken);
		if (ast == null) return null;
		
		IStrategoTerm result = identifyCompletionNode(ast, COMPLETION_TOKEN);
		if (currentCompletionNode == null) result = insertNewCompletionNode(ast, offset, document);
		return result;
	}

	private IStrategoTerm parse(SGLRParseController controller, int offset, String document) {
		JSGLRI parser = controller.getParser();
		IStrategoTerm result;
		
		controller.scheduleParserUpdate(REINIT_PARSE_DELAY, true); // cancel current parse
		Debug.startTimer();
		controller.getParseLock().lock();
		Debug.stopTimer("Completion acquired parse lock");
		try {
			if (completionFunction == null && controller.getCurrentAst() != null) {
				// Don't reparse for keyword completion
				result = controller.getCurrentAst().cloneIgnoreTokens();
			} else {
				Debug.startTimer();
				try {
					result = parser.parse(document.toCharArray(), controller.getPath().toPortableString());
				} finally {
					Debug.stopTimer("Completion parsed");
				}
				lastParserAst = controller.getCurrentAst();
				lastCompletionAst = result.cloneIgnoreTokens();
			}
		} catch (SGLRException e) {
			Environment.logException("Could not reparse input for content completion", e);
			result = forceUseOldAst(controller, offset, document);
		} catch (IOException e) {
			Environment.logException("Could not reparse input for content completion", e);
			result = forceUseOldAst(controller, offset, document);
		} finally {
			controller.getParseLock().unlock();
		}
		return result;
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
						IStrategoTerm input = observer.makeInputTerm(currentCompletionNode, true, false);
						result = observer.invokeSilent(completionFunction, input, SourceAttachment.getResource(currentCompletionNode));
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
	
	private ICompletionProposal[] toProposals(IStrategoTerm proposals, String document, int offset, Set<String> sorts) {
		Debug.startTimer();
		
		String error = "No proposals available - '" + completionFunction
				+ "' did not return a ([proposal], description) list";

		if (proposals.getTermType() != LIST)
			return createErrorProposal(error, offset);

		ITermFactory factory = Environment.getTermFactory();
		IStrategoString emptyString = factory.makeString("");
		Region offsetRegion = new Region(offset, 0);
		String prefix = currentCompletionPrefix;
		
		Set<ICompletionProposal> results = getKeywordAndTemplateProposals(document, prefix, offsetRegion, offset, sorts);

		for (IStrategoList cons = (IStrategoList) proposals; !cons.isEmpty(); cons = cons.tail()) {
			IStrategoTerm proposal = cons.head();
			boolean confirmed = false;
			String newText;
			IStrategoList newTextParts;
			IStrategoString description;
			
			if (proposal.getTermType() == STRING) {
				newTextParts = factory.makeList(proposal);
				newText = ((IStrategoString) proposal).stringValue();
				description = emptyString;
			} else if (proposal.getTermType() == LIST) {
				newTextParts = (IStrategoList) proposal;
				String head = newTextParts.size() == 0 ? "" : asJavaString(newTextParts.head());
				if (head.length() >= prefix.length()) {
					if (startsWithCaseInsensitive(head, prefix)) confirmed = true;
					else continue;
				}
				newText = proposalPartsToDisplayString(newTextParts);
				description = emptyString;
			} else {
				IStrategoTerm newTextTerm = termAt(proposal, 0);
				if (proposal.getTermType() != TUPLE || proposal.getSubtermCount() != 2
						|| (newTextTerm.getTermType() != LIST && termAt(proposal, 0).getTermType() != STRING)
						|| termAt(proposal, 1).getTermType() != STRING)
					return createErrorProposal(error, offset);
				if (newTextTerm.getTermType() == LIST) {
					newTextParts = (IStrategoList) newTextTerm;
					String head = newTextParts.size() == 0 ? "" : asJavaString(newTextParts.head());
					if (head.length() >= prefix.length()) {
						if (startsWithCaseInsensitive(head, prefix)) confirmed = true;
						else continue;
					}
					newText = proposalPartsToDisplayString(newTextParts);
				} else {
					newTextParts = factory.makeList(newTextTerm);
					newText = ((IStrategoString) newTextTerm).stringValue();
				}
				description = termAt(proposal, 1);
			}
			if (!confirmed && (newTextParts.isEmpty() || !startsWithCaseInsensitive(newText,prefix)))
				continue;
			results.add(new ContentProposal(this, newText, newText, prefix, offsetRegion, newTextParts, description.stringValue()));
		}
		
		return toSortedArray(results);
	}
	
	private static boolean startsWithCaseInsensitive(String s, String prefix) {
		return s.length() >= prefix.length() && s.regionMatches(true, 0, prefix, 0, prefix.length());
	}
	
	private String proposalPartsToDisplayString(IStrategoList proposalParts) {
		proposalBuilder.setLength(0);
		for (IStrategoList cons = proposalParts; !cons.isEmpty(); cons = cons.tail()) {
			IStrategoTerm part = cons.head();
			if (part.getTermType() != STRING) return null;
			proposalBuilder.append(asJavaString(part));
		}
		if (proposalBuilder.indexOf("\\n") != -1 || proposalBuilder.indexOf("\\t") != -1) {
			return asJavaString(proposalParts.head());
		} else {
			return proposalBuilder.toString();
		}
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
	
	private Set<ICompletionProposal> getKeywordAndTemplateProposals(String document, String prefix,
			Region offsetRegion, int offset, Set<String> sorts) {
		
		Set<ICompletionProposal> results = new HashSet<ICompletionProposal>();
		boolean backTrackResultsOnly = false;
		
		// TODO: simplify - turn completion keywords in completion templates?
		for (String proposal : keywords) {
			if (!backTrackResultsOnly && proposal.regionMatches(IGNORE_TEMPLATE_PREFIX_CASE, 0, prefix, 0, prefix.length())) {
				if (prefix.length() > 0 || identifierLexical.matcher(proposal).lookingAt())
					results.add(new ContentProposal(this, proposal, proposal, prefix, offsetRegion,
							offset + proposal.length() - prefix.length(), ""));
			} /*else*/ {
				Matcher matcher = identifierLexical.matcher(proposal);
				if (matcher.find() && (matcher.start() > 0 || matcher.end() < proposal.length())) {
					// Handle completion literals with special characters, like "(disabled)"
					if (matcher.start() == 0 && !matcher.find(matcher.end()))
						continue;
					do {
						if (document.regionMatches(offset - matcher.start() - prefix.length(), proposal, 0, matcher.start()) 
								&& proposal.regionMatches(matcher.start(), prefix, 0, prefix.length())) {
							
							String subProposal = proposal.substring(matcher.start());
							if (!backTrackResultsOnly) results.clear();
							backTrackResultsOnly = true;
							results.add(new ContentProposal(this, proposal, subProposal, prefix,
									offsetRegion, offset + matcher.end() - matcher.start() - prefix.length(), ""));
							break;
						}
					} while (matcher.find(matcher.end()));
				}
			}
		}
		
		for (ContentProposalTemplate proposal : templates) {
			String proposalPrefix = proposal.getPrefix();
			if (proposal.getSort() != null && !sorts.contains(proposal.getSort()))
				continue;
			if (!backTrackResultsOnly && proposalPrefix.regionMatches(IGNORE_TEMPLATE_PREFIX_CASE, 0, prefix, 0, prefix.length())) {
				if (!proposal.isBlankLineRequired() || isBlankBeforeOffset(document, offset - prefix.length()))
					if (prefix.length() > 0 || identifierLexical.matcher(proposalPrefix).lookingAt() || proposalPrefix.length() == 0)
						results.add(new ContentProposal(this, proposal.getPrefix(), proposal, prefix, offsetRegion));
			} /*else*/ {
				Matcher matcher = identifierLexical.matcher(proposalPrefix);
				if (matcher.find() && (matcher.start() > 0 || matcher.end() < proposalPrefix.length())) {
					// Handle completion literals with special characters, like "(disabled)"
					if (matcher.start() == 0 && !matcher.find(matcher.end()))
						continue;
					do {
						if (document.regionMatches(offset - matcher.start() - prefix.length(), proposalPrefix, 0, matcher.start()) 
								&& proposalPrefix.regionMatches(matcher.start(), prefix, 0, prefix.length())) {
							
							// TODO: respect proposal.isBlankLineRequired() here?
							String bigPrefix = proposalPrefix.substring(0, matcher.start() + prefix.length());
							if (!backTrackResultsOnly) results.clear();
							backTrackResultsOnly = true;
							results.add(new ContentProposal(this, proposal.getPrefix(), proposal, bigPrefix, offsetRegion));
							break;
						}
					} while (matcher.find(matcher.end()));
				}
			}
		}
		
		return results;
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

	/**
	 * Reuse the previous AST if the user just added or deleted a single character.
	 */
	private IStrategoTerm tryReusePreviousAst(int offset, String document) {
		if (offset == 0) return null;
		if (lastCompletionNode != null && lastDocument.length() == document.length() - 1 && lastOffset == offset - 1) {
			// Reuse document, ignoring latest typed character
			String newCharacter = document.substring(offset - 1, offset);
			String previousDocument = lastDocument.substring(0, offset - 1) + newCharacter + lastDocument.substring(offset - 1);
			if (document.equals(previousDocument))
				return reusePreviousAst(offset, document, lastCompletionPrefix + newCharacter);
		} else if (lastCompletionNode != null && lastCompletionPrefix.length() > 0
				&& lastDocument.length() == document.length() + 1 && lastOffset == offset + 1) {
			// Reuse document, ignoring previously typed character
			String previousDocument = lastDocument.substring(0, offset) + lastDocument.substring(offset + 1);
			if (document.equals(previousDocument))
				return reusePreviousAst(offset, document, lastCompletionPrefix.substring(0, lastCompletionPrefix.length() - 1));
		} else if (lastCompletionNode != null && lastDocument.equals(document) && offset == lastOffset) {
			return reusePreviousAst(offset, document, lastCompletionPrefix);
		}
		lastDocument = document;
		lastOffset = offset;
		return null;
	}

	private IStrategoTerm reusePreviousAst(int offset, String document, String prefix) {
		currentCompletionPrefix = prefix;
		lastDocument = document;
		lastOffset = offset;
		String prefixInAst = sanitizePrefix(currentCompletionPrefix);
		if (prefixInAst == null)
			return null;
		currentCompletionNode = lastCompletionNode;
		replacePrefix(currentCompletionNode, prefixInAst);
		return lastCompletionAst;
	}

	private String sanitizePrefix(String prefix) {
		Matcher matcher = identifierLexical.matcher(prefix);
		if (prefix.length() == 0) {
			return "";
		} else if (matcher.lookingAt()) {
			return prefix.substring(0, matcher.end());
		} else {
			return null;
		}
	}
	
	private IStrategoTerm forceUseOldAst(SGLRParseController parser, int offset, String document) {
		if (parser.getCurrentAst() != lastParserAst) { // parser has a more recent AST
			lastParserAst = parser.getCurrentAst();
			lastCompletionAst = parser.getCurrentAst();
			currentCompletionNode = null;
		} else {
			if (currentCompletionNode == null)
				return null;
			replacePrefix(currentCompletionNode, getPrefix(offset, document));
		}
		return lastCompletionAst;
	}

	private static SGLRParseController getParser(IParseController controller) {
		if (controller instanceof DynamicParseController)
			controller = ((DynamicParseController) controller).getWrapped();
		return (SGLRParseController) controller;
	}
	
	private IStrategoTerm identifyCompletionNode(IStrategoTerm ast, final String completionToken) {
		class Visitor extends TermVisitor {	
			public void preVisit(IStrategoTerm node) {
				if (isTermString(node)) {
					String value = ((IStrategoString) node).stringValue();
					if (value.indexOf(completionToken) > -1) {
						replaceCompletionNode(node, value.replace(completionToken, ""));
					}
				}
			}
		}
		new Visitor().visit(ast);
		return ast;
	}

	private void replaceCompletionNode(IStrategoTerm node, String value) {
		ArrayList<IStrategoTerm> siblings = getParent(node).getChildren();
		int siblingIndex = siblings.indexOf(node);
		IStrategoTerm newNode = createCompletionNode(value, getLeftToken(node), getRightToken(node));
		setParent(newNode, getParent(node));
		currentCompletionNode = getParent(node); // add a bit of context
		siblings.set(siblingIndex, newNode);
	}

	private IStrategoTerm createCompletionNode(String prefix, IToken left, IToken right) {
		ArrayList<IStrategoTerm> children = new ArrayList<IStrategoTerm>();
		children.add(new StringAstNode(prefix, null, left, right));
		currentCompletionPrefix = prefix;
		currentCompletionNode = new IStrategoTerm(null, left, right, COMPLETION_CONSTRUCTOR, children);
		return currentCompletionNode;
	}
	
	private IStrategoTerm insertNewCompletionNode(IStrategoTerm ast, final int offset, String document) {
		class Visitor extends TermVisitor {
			IStrategoTerm targetNode, lastNode;
			
			public void preVisit(IStrategoTerm node) {
				if (getLeftToken(node).getStartOffset() <= offset
						&& offset <= getRightToken(node).getEndOffset()) {
					targetNode = node;
				}
				lastNode = node;
			}
		}
		Visitor visitor = new Visitor();
		visitor.visit(ast);
		if (visitor.targetNode != null)
			insertNewCompletionNode(visitor.targetNode, getPrefix(offset, document));
		else
			insertNewCompletionNode(visitor.lastNode, getPrefix(offset, document));
		return ast;
	}
	
	private String getPrefix(int offset, String document) {
		int prefixStart = offset;
		int lastGoodPrefixStart = offset;
		while (--prefixStart >= 0) {
			String prefix = document.substring(prefixStart, offset);
			if (identifierLexical.matcher(prefix).matches()) {
				lastGoodPrefixStart = prefixStart;
			} else if (prefix.charAt(0) == '\n') {
				return document.substring(lastGoodPrefixStart, offset);
			}
		}
		return document.substring(0, offset);
	}
	
	private void replacePrefix(IStrategoTerm completionNode, String prefix) {
		if (completionNode.getConstructor() != COMPLETION_CONSTRUCTOR) {
			for (Object child : completionNode.getChildren()) {
				if (((IStrategoTerm) child).getConstructor() == COMPLETION_CONSTRUCTOR)
					completionNode = (IStrategoTerm) child;
			}
		}
		StringAstNode prefixNode = (StringAstNode) completionNode.getSubterm(0);
		prefixNode.setValue(prefix);
	}
	
	private void insertNewCompletionNode(IStrategoTerm node, String prefix) {
		// Create a new UNKNOWN(COMPLETION(prefix)) node
		IStrategoTerm result = createCompletionNode(prefix, getLeftToken(node), getRightToken(node));
		ArrayList<IStrategoTerm> newNodeContainer = new ArrayList<IStrategoTerm>(1);
		newNodeContainer.add(result);
		currentCompletionNode = result = new IStrategoTerm(null, getLeftToken(node), getRightToken(node), COMPLETION_UNKNOWN, newNodeContainer);
		
		// Insert the node in a list near the textual input location
		if (isTermList(node)) {
			node.getChildren().add(result);
			setParent(result, node);
			return;
		}
		for (IStrategoTerm parent = getParent(node); parent != null; node = parent, parent = getParent(parent)) {
			if (isTermList(parent)) {
				int i = parent.getChildren().indexOf(node);
				parent.getChildren().add(i + 1, result);
				setParent(result, parent);
				return;
			}
		}
		setParent(result, getRoot(node));
		return;
	}
	
}
