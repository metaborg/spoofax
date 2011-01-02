package org.strategoxt.imp.runtime.stratego;

import java.util.List;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.parser.ast.SubListAstNode;

/**
 * Returns the AST position of the node
 * @author Maartje de Jonge
 */
public class OriginPositionPrimitive extends AbstractOriginPrimitive {
	
	public OriginPositionPrimitive() {
		super("SSL_EXT_origin_position");
	}

	@Override
	protected IStrategoTerm call(IContext env, IStrategoTerm node) {
		IStrategoList pos;
		ISimpleTerm parent = node.getNode().getParent();
		if(parent instanceof SubListAstNode){
			List<Integer> posSublistElement = StrategoTermPath.createPathList(node.getNode());
			int posInCompleteList = ((SubListAstNode)parent).getIndexStart() + posSublistElement.get(posSublistElement.size()-1);
			pos=createPathToSublistChild((SubListAstNode)parent, posInCompleteList);
		}
		else{
			pos=StrategoTermPath.createPath(node.getNode());
		}
		return pos;
	}

		
	private IStrategoList createPathToSublistChild(SubListAstNode node, int posInCompleteList) {
		IStrategoList posStart;
		List<Integer> pathToStart = StrategoTermPath.createPathList(node);
		pathToStart.add(Integer.valueOf(posInCompleteList));
		posStart =StrategoTermPath.toStrategoPath(pathToStart);
		return posStart;
	}

	
}
