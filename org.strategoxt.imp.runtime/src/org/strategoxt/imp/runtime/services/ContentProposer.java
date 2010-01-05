package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.*;
import static org.spoofax.interpreter.terms.IStrategoTerm.*;

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
import org.eclipse.imp.editor.SourceProposal;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IContentProposer;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Point;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.SGLRException;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.DynamicParseController;
import org.strategoxt.imp.runtime.parser.JSGLRI;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.parser.ast.AbstractVisitor;
import org.strategoxt.imp.runtime.parser.ast.AstNode;
import org.strategoxt.imp.runtime.parser.ast.ListAstNode;
import org.strategoxt.imp.runtime.parser.ast.RootAstNode;
import org.strategoxt.imp.runtime.parser.ast.StringAstNode;

/**
 * Content completion.
 * 
 * Works in 6 steps:
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
	
	private static final String COMPLETION_CONSTRUCTOR = "COMPLETION";
	
	private static final String COMPLETION_UNKNOWN = "NOCONTEXT";
	
	private final StringBuilder proposalBuilder = new StringBuilder();
	
	private final StrategoObserver observer;
	
	private final String completionFunction;
	
	private final Pattern completionLexical;
	
	private final String[] keywords;
	
	private SGLRParseController parser;
	
	private RootAstNode lastParserAst;
	
	private RootAstNode lastCompletionAst;
	
	private AstNode currentCompletionNode;
	
	private String currentCompletionPrefix;
	
	public ContentProposer(StrategoObserver observer, String completionFunction, Pattern completionLexical, String[] keywords) {
		this.observer = observer;
		this.completionFunction = completionFunction;
		this.completionLexical = completionLexical;
		this.keywords = keywords;
	}
	
	public ICompletionProposal[] getContentProposals(IParseController controller, int offset, ITextViewer viewer) {
		currentCompletionNode = null;
		String document = viewer.getDocument().get();
		String completionToken = createCompletionToken();
		
		if (!completionLexical.matcher(completionToken).matches())
			return createErrorProposal("No proposals available - completion lexical must allow letters and numbers", offset);
		
		constructAst(getParser(controller), offset, document, completionToken);
		
		if (currentCompletionNode == null) {
			if (lastCompletionAst == null && lastParserAst == null)
				return createErrorProposal("No proposals available - syntax errors", offset);
			else
				return createErrorProposal("No proposals available - could not identify proposal context", offset);
		}

		ICompletionProposal[] results = toProposals(invokeCompletionFunction(), document, offset);
		
		if (results.length == 1 && results[0] instanceof SourceProposal) {
			results[0].apply(viewer.getDocument());
			Point selection = ((SourceProposal) results[0]).getSelection(viewer.getDocument());
			viewer.setSelectedRange(selection.x, selection.y);
			return null;
		}
		return results;
    }

	public static String createCompletionToken() {
		return "CONTENTCOMPLETE" + Math.abs(new Random().nextInt());
	}

	private RootAstNode constructAst(SGLRParseController controller, int offset, String document, String completionToken) {
		this.parser = controller;
		document = document.substring(0, offset) + completionToken + document.substring(offset);
		JSGLRI parser = controller.getParser();
		RootAstNode ast;
		
		controller.scheduleParserUpdate(DynamicParseController.REINIT_PARSE_DELAY, true);
		controller.getParseLock().lock();
		try {
			if (completionFunction == null && controller.getCurrentAst() != null) {
				ast = controller.getCurrentAst().cloneIgnoreTokens();
			} else {
				ast = parser.parse(document.toCharArray(), controller.getPath().toPortableString());
				lastParserAst = controller.getCurrentAst();
				lastCompletionAst = ast.cloneIgnoreTokens();
			}
		} catch (SGLRException e) {
			Environment.logException("Could not reparse input for content completion", e);
			ast = getPreviousAst(controller);
		} catch (IOException e) {
			Environment.logException("Could not reparse input for content completion", e);
			ast = getPreviousAst(controller);
		} finally {
			controller.getParseLock().unlock();
		}

		if (ast == null) return null;
		
		RootAstNode result = identifyCompletionNode(ast, completionToken);
		if (currentCompletionNode == null) result = insertNewCompletionNode(ast, offset, document);
		return result;
	}

	private IStrategoTerm invokeCompletionFunction() {
		if (completionFunction == null) {
			return Environment.getTermFactory().makeList();
		} else {
			synchronized (observer.getSyncRoot()) {
				return observer.invokeSilent(completionFunction, currentCompletionNode);
			}
		}
	}
	
	private ICompletionProposal[] toProposals(IStrategoTerm term, String document, int offset) {
		if (term == null)
			return createErrorProposal("No proposals available - completion strategy failed", offset);
		
		String error = "No proposals available - '" + completionFunction
				+ "' did not return a ([proposal], description) list";
		Set<IStrategoTerm> resultTerms = new HashSet<IStrategoTerm>(term.getSubtermCount());

		if (term.getTermType() != LIST)
			return createErrorProposal(error, offset);
		
		// Create a set first, removing duplicates
		for (IStrategoList cons = (IStrategoList) term; !cons.isEmpty(); cons = cons.tail()) {
			IStrategoTerm proposal = cons.head();
			resultTerms.add(proposal);
		}
		
		Region offsetRegion = new Region(offset, 0);
		Set<ICompletionProposal> results = getKeywordProposals(document, currentCompletionPrefix, offsetRegion, offset);

		for (IStrategoTerm proposal : resultTerms) {
			if (proposal.getTermType() != TUPLE || proposal.getSubtermCount() != 2
					|| termAt(proposal, 0).getTermType() != LIST
					|| termAt(proposal, 1).getTermType() != STRING)
				return createErrorProposal(error, offset);
			IStrategoList newTextParts = termAt(proposal, 0);
			String newText = proposalPartsToString(newTextParts);
			if (newTextParts.isEmpty() || !newText.startsWith(currentCompletionPrefix))
				continue;
			IStrategoString description = termAt(proposal, 1);
			results.add(new SPISourceProposal(newText, newText, currentCompletionPrefix, offsetRegion, newTextParts, description.stringValue())); 
		}
		
		return toSortedArray(results);
	}

	private static ICompletionProposal[] toSortedArray(Set<ICompletionProposal> results) {
		ICompletionProposal[] resultArray = results.toArray(new ICompletionProposal[results.size()]);
		
		Arrays.sort(resultArray, new Comparator<ICompletionProposal>() {
			public int compare(ICompletionProposal o1, ICompletionProposal o2) {
				return o1.getDisplayString().compareTo(o2.getDisplayString());
			}
		});
		return resultArray;
	}
	
	private String proposalPartsToString(IStrategoList proposalParts) {
		proposalBuilder.setLength(0);
		for (IStrategoList cons = proposalParts; !cons.isEmpty(); cons = cons.tail()) {
			IStrategoTerm part = cons.head();
			if (part.getTermType() != STRING) return null;
			proposalBuilder.append(asJavaString(part));
		}
		return proposalBuilder.toString();
	}

	private Point proposalPartsToSelection(IStrategoList proposalParts, int offset) {
		int i = asJavaString(proposalParts.head()).length();
		for (IStrategoList cons = proposalParts.tail(); !cons.isEmpty(); cons = cons.tail()) {
			String part = asJavaString(cons.head());
			if (completionLexical.matcher(part).matches())
				return new Point(offset + i, part.length());
			i += part.length();
		}
		return new Point(offset + i, 0);
	}
	
	private Set<ICompletionProposal> getKeywordProposals(String document, String prefix, Region offsetRegion, int offset) {
		Set<ICompletionProposal> results = new HashSet<ICompletionProposal>();
		boolean specialCharsOnly = false;
		
	proposals:
		for (String proposal : keywords) {
			Matcher matcher = completionLexical.matcher(proposal);
			if (!specialCharsOnly && proposal.regionMatches(true, 0, prefix, 0, prefix.length())) {
				results.add(new SPISourceProposal(proposal, proposal, prefix, offsetRegion,
						offset + proposal.length() - prefix.length(), ""));
			} else if (matcher.find() && (matcher.start() > 0 || matcher.end() < proposal.length())) {
				// Handle completion literals with special characters, like "(disabled)"
				if (matcher.start() == 0 && !matcher.find(matcher.end()))
					continue;
				do {
					if (document.regionMatches(offset - matcher.start() - prefix.length(), proposal, 0, matcher.start()) 
							&& proposal.regionMatches(matcher.start(), prefix, 0, prefix.length())) {
						String subProposal = proposal.substring(matcher.start());
						if (!specialCharsOnly) results.clear();
						specialCharsOnly = true;
						results.add(new SPISourceProposal(proposal, subProposal, prefix,
								offsetRegion, offset + matcher.end() - matcher.start() - prefix.length(), ""));
						continue proposals;
					}
				} while (matcher.find(matcher.end()));
			}
		}
		return results;
	}

	private ICompletionProposal[] createErrorProposal(String error, int offset) {
		return new ICompletionProposal[] { new ErrorProposal(error, offset) };
	}
	
	private RootAstNode getPreviousAst(SGLRParseController parser) {
		return parser.getCurrentAst() == lastParserAst
				? lastCompletionAst
				: lastParserAst.cloneIgnoreTokens();
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
		if (prefix.length() > 0 && !completionLexical.matcher(prefix).matches())
			Environment.logWarning("Identifier does not match completion lexical pattern: '" + prefix + "'");
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
			if (!completionLexical.matcher(prefix).matches())
				return document.substring(prefixStart + 1, offset);
		}
		return document.substring(0, offset);
	}
	
	private void insertNewCompletionNode(AstNode node, String prefix) {
		// Create a new UNKNOWN(COMPLETION(prefix)) node
		AstNode newNode = createCompletionNode(prefix, node.getLeftIToken(), node.getRightIToken());
		ArrayList<AstNode> newNodeContainer = new ArrayList<AstNode>(1);
		newNodeContainer.add(newNode);
		newNode = new AstNode(null, node.getLeftIToken(), node.getRightIToken(), COMPLETION_UNKNOWN, newNodeContainer);
		
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
	
	/**
	 * A content proposal that selects the lexical at the cursor location.
	 * 
	 * @author Lennart Kats <lennart add lclnet.nl>
	 */
	private class SPISourceProposal extends SourceProposal {
		
		private final IStrategoList newTextParts;
		
		public SPISourceProposal(String proposal, String newText, String prefix, Region region,
				int cursorLoc, String addlInfo) {
			super(proposal, newText, prefix, region, cursorLoc, addlInfo);
			this.newTextParts = null;
		}

		public SPISourceProposal(String proposal, String newText, String prefix, Region region, 
				IStrategoList newTextParts, String addlInfo) {
			super(proposal, newText, prefix, region, addlInfo);
			this.newTextParts = newTextParts;
		}

		@Override
		public Point getSelection(IDocument document) {
			if (newTextParts == null)
				return super.getSelection(document);
			else
				return proposalPartsToSelection(newTextParts, getRange().getOffset() - getPrefix().length());
		}
		
		@Override
		public void apply(IDocument document) {
			super.apply(document);
			observer.setRushNextUpdate(true);
			parser.getErrorHandler().setRushNextUpdate(true);
			parser.scheduleParserUpdate(0, false);
		}
	}
}
