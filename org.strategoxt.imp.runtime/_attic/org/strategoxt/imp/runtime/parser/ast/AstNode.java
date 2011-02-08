package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lpg.runtime.ISimpleTerm;
import lpg.runtime.IAstVisitor;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.spoofax.jsglr.client.imploder.IToken;

import org.eclipse.core.resources.IResource;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermPrinter;
import org.spoofax.terms.io.InlinePrinter;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.parser.tokens.SGLRToken;
import org.strategoxt.imp.runtime.stratego.adapter.ISimpleTerm;

/**
 * A node of an SGLR abstract syntax tree.
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class IStrategoTerm implements ISimpleTerm, Iterable<IStrategoTerm>, ISimpleTerm, Cloneable {
	// Globally unique object (circumvent interning)

	/** The sort name for strings. */
	public static final String STRING_SORT = new String("<string>");

	// TODO2: Read-only array list
	static final ArrayList<IStrategoTerm> EMPTY_LIST = new ArrayList<IStrategoTerm>(0);
	
	private ArrayList<IStrategoTerm> children;
	
	private final String sort;
	
	private String constructor;
	
	private IStrategoTerm term;
	
	private IToken leftToken, rightToken;
	
	private IStrategoTerm parent;
	
	private IStrategoList annotations;
		
	// Accessors
	
	/**
	 * Returns the constructor name of this node, or null if not applicable. 
	 */
	public String getConstructor() {
		return constructor;
	}
	
	public void setConstructor(String constructor) {
		this.constructor = constructor;
	}
	
	public String getSort() {
		return sort;
	}
	
	public boolean isList() {
		return false;
	}
	
	public IResource getResource() {
		return getRoot().getResource();
	}
	
	public SGLRParseController getParseController() {
		return getRoot().getParseController();
	}
	
	// (concrete type exposed by ISimpleTerm interface)
	public final ArrayList<IStrategoTerm> getChildren() {
		assert EMPTY_LIST.size() == 0 && (children.size() == 0 || children.get(0).getParent() == this || this instanceof SubListAstNode);
		
		return children;
	}

	public int getTermType() {
		return IStrategoTerm.APPL;
	}

	/** Get the leftmost token associated with this node. */
	public IToken getLeftIToken() {
		return leftToken;
	}

	/** Get the leftmost token associated with this node. */
	public IToken getRightIToken() {
		return rightToken;
	}

	/** Set the leftmost token associated with this node. */
	protected void setLeftIToken(IToken value) {
		leftToken = value;
	}

	/** Set the leftmost token associated with this node. */
	protected void setRightIToken(IToken value) {
		rightToken = value;
	}

	public IStrategoTerm getParent() {
		return parent;
	}

	public void setParent(IStrategoTerm value) {
		parent = value;
	}
	
	public IStrategoTerm getRoot() {
		IStrategoTerm result = this;
		while (result.getParent() != null)
			result = result.getParent();
		if (!hasImploderOrigin(result))
			throw new IllegalStateException("Tree not initialized using IStrategoTerm.create()");
		else
			return (IStrategoTerm) result;
	}
	
	public IStrategoList getAnnotations() {
		return annotations;
	}
	
	protected void setAnnotations(IStrategoList annotations) {
		this.annotations = annotations;
	}
	
	public IStrategoTerm {
		if (term != null) return term;
		else return Environment.getTermFactory().wrap(this);
	}
	
	// Initialization
	
	/**
	 * Create a new AST node and set it to be the parent node of its children.
	 */
	public IStrategoTerm(String sort, IToken leftToken, IToken rightToken, String constructor,
			ArrayList<IStrategoTerm> children) {
		
		assert children != null;
		
		this.constructor = constructor;
		this.sort = sort;
		this.leftToken = leftToken;
		this.rightToken = rightToken;
		this.children = children;
		
		assert leftToken != null && rightToken != null;
		setReferences(leftToken, rightToken, children);
	}

	private void setReferences(IToken leftToken, IToken rightToken, ArrayList<IStrategoTerm> children) {
		overrideReferences(leftToken, rightToken, children, null);
	}
	
	/**
	 * Set/override references to parent nodes.
	 */
	protected void overrideReferences(IToken leftToken, IToken rightToken, ArrayList<IStrategoTerm> children, IStrategoTerm oldNode) {
		ITokenizer parseStream = leftToken.getTokenizer();
		int tokenIndex = leftToken.getIndex();
		int endTokenIndex = rightToken.getIndex();

		// Set ast node for tokens before children, and set parent references
		for (int childIndex = 0, size = children.size(); childIndex < size; childIndex++) {
			IStrategoTerm child = children.get(childIndex);
			child.parent = this;
			
			int childStart = child.getLeftToken().getIndex();
			int childEnd = child.getRightToken().getIndex();
			
			while (tokenIndex < childStart) {
				SGLRToken token = (SGLRToken) parseStream.getTokenAt(tokenIndex++);
				if (token.getAstNode() == oldNode)
					token.setAstNode(this);
			}
			
			tokenIndex = childEnd + 1; 
		}
		
		// Set ast node for tokens after children
		while (tokenIndex <= endTokenIndex) {
			SGLRToken token = (SGLRToken) parseStream.getTokenAt(tokenIndex++);
			if (token.getAstNode() == oldNode)
				token.setAstNode(this);
		}
	}
	
	// General access
	
	public Iterator<IStrategoTerm> iterator() {
		return children.iterator();
	}
	
	/**
	 * Creates a "deep" clone of this IStrategoTerm,
	 * but maintains a shallow clone of all tokens,
	 * which still point back to the original AST.
	 */
	public IStrategoTerm cloneIgnoreTokens() {
		// TODO: create a better IStrategoTerm.clone() method? this is a bit of a cop-out...
		try {
			IStrategoTerm result = (IStrategoTerm) super.clone();
			ArrayList<IStrategoTerm> children = result.children;
			ArrayList<IStrategoTerm> newChildren = new ArrayList<IStrategoTerm>(children.size());
			for (int i = 0, size = children.size(); i < size; i++) {
				IStrategoTerm newChild = children.get(i).cloneIgnoreTokens();
				newChild.parent = result;
				newChildren.add(newChild);
			}
			result.children = newChildren;
			return result;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}
	}
	
	@Deprecated
	public static List<String> getSorts(List<? extends IStrategoTerm> children) {
  	  List<String> result = new ArrayList<String>(children.size());
  	  
  	  for (IStrategoTerm node : children) {
  		  result.add(getSort(node));
  	  }
  	  
  	  return result;
	}
	
	@Override
	public int hashCode() {
		return.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ISimpleTerm) {
			return this == obj || ((ISimpleTerm) obj).equals);
		} else {
			return false;
		}
	}
	
	// Visitor support
	
	public void accept(IAstVisitor visitor) {
		if (visitor.preVisit(this)) {
			for (int i = 0, size = children.size(); i < size; i++) {
				children.get(i).accept(visitor);
			}
		}
		
		visitor.postVisit(this);
	}
	
	// LPG legacy/compatibility
	
	/**
	 * Get all children (including the null ones).
	 * 
	 * @deprecated  Unused; ATermAstNode does not include null children.
	 */
	@Deprecated
	public ArrayList<IStrategoTerm> getAllChildren() {
		return getChildren();
	}

	@Deprecated
	public IToken[] getPrecedingAdjuncts() {
		return getLeftIToken().getPrecedingAdjuncts();
	}
	
	@Deprecated
	public IToken[] getFollowingAdjuncts() {
		return getRightIToken().getFollowingAdjuncts();
	}

	@Deprecated
	public IStrategoTerm getNextAst() {
		return null;
	}

	/**
	 * Pretty prints the AST formed by this node.
	 * 
	 * @see #prettyPrint(ITermPrinter)
	 * @see #yield()
	 */
	@Override
	public final String toString() {
		ITermPrinter result = new InlinePrinter();
		prettyPrint(result);
		return result.getString();
	}
	
	public void prettyPrint(ITermPrinter printer) {
		printer.print(constructor == null ? "<null>" : constructor);
		//sb.append(':');
		//sb.append(sort);
		printer.print("(");
		if (getSubtermCount() > 0) {
			getSubterm(0).prettyPrint(printer);
			for (int i = 1; i < getSubtermCount(); i++) {
				printer.print(",");
				getSubterm(i).prettyPrint(printer);
			}
		}
		printer.print(")");
	}

	/**
	 * Return the input string that formed this AST.
	 */
	public String yield() {
		return getLeftIToken().getTokenizer().toString(getLeftIToken(), getRightIToken());
	}
}
