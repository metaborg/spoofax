package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import org.eclipse.core.runtime.IPath;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermPrinter;
import org.spoofax.interpreter.terms.InlinePrinter;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.tokens.SGLRToken;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;

import lpg.runtime.IAst;
import lpg.runtime.IAstVisitor;
import lpg.runtime.IPrsStream;
import lpg.runtime.IToken;

/**
 * A node of an SGLR abstract syntax tree.
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class AstNode implements IAst, Iterable<AstNode>, IStrategoAstNode {
	// Globally unique object (circumvent interning)

	/** The sort name for strings. */
	public static final String STRING_SORT = new String("<string>");

	// TODO2: Read-only array list
	static final ArrayList<AstNode> EMPTY_LIST = new ArrayList<AstNode>(0);
	
	protected final ArrayList<AstNode> children;
	
	private final String constructor, sort;
	
	private IStrategoTerm term;
	
	private IToken leftToken, rightToken;
	
	private AstNode parent;
		
	// Accessors
	
	/**
	 * Returns the constructor name of this node, or null if not applicable. 
	 */
	public String getConstructor() {
		return constructor;
	}
	
	public String getSort() {
		return sort;
	}
	
	public boolean isList() {
		return false;
	}
	
	// must expose impl. type for interface
	public final ArrayList<AstNode> getChildren() {
		assert EMPTY_LIST.size() == 0;
		
		return children;
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

	public AstNode getParent() {
		return parent;
	}

	private void setParent(AstNode value) {
		parent = value;
	}
	
	public RootAstNode getRoot() {
		AstNode result = this;
		while (result.getParent() != null)
			result = result.getParent();
		return (RootAstNode) result;
	}
	
	public IPath getResourcePath() {
		return getRoot().getParseController().getPath();
	}
	
	public IPath getRootPath() {
		return getRoot().getParseController().getProject().getRawProject().getLocation();
	}
	
	public IStrategoTerm getTerm() {
		if (term != null) return term;
		else return Environment.getWrappedAstNodeFactory().wrapNew(this);
	}
	
	// Initialization
	
	/**
	 * Create a new AST node and set it to be the parent node of its children.
	 */
	public AstNode(String sort, String constructor, IToken leftToken, IToken rightToken,
			ArrayList<AstNode> children) {
		
		assert rightToken != null;
		assert children != null;
		
		this.constructor = constructor;
		this.sort = sort;
		this.leftToken = leftToken;
		this.rightToken = rightToken;
		this.children = children;
		
		if (leftToken != null)
			setReferences(leftToken, rightToken, children);
	}

	private void setReferences(IToken leftToken, IToken rightToken, ArrayList<AstNode> children) {
		IPrsStream parseStream = leftToken.getPrsStream();
		int end = rightToken.getTokenIndex();
		
		for (int i = leftToken.getTokenIndex(); i <= end; i++) {
			SGLRToken token = (SGLRToken) parseStream.getTokenAt(i);
			if (token.getAstNode() == null) token.setAstNode(this);
		}
		
		for (AstNode node : children) {
			node.setParent(this);
		}
	}
	
	// General access
	
	public Iterator<AstNode> iterator() {
		return children.iterator();
	}

    public String yield() {
        return getLeftIToken().getPrsStream().toString(getLeftIToken(), getRightIToken());
    }
    
    // map(getSort)
    public static List<String> getSorts(List<? extends AstNode> children) {
  	  List<String> result = new ArrayList<String>(children.size());
  	  
  	  for (AstNode node : children) {
  		  result.add(node.getSort());
  	  }
  	  
  	  return result;
    }
    
    @Override
    public int hashCode() {
    	int result =  235235 ^ getChildren().size();
    	String constructor = getConstructor();
    	String sort = getSort();
    	
    	if (constructor != null) result ^= constructor.hashCode();  
    	if (sort != null) result ^= sort.hashCode();
    	
    	return result;
    }

    /* UNDONE: Removed aterm<->ast node coupling
    @Override
	public boolean equals(Object o) {
    	if (o instanceof SGLRAstNode)
    		return getATerm().equals(((SGLRAstNode) o).getATerm());
    	else
    		return false;
    }
    
    @Override
    public int hashCode() {
    	return getATerm().hashCode();
    }
    */
    
    // Visitor support
    
    @Override
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
	public ArrayList<? extends AstNode> getAllChildren() {
		return getChildren();
	}

    public IToken[] getPrecedingAdjuncts() {
    	return getLeftIToken().getPrecedingAdjuncts();
    }
    
    public IToken[] getFollowingAdjuncts() {
    	return getRightIToken().getFollowingAdjuncts();
    }

	public AstNode getNextAst() {
		return null;
	}

	@Override
	public final String toString() {
		ITermPrinter result = new InlinePrinter();
		prettyPrint(result);
		return result.getString();
	}
	
	public void prettyPrint(ITermPrinter printer) {
		printer.print(constructor);
		//sb.append(':');
		//sb.append(sort);
		printer.print("(");
		if (getChildren().size() > 0) {
			getChildren().get(0).prettyPrint(printer);
			for (int i = 1; i < getChildren().size(); i++) {
				printer.print(",");
				getChildren().get(i).prettyPrint(printer);
			}
		}
		printer.print(")");
	}
}
