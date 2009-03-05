package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.IOperatorRegistry;
import org.spoofax.interpreter.library.jsglr.JSGLRPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.parser.JSGLRI;
import org.strategoxt.imp.runtime.parser.ast.AsfixImploder;
import org.strategoxt.imp.runtime.parser.ast.AstNode;
import org.strategoxt.imp.runtime.parser.tokens.SGLRTokenizer;
import org.strategoxt.imp.runtime.parser.tokens.TokenKindManager;

import aterm.ATerm;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class IMPAsfixImplodePrimitive extends JSGLRPrimitive {
	
	private final AsfixImploder imploder = new AsfixImploder(new TokenKindManager());

	protected IMPAsfixImplodePrimitive() {
		super("SPI_implode_asfix", 0, 1);
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {
		
		IOperatorRegistry library = env.getOperatorRegistry(IMPJSGLRLibrary.REGISTRY_NAME);
		IMPJSGLRPrimitive jsglr = (IMPJSGLRPrimitive) library.get(IMPJSGLRPrimitive.NAME);
		
		char[] inputChars = jsglr.getInputChars(tvars[0]);
		ATerm asfix = jsglr.getInputTerm(tvars[0]);
		
		if (inputChars == null || asfix == null) {
			// TODO: Redirect to standard asfix implosion?
			throw new InterpreterException("Could not implode asfix tree; not parsed using JSGLR");
		}
		
		SGLRTokenizer tokenizer = JSGLRI.getTokenizer(asfix);
		AstNode result = imploder.implode(asfix, tokenizer);
		env.setCurrent(result.getTerm());
		
		// TODO: Make a RootAstNode object from this tree and for IMPSGLRIPrimitive
		//       which either refers to a (possibly fresh) ParseController or
		//       some other cookie that traces back the tree to the file
		
		return true;
	}
}
