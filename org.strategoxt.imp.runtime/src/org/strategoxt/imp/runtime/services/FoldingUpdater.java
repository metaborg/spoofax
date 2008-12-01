package org.strategoxt.imp.runtime.services;

import java.util.List;

import lpg.runtime.ILexStream;
import lpg.runtime.IPrsStream;
import lpg.runtime.IToken;

import org.eclipse.imp.services.base.FolderBase;
import org.strategoxt.imp.runtime.parser.ast.AstNode;
import org.strategoxt.imp.runtime.parser.ast.AbstractVisitor;
import static org.strategoxt.imp.runtime.parser.tokens.TokenKind.*;

/**
 * Folding service. Includes special logic to deal with
 * layout tokens in SGLR token streams.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class FoldingUpdater extends FolderBase {
	private IPrsStream parseStream;
	
	private final List<NodeMapping> folded;
	private final List<NodeMapping> defaultFolded;
	
	public FoldingUpdater(List<NodeMapping> folded, List<NodeMapping> defaultFolded) {
		this.folded = folded;
		this.defaultFolded = defaultFolded;
	}
	
    private class FoldingVisitor extends AbstractVisitor {
		@Override
		public boolean preVisit(AstNode node) {
          String constructor = node.getConstructor();
          String sort = node.getSort();
          
          for (NodeMapping folding : folded) {
        	  if (folding.getAttribute(constructor, sort, 0) != null) {
        		  makeCompleteAnnotation(node);
        		  break;
        	  }
          }
          
          for (NodeMapping folding : defaultFolded) {
        	  if (folding.getAttribute(constructor, sort, 0) != null) {
        		  makeCompleteAnnotation(node);
        		  // TODO: Fold node by default
        		  break;
        	  }
          }
                    
          return true;
        }

		@Override
		public void postVisit(AstNode node) {
			// Nothing to see here; move along.
		}
	}

	@Override
	public void sendVisitorToAST(java.util.HashMap newAnnotations, java.util.List annotations,
			java.lang.Object node) {
		
		AstNode astNode = (AstNode) node;
		parseStream = astNode.getLeftIToken().getPrsStream();
		
		astNode.accept(new FoldingVisitor());
	}

	public void makeCompleteAnnotation(AstNode node) {
		makeCompleteAnnotation(node.getLeftIToken(), node.getRightIToken());
	}

	public void makeCompleteAnnotation(IToken firstToken, IToken lastToken) {
		final int start = firstToken.getEndOffset();
		int end = -1;

		if (firstToken.getLine() != lastToken.getLine()) {
			// Consume any layout tokens at the end of our AST node until the
			// next EOL
			while (parseStream.getStreamLength() >= lastToken.getTokenIndex()) {
				IToken next = parseStream.getTokenAt(lastToken.getTokenIndex() + 1);

				if (next.getKind() == TK_LAYOUT.ordinal()) {
					lastToken = next;
					if ((end = getEndOfLinePosition(next)) != -1)
						break;
				} else {
					// Next AST node starts at the same line!
					break;
				}
			}

			if (end == -1)
				end = lastToken.getEndOffset();
			
			makeAnnotation(start, end - start + 1);
		}
	}

	private int getEndOfLinePosition(IToken token) {
		int end = token.getEndOffset();

		ILexStream lexStream = parseStream.getLexStream();

		for (int i = token.getStartOffset(); i <= end; i++) {
			char c = lexStream.getCharValue(i);
			if (c == '\n' || c == '\r')
				return i;
		}

		return -1;
	}
}
