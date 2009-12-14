package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.IOperatorRegistry;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.JSGLRI;
import org.strategoxt.imp.runtime.parser.ast.AsfixImploder;
import org.strategoxt.imp.runtime.parser.ast.AstNode;
import org.strategoxt.imp.runtime.parser.tokens.SGLRTokenizer;
import org.strategoxt.imp.runtime.parser.tokens.TokenKindManager;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;
import org.strategoxt.stratego_sglr.implode_asfix_1_0;

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
		AbstractPrimitive mappingPrimitive = library.get(IMPParseStringPTPrimitive.NAME);
		if (!(mappingPrimitive instanceof IMPParseStringPTPrimitive)) {
			// Spoofax/IMP parsing may not be used for this context
			return super.invoke(context, asfix, implodeConcreteSyntax);
		}

		IMPParseStringPTPrimitive mapping = (IMPParseStringPTPrimitive) mappingPrimitive;
		
		char[] inputChars = mapping.getInputChars(asfix);
		ATerm asfixATerm = mapping.getInputTerm(asfix);
		
		if (inputChars == null || asfix == null) {
			Environment.logException("Could not find origin term for asfix tree");
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
