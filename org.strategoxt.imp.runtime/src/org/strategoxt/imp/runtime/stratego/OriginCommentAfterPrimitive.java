package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.stratego.DocumentStructure.TextFragment;

/**
 * Extracts all comment lines directly after the current node on the same line
 * @author Maartje de Jonge
 */
public class OriginCommentAfterPrimitive extends AbstractOriginPrimitive {
	
	public OriginCommentAfterPrimitive() {
		super("SSL_EXT_origin_comment_after");
	}

	@Override
	protected IStrategoTerm call(IContext env, IStrategoTerm node) {
		DocumentStructure loStructure=new DocumentStructure(node);
		TextFragment commentBlock=loStructure.getCommentsAfter();
		if(commentBlock==null)
			return null;
		ILexStream lexStream=node.getNode().getLeftToken().getILexStream();
		return env.getFactory().makeTuple(
				env.getFactory().makeInt(commentBlock.getStart()),
				env.getFactory().makeInt(commentBlock.getEnd()),
				env.getFactory().makeString(commentBlock.getText(lexStream))
		);
	}
}
