package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.terms.attachments.ParentAttachment.getParent;

import java.util.List;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.parser.ast.StrategoSubList;

/**
 * Returns the AST position of the node
 * @author Maartje de Jonge
 */
public class OriginPositionPrimitive extends AbstractOriginPrimitive {
	
	public OriginPositionPrimitive() {
		super("SSL_EXT_origin_position");
	}

	@Override
	protected IStrategoTerm call(IContext env, IStrategoTerm origin) {
		IStrategoList pos;
		ISimpleTerm parent = getParent(origin);
		if (parent instanceof StrategoSubList) {
			List<Integer> posSublistElement = StrategoTermPath.createPathList(origin);
			int posInCompleteList = ((StrategoSubList)parent).getIndexStart() + posSublistElement.get(posSublistElement.size()-1);
			pos=createPathToSublistChild((StrategoSubList)parent, posInCompleteList);
		}
		else{
			pos=StrategoTermPath.createPath(origin);
		}
		return pos;
	}

		
	private IStrategoList createPathToSublistChild(StrategoSubList node, int posInCompleteList) {
		IStrategoList posStart;
		List<Integer> pathToStart = StrategoTermPath.createPathList(node);
		pathToStart.add(Integer.valueOf(posInCompleteList));
		posStart =StrategoTermPath.toStrategoPath(pathToStart);
		return posStart;
	}

	
}
