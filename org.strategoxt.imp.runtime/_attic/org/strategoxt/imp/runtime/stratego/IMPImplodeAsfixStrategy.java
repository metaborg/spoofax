package org.strategoxt.imp.runtime.stratego;

import java.io.File;
import java.io.FileNotFoundException;

import org.spoofax.interpreter.library.IOperatorRegistry;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;
import org.strategoxt.stratego_sglr.implode_asfix_1_0;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class IMPImplodeAsfixStrategy extends implode_asfix_1_0 {
	
	private static final Strategy outer = implode_asfix_1_0.instance;
	
	@Override
	public IStrategoTerm invoke(Context context, IStrategoTerm asfix, Strategy implodeConcreteSyntax) {
		IOperatorRegistry library = context.getOperatorRegistry(IMPJSGLRLibrary.REGISTRY_NAME);
		if (!(library instanceof IMPJSGLRLibrary)) {
			// Spoofax/IMP parsing may not be used for this context
			return outer.invoke(context, asfix, implodeConcreteSyntax);
		}
		
		SourceMappings mappings = ((IMPJSGLRLibrary) library).getMappings();
		File inputFile = mappings.getInputFile((IStrategoAppl) asfix);
		
		IStrategoTerm result = outer.invoke(context, asfix, implodeConcreteSyntax);
		if (result != null) {
			try {
				SourceAttachment.setSource(asfix, EditorIOAgent.getResource(inputFile), null);
			} catch (FileNotFoundException e) {
				// Ignore
			}
		}
		return result;
	}
	
	/* TODO: consider restoring originful implode-asfix functionality 
	
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
		String inputChars = mappings.getInputString(asfix);
		IStrategoTerm asfixIStrategoTerm = mappings.getInputTerm(asfix);
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
		
		IStrategoTerm result = imploder.implode(asfixIStrategoTerm, tokenizer);
		IResource resource = null;
		try {
			if (inputFile != null)
				resource = EditorIOAgent.getResource(inputFile);
		} catch (FileNotFoundException e) {
			resource = null;
		}
		result = IStrategoTerm.makeRoot(result, null, resource);
		return result;
	}
	*/
}
