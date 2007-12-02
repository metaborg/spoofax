package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

import lpg.runtime.IToken;

/**
 * Exception thrown when a parse node cannot be converted to an AST Node.  
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class UnknownAstNodeException extends RuntimeException { // unexpected; unchecked
	private static final long serialVersionUID = 1802473799644363292L;

	private final IToken leftToken, rightToken;
	
	public IToken getLeftToken() {
		return leftToken;
	}
	
	public IToken getRightToken() {
		return rightToken;
	}
	
	public UnknownAstNodeException(String sort, String constructor,
			ArrayList<? extends SGLRAstNode> children, IToken leftToken, IToken rightToken) {

		super(
			"Specified AST node with constructor "
			+ constructor + ", sort " + sort + " and children "
			+ SGLRAstNode.getSorts(children)
			+ " does not exist."
		);
		
		this.leftToken = leftToken;
		this.rightToken = rightToken;
	}
	
	public UnknownAstNodeException(String sort, IToken leftToken, IToken rightToken) {
		super("Specified AST node with sort " + sort + " does not exist.");
		
		this.leftToken = leftToken;
		this.rightToken = rightToken;
	}
}
