package org.strategoxt.imp.runtime.services;

import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;
import static org.spoofax.terms.Term.isTermString;
import static org.spoofax.terms.attachments.ParentAttachment.getRoot;
import static org.strategoxt.imp.runtime.Environment.getTermFactory;
import static org.strategoxt.imp.runtime.services.ContentProposer.COMPLETION_TOKEN;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.imp.parser.IParseController;
import org.spoofax.NotImplementedException;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.shared.SGLRException;
import org.spoofax.terms.TermVisitor;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.DynamicParseController;
import org.strategoxt.imp.runtime.parser.JSGLRI;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

/**
 * Content completion parsing and tree construction
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class ContentProposerParser {
	
	protected static final IStrategoConstructor COMPLETION_CONSTRUCTOR =
		getTermFactory().makeConstructor("COMPLETION", 0);
	
	protected static final IStrategoConstructor COMPLETION_UNKNOWN =
		getTermFactory().makeConstructor("NOCONTEXT", 0);

	private static final long REINIT_PARSE_DELAY = 4000;
	
	private final Pattern identifierLexical;
	
	private SGLRParseController parser;
	
	private IStrategoTerm lastParserAst;
	
	private IStrategoTerm lastCompletionAst;
	
	private IStrategoTerm lastCompletionNode;
	
	private String lastCompletionPrefix;
	
	private String lastDocument;
	
	private int lastOffset;
	
	private IStrategoTerm completionNode;
	
	private String completionPrefix;
	
	public ContentProposerParser(Pattern identifierLexical) {
		this.identifierLexical = identifierLexical;
	}
	
	protected SGLRParseController getParser() {
		return parser;
	}
	
	public IStrategoTerm getCompletionNode() {
		return completionNode;
	}
	
	public String getCompletionPrefix() {
		return completionPrefix;
	}
	
	public boolean isFatalSyntaxError() {
		return lastCompletionAst == null && lastParserAst == null && completionNode == null;
	}

	public IStrategoTerm parse(IParseController icontroller, int offset, String document, boolean avoidReparse) {
		lastCompletionNode = completionNode;
		lastCompletionPrefix = completionPrefix;
		completionNode = null;
		
		SGLRParseController controller = this.parser = getParser(icontroller);
		IStrategoTerm ast = tryReusePreviousAst(offset, document);
		if (ast != null) return ast;

		String documentWithToken = document.substring(0, offset) + COMPLETION_TOKEN + document.substring(offset);
		ast = parse(controller, offset, documentWithToken, avoidReparse);
		if (ast == null) return null;
		
		IStrategoTerm result = identifyCompletionNode(ast, COMPLETION_TOKEN);
		if (completionNode == null) result = insertNewCompletionNode(ast, offset, document);
		return result;
	}

	private IStrategoTerm parse(SGLRParseController controller, int offset, String document, boolean avoidReparse) {
		JSGLRI parser = controller.getParser();
		IStrategoTerm result;
		
		controller.scheduleParserUpdate(REINIT_PARSE_DELAY, true); // cancel current parse
		Debug.startTimer();
		controller.getParseLock().lock();
		Debug.stopTimer("Completion acquired parse lock");
		try {
			if (avoidReparse && controller.getCurrentAst() != null) {
				// Don't reparse for keyword completion
				result = controller.getCurrentAst();
			} else {
				Debug.startTimer();
				try {
					result = parser.parse(document, controller.getPath().toPortableString());
				} finally {
					Debug.stopTimer("Completion parsed");
				}
				lastParserAst = controller.getCurrentAst();
				lastCompletionAst = result;
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
		completionPrefix = prefix;
		lastDocument = document;
		lastOffset = offset;
		String prefixInAst = sanitizePrefix(completionPrefix);
		if (prefixInAst == null)
			return null;
		completionNode = lastCompletionNode;
		replacePrefix(completionNode, prefixInAst);
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
			completionNode = null;
		} else {
			if (completionNode == null)
				return null;
			replacePrefix(completionNode, readPrefix(offset, document));
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
						replaceCompletionNode(getRoot(node), node, value.replace(completionToken, ""));
					}
				}
			}
		}
		new Visitor().visit(ast);
		return ast;
	}
	
	private IStrategoTerm replaceCompletionNode(IStrategoTerm tree, final IStrategoTerm node, final String value) {
		throw new NotImplementedException();
		/* TODO: implement missing ContentProposer bits
		return topdown_1_0.instance.invoke(new Context(Environment.getTermFactory()), node,
			new Strategy() {
				@Override
				public IStrategoTerm invoke(Context context, IStrategoTerm current) {
					if (current == node) {
						current = createCompletionNode(value, getLeftToken(node), getRightToken(node));
						currentCompletionNode = getParent(current); // add a bit of context
						if (currentCompletionNode == null) currentCompletionNode = current;
					}
					ParentAttachment parent = new ParentAttachment(parent, elementParent)
					Iterator<IStrategoTerm> iterator = TermVisitor.tryGetListIterator(current); 
					for (int i = 0, max = current.getSubtermCount(); i < max; i++) {
						IStrategoTerm child = iterator == null ? current.getSubterm(i) : iterator.next();
						
					}
				}
			});
			*/
	}

	private IStrategoTerm createCompletionNode(String prefix, IToken left, IToken right) {
		throw new NotImplementedException();
		/* TODO: implement missing ContentProposer bits
		ArrayList<IStrategoTerm> children = new ArrayList<IStrategoTerm>();
		children.add(new StringAstNode(prefix, null, left, right));
		currentCompletionPrefix = prefix;
		currentCompletionNode = new IStrategoTerm(null, left, right, COMPLETION_CONSTRUCTOR, children);
		return currentCompletionNode;
		*/
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
			insertNewCompletionNode(visitor.targetNode, readPrefix(offset, document));
		else
			insertNewCompletionNode(visitor.lastNode, readPrefix(offset, document));
		return ast;
	}
	
	public String readPrefix(int offset, String document) {
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
		throw new NotImplementedException();
		/* TODO: implement missing ContentProposer bits
		if (completionNode.getConstructor() != COMPLETION_CONSTRUCTOR) {
			for (Object child : completionNode.getChildren()) {
				if (((IStrategoTerm) child).getConstructor() == COMPLETION_CONSTRUCTOR)
					completionNode = (IStrategoTerm) child;
			}
		}
		StringAstNode prefixNode = (StringAstNode) completionNode.getSubterm(0);
		prefixNode.setValue(prefix);
		*/
	}
	
	private void insertNewCompletionNode(IStrategoTerm node, String prefix) {
		throw new NotImplementedException();
		/* TODO: implement missing ContentProposer bits
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
		*/
	}
	
}
