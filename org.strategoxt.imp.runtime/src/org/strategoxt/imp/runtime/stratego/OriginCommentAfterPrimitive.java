package org.strategoxt.imp.runtime.stratego;

import lpg.runtime.ILexStream;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.stratego.DocumentaryStructure.TextFragment;
import org.strategoxt.imp.runtime.stratego.adapter.IWrappedAstNode;

/**
 * Extracts all comment lines directly after the current node on the same line
 * @author Maartje de Jonge
 */
public class OriginCommentAfterPrimitive extends AbstractOriginPrimitive {
	
	public OriginCommentAfterPrimitive() {
		super("SSL_EXT_origin_comment_after");
	}

	@Override
	protected IStrategoTerm call(IContext env, IWrappedAstNode node) {
		DocumentaryStructure loStructure=new DocumentaryStructure(node);
		TextFragment commentBlock=loStructure.getCommentsAfter();
		if(commentBlock==null)
			return null;
		ILexStream lexStream=node.getNode().getLeftIToken().getILexStream();
		return env.getFactory().makeTuple(
				env.getFactory().makeInt(commentBlock.getStart()),
				env.getFactory().makeInt(commentBlock.getEnd()),
				env.getFactory().makeString(commentBlock.getText(lexStream))
		);
	}
}
