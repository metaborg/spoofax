package org.strategoxt.imp.runtime.stratego;

import java.io.File;
import java.io.FileNotFoundException;

import org.eclipse.core.resources.IResource;
import org.spoofax.interpreter.library.IOperatorRegistry;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.parser.ast.AsfixImploder;
import org.strategoxt.imp.runtime.parser.ast.AstNode;
import org.strategoxt.imp.runtime.parser.ast.RootAstNode;
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
		if (!(library instanceof IMPJSGLRLibrary)) {
			// Spoofax/IMP parsing may not be used for this context
			return super.invoke(context, asfix, implodeConcreteSyntax);
		}
		
		SourceMappings mappings = ((IMPJSGLRLibrary) library).getMappings();
		char[] inputChars = mappings.getInputChars(asfix);
		ATerm asfixATerm = mappings.getInputTerm(asfix);
		File inputFile = mappings.getInputFile((IStrategoAppl) asfix);
		SGLRTokenizer tokenizer = mappings.getTokenizer(asfix);
		
		if (inputChars == null || asfix == null) {
			// HACK: stfu
			boolean silent = false;
			try {
				silent = "Spoofax-Testing".equals(((EditorIOAgent) context.getIOAgent()).getDescriptor().getLanguage().getName());
			} catch (BadDescriptorException e) {
				// Ignore
			}
			if (!silent)
				Environment.logWarning("Could not find origin term for asfix tree (did it change after parsing?)");
			return outer.invoke(context, asfix, implodeConcreteSyntax);
		}
		
		AstNode result = imploder.implode(asfixATerm, tokenizer);
		IResource resource = null;
		try {
			if (inputFile != null)
				resource = EditorIOAgent.getResource(inputFile);
		} catch (FileNotFoundException e) {
			resource = null;
		}
		result = RootAstNode.makeRoot(result, null, resource);
		return result.getTerm();
	}
}
