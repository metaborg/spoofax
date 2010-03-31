package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.asJavaString;
import static org.spoofax.interpreter.core.Tools.termAt;
import static org.spoofax.interpreter.terms.IStrategoTerm.LIST;
import static org.spoofax.interpreter.terms.IStrategoTerm.STRING;
import static org.spoofax.interpreter.terms.IStrategoTerm.TUPLE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lpg.runtime.IToken;

import org.eclipse.imp.editor.ErrorProposal;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IContentProposer;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.SGLRException;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.DynamicParseController;
import org.strategoxt.imp.runtime.parser.JSGLRI;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.parser.ast.AbstractVisitor;
import org.strategoxt.imp.runtime.parser.ast.AstNode;
import org.strategoxt.imp.runtime.parser.ast.ListAstNode;
import org.strategoxt.imp.runtime.parser.ast.RootAstNode;
import org.strategoxt.imp.runtime.parser.ast.StringAstNode;
import org.strategoxt.imp.runtime.stratego.adapter.WrappedAstNodeFactory;
import org.strategoxt.lang.terms.TermFactory;

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
	
	private static final String COMPLETION_CONSTRUCTOR = "COMPLETION";
	
	private static final String COMPLETION_UNKNOWN = "NOCONTEXT";

	private static final long REINIT_PARSE_DELAY = 4000;
	
	private final StringBuilder proposalBuilder = new StringBuilder();
	
	private final StrategoObserver observer;
	
	private final String completionFunction;
	
	private final Pattern identifierLexical;
	
	private final String[] keywords;
	
	private final ContentProposalTemplate[] templates;
	
	private SGLRParseController parser;
	
	private RootAstNode lastParserAst;
	
	private RootAstNode lastCompletionAst;
	
	private AstNode lastCompletionNode;
	
	private String lastDocument;
	
	private int lastOffset;
	
	private AstNode currentCompletionNode;
	
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
		
		constructAst(getParser(controller), offset, document);
		
		if (currentCompletionNode == null) {
			if (lastCompletionAst == null && lastParserAst == null)
				return createErrorProposal("No proposals available - syntax errors", offset);
			else
				return createErrorProposal("No proposals available - could not identify proposal context", offset);
		}

		ICompletionProposal[] results = toProposals(invokeCompletionFunction(), document, offset);
		
		/* UNDONE: automatic proposal insertion
		if (results.length == 1 && results[0] instanceof SourceProposal) {
			results[0].apply(viewer.getDocument());
			Point selection = ((SourceProposal) results[0]).getSelection(viewer.getDocument());
			viewer.setSelectedRange(selection.x, selection.y);
			return null;
		}
		*/
		
		return results;
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

	private RootAstNode constructAst(SGLRParseController controller, int offset, String document) {
		this.parser = controller;
		RootAstNode ast = tryReusePreviousAst(offset, document);
		if (ast != null) return ast;

		String documentWithToken = document.substring(0, offset) + COMPLETION_TOKEN + document.substring(offset);
		ast = parse(controller, offset, documentWithToken);
		if (ast == null) return null;
		
		RootAstNode result = identifyCompletionNode(ast, COMPLETION_TOKEN);
		if (currentCompletionNode == null) result = insertNewCompletionNode(ast, offset, document);
		return result;
	}

	private RootAstNode parse(SGLRParseController controller, int offset, String document) {
		JSGLRI parser = controller.getParser();
		RootAstNode result;
		
		controller.scheduleParserUpdate(REINIT_PARSE_DELAY, true);
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

	private IStrategoTerm invokeCompletionFunction() {
		if (completionFunction == null) {
			return Environment.getTermFactory().makeList();
		} else {
			// TODO: is using this lock in the main thread a deadlock risk?
			class Runner implements Runnable {
				IStrategoTerm result;
				public void run() {
					synchronized (observer.getSyncRoot()) {
						IStrategoTerm input = observer.makeInputTerm(currentCompletionNode, true, false);
						result = observer.invokeSilent(completionFunction, input, currentCompletionNode.getResource());
						if (result == null) {
							observer.reportRewritingFailed();
							result = TermFactory.EMPTY_LIST;
						}
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
	
	private ICompletionProposal[] toProposals(IStrategoTerm proposals, String document, int offset) {
		Debug.startTimer();
		
		String error = "No proposals available - '" + completionFunction
				+ "' did not return a ([proposal], description) list";

		if (proposals.getTermType() != LIST)
			return createErrorProposal(error, offset);

		WrappedAstNodeFactory factory = Environment.getTermFactory();
		IStrategoString emptyString = factory.makeString("");
		Region offsetRegion = new Region(offset, 0);
		String prefix = currentCompletionPrefix;
		
		Set<ICompletionProposal> results = getKeywordAndTemplateProposals(document, prefix, offsetRegion, offset);

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
					if (head.startsWith(prefix)) confirmed = true;
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
						if (head.startsWith(prefix)) confirmed = true;
						else continue;
					}
					newText = proposalPartsToDisplayString(newTextParts);
				} else {
					newTextParts = factory.makeList(newTextTerm);
					newText = ((IStrategoString) newTextTerm).stringValue();
				}
				description = termAt(proposal, 1);
			}
			if (!confirmed && (newTextParts.isEmpty() || !newText.startsWith(prefix)))
				continue;
			results.add(new ContentProposal(this, newText, newText, prefix, offsetRegion, newTextParts, description.stringValue()));
		}
		
		return toSortedArray(results);
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
	
	private Set<ICompletionProposal> getKeywordAndTemplateProposals(String document, String prefix, Region offsetRegion, int offset) {
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
			if (!backTrackResultsOnly && proposalPrefix.regionMatches(IGNORE_TEMPLATE_PREFIX_CASE, 0, prefix, 0, prefix.length())) {
				if (!proposal.isBlankLineRequired() || isBlankBeforeOffset(document, offset - prefix.length()))
					if (prefix.length() > 0 || identifierLexical.matcher(proposal.getPrefix()).lookingAt())
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
	private RootAstNode tryReusePreviousAst(int offset, String document) {
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

	private RootAstNode reusePreviousAst(int offset, String document, String prefix) {
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
	
	private RootAstNode forceUseOldAst(SGLRParseController parser, int offset, String document) {
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
	
	private RootAstNode identifyCompletionNode(RootAstNode ast, final String completionToken) {
		class Visitor extends AbstractVisitor {	
			public boolean preVisit(AstNode node) {
				if (node instanceof StringAstNode) {
					String value = ((StringAstNode) node).getValue();
					if (value.indexOf(completionToken) > -1) {
						replaceCompletionNode(node, value.replace(completionToken, ""));
					}
				}
				return true;
			}
			
			public void postVisit(AstNode node) {
				// Unused
			}
		}
		ast.accept(new Visitor());
		return ast;
	}

	private void replaceCompletionNode(AstNode node, String value) {
		ArrayList<AstNode> siblings = node.getParent().getChildren();
		int siblingIndex = siblings.indexOf(node);
		AstNode newNode = createCompletionNode(value, node.getLeftIToken(), node.getRightIToken());
		newNode.setParent(node.getParent());
		currentCompletionNode = node.getParent(); // add a bit of context
		siblings.set(siblingIndex, newNode);
	}

	private AstNode createCompletionNode(String prefix, IToken left, IToken right) {
		ArrayList<AstNode> children = new ArrayList<AstNode>();
		children.add(new StringAstNode(prefix, null, left, right));
		currentCompletionPrefix = prefix;
		currentCompletionNode = new AstNode(null, left, right, COMPLETION_CONSTRUCTOR, children);
		return currentCompletionNode;
	}
	
	private RootAstNode insertNewCompletionNode(RootAstNode ast, final int offset, String document) {
		class Visitor extends AbstractVisitor {
			AstNode targetNode, lastNode;
			
			public boolean preVisit(AstNode node) {
				if (node.getLeftIToken().getStartOffset() <= offset
						&& offset <= node.getRightIToken().getEndOffset()) {
					targetNode = node;
				}
				lastNode = node;
				return true;
			}
			
			public void postVisit(AstNode node) {
				// Unused
			}
		}
		Visitor visitor = new Visitor();
		ast.accept(visitor);
		if (visitor.targetNode != null)
			insertNewCompletionNode(visitor.targetNode, getPrefix(offset, document));
		else
			insertNewCompletionNode(visitor.lastNode, getPrefix(offset, document));
		return ast;
	}
	
	private String getPrefix(int offset, String document) {
		int prefixStart = offset;
		while (--prefixStart >= 0) {
			String prefix = document.substring(prefixStart, offset);
			if (!identifierLexical.matcher(prefix).matches())
				return document.substring(prefixStart + 1, offset);
		}
		return document.substring(0, offset);
	}
	
	private void replacePrefix(AstNode completionNode, String prefix) {
		if (completionNode.getConstructor() != COMPLETION_CONSTRUCTOR) {
			for (Object child : completionNode.getChildren()) {
				if (((AstNode) child).getConstructor() == COMPLETION_CONSTRUCTOR)
					completionNode = (AstNode) child;
			}
		}
		StringAstNode prefixNode = (StringAstNode) completionNode.getChildren().get(0);
		prefixNode.setValue(prefix);
	}
	
	private void insertNewCompletionNode(AstNode node, String prefix) {
		// Create a new UNKNOWN(COMPLETION(prefix)) node
		AstNode newNode = createCompletionNode(prefix, node.getLeftIToken(), node.getRightIToken());
		ArrayList<AstNode> newNodeContainer = new ArrayList<AstNode>(1);
		newNodeContainer.add(newNode);
		currentCompletionNode = newNode = new AstNode(null, node.getLeftIToken(), node.getRightIToken(), COMPLETION_UNKNOWN, newNodeContainer);
		
		// Insert the node in a list near the textual input location
		if (node instanceof ListAstNode) {
			node.getChildren().add(newNode);
			newNode.setParent(node);
			return;
		}
		for (AstNode parent = node.getParent(); parent != null; node = parent, parent = parent.getParent()) {
			if (parent instanceof ListAstNode) {
				int i = parent.getChildren().indexOf(node);
				parent.getChildren().add(i + 1, newNode);
				newNode.setParent(parent);
				return;
			}
		}
		return;
	}
	
}
