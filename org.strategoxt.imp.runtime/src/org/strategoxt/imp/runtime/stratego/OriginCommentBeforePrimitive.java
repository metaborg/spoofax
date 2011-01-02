package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.stratego.DocumentStructure.TextFragment;

/**
 * Extracts all comment lines directly in front of the current node
 * @author Maartje de Jonge
 */
public class OriginCommentBeforePrimitive extends AbstractOriginPrimitive {
	
	public OriginCommentBeforePrimitive() {
		super("SSL_EXT_origin_comment_before");
	}

	@Override
	protected IStrategoTerm call(IContext env, IStrategoTerm node) {
		DocumentStructure loStructure=new DocumentStructure(node);
		TextFragment commentBlock=loStructure.getCommentsBefore();
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
