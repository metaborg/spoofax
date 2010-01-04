package org.strategoxt.imp.runtime.stratego;

import java.io.File;
import java.net.URI;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.spoofax.interpreter.library.IOperatorRegistry;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;
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
		File inputFile = mappings.getInputFile(asfix);
		SGLRTokenizer tokenizer = mappings.getTokenizer(asfix);
		
		if (inputChars == null || asfix == null) {
			Environment.logException("Could not find origin term for asfix tree (did it change after parsing?)");
			return outer.invoke(context, asfix, implodeConcreteSyntax);
		}
		
		AstNode result = imploder.implode(asfixATerm, tokenizer);
		result = RootAstNode.makeRoot(result, getResource(inputFile));
		return result.getTerm();
		
		// TODO: Make a RootAstNode object from this tree and for IMPSGLRIPrimitive
		//       which either refers to a (possibly fresh) ParseController or
		//       some other cookie that traces back the tree to the file
	}

	private static IResource getResource(File file) {
		URI uri = file.toURI();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IResource[] resources = workspace.getRoot().findFilesForLocationURI(uri);
		if (resources.length == 0) {
			Environment.logWarning("Parsed file not in workspace: " + file);
			return null;
		}

		IResource resource = resources[0];
		return resource;
	}
}
