package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.library.IOperatorRegistry;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.parser.JSGLRI;
import org.strategoxt.imp.runtime.parser.ast.AsfixImploder;
import org.strategoxt.imp.runtime.parser.ast.AstNode;
import org.strategoxt.imp.runtime.parser.tokens.SGLRTokenizer;
import org.strategoxt.imp.runtime.parser.tokens.TokenKindManager;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;
import org.strategoxt.libstratego_sglr.implode_asfix_1_0;

import aterm.ATerm;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class IMPImplodeAsfixStrategy extends implode_asfix_1_0 {
	
	private static final Strategy outer = implode_asfix_1_0.instance;
	
	private final AsfixImploder imploder = new AsfixImploder(new TokenKindManager());

	@Override
	public IStrategoTerm invoke(Context context, IStrategoTerm asfix, Strategy implodeConcreteSyntax) {
		if (implodeConcreteSyntax.invoke(context, asfix) == null)
			return super.invoke(context, asfix, implodeConcreteSyntax);
		
		IOperatorRegistry library = context.getOperatorRegistry(IMPJSGLRLibrary.REGISTRY_NAME);
		IMPParseStringPTPrimitive jsglr = (IMPParseStringPTPrimitive) library.get(IMPParseStringPTPrimitive.NAME);
		
		char[] inputChars = jsglr.getInputChars(asfix);
		ATerm asfixATerm = jsglr.getInputTerm(asfix);
		
		if (inputChars == null || asfix == null) {
			return outer.invoke(context, asfix, implodeConcreteSyntax);
		}
		
		SGLRTokenizer tokenizer = JSGLRI.getTokenizer(asfixATerm);
		AstNode result = imploder.implode(asfixATerm, tokenizer);
		return result.getTerm();
		
		// TODO: Make a RootAstNode object from this tree and for IMPSGLRIPrimitive
		//       which either refers to a (possibly fresh) ParseController or
		//       some other cookie that traces back the tree to the file
	}
}
