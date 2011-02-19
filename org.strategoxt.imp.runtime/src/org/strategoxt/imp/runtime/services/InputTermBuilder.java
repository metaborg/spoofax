package org.strategoxt.imp.runtime.services;

import static org.spoofax.terms.attachments.ParentAttachment.getParent;
import static org.spoofax.terms.attachments.ParentAttachment.getRoot;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.stratego.SourceAttachment;
import org.strategoxt.imp.runtime.stratego.StrategoTermPath;
import org.strategoxt.lang.Context;
import org.strategoxt.stratego_aterm.implode_aterm_0_0;
import org.strategoxt.stratego_aterm.stratego_aterm;

/**
 * Builder of Stratego editor service input tuples.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class InputTermBuilder {
	
	private static final Map<IResource, IStrategoTerm> EMPTY_MAP =
		Collections.emptyMap();
	
	private HybridInterpreter runtime;
	
	private final Map<IResource, IStrategoTerm> resultingAsts;
	
	public InputTermBuilder(HybridInterpreter runtime, Map<IResource, IStrategoTerm> resultingAsts) {
		this.runtime = runtime;
		this.resultingAsts = resultingAsts;
	}
	
	public InputTermBuilder(HybridInterpreter runtime) {
		this(runtime, EMPTY_MAP);
	}
	
	public HybridInterpreter getRuntime() {
		return runtime;
	}

	/**
	 * Create an input term for a control rule.
	 */
	public IStrategoTuple makeInputTerm(IStrategoTerm node, boolean includeSubNode) {
		return makeInputTerm(node, includeSubNode, false);
	}
	
	/**
	 * Create an input term for a control rule.
	 */
	public IStrategoTuple makeInputTerm(IStrategoTerm node, boolean includeSubNode, boolean useSourceAst) {
		Context context = runtime.getCompiledContext();
		IResource resource = SourceAttachment.getResource(node);
		IStrategoTerm resultingAst = useSourceAst ? null : resultingAsts.get(resource);
		IStrategoList termPath = StrategoTermPath.getTermPathWithOrigin(context, resultingAst, node);
		IStrategoTerm targetTerm;
		IStrategoTerm rootTerm;
		
		if (termPath != null) {
			targetTerm = StrategoTermPath.getTermAtPath(context, resultingAst, termPath);
			rootTerm = resultingAst;
		} else {
			targetTerm = node;
			termPath = StrategoTermPath.createPath(node);
			rootTerm = getRoot(node);
		}
		
		ITermFactory factory = Environment.getTermFactory();
		String path = resource.getProjectRelativePath().toPortableString();
		String absolutePath = tryGetProjectPath(resource);
		
		if (includeSubNode) {
			IStrategoTerm[] inputParts = {
					targetTerm,
					termPath,
					rootTerm,
					factory.makeString(path),
					factory.makeString(absolutePath)
				};
			return factory.makeTuple(inputParts);
		} else {
			IStrategoTerm[] inputParts = {
					node,
					factory.makeString(path),
					factory.makeString(absolutePath)
				};
			return factory.makeTuple(inputParts);
		}
	}

	protected String tryGetProjectPath(IResource resource) {
		return resource.getProject() != null && resource.getProject().exists()
				? resource.getProject().getLocation().toString()
				: resource.getFullPath().removeLastSegments(1).toString();
	}

	/**
	 * Create an input term for a control rule,
	 * based on the IStrategoTerm syntax of the AST of the source file.
	 */
	public IStrategoTuple makeATermInputTerm(IStrategoTerm node, boolean includeSubNode, IResource resource) {
		stratego_aterm.init(runtime.getCompiledContext());
		
		ITermFactory factory = Environment.getTermFactory();
		String path = resource.getProjectRelativePath().toPortableString();
		String absolutePath = resource.getProject().getLocation().toOSString();
		
		if (includeSubNode) {
			node = getImplodableNode(node);
			IStrategoTerm[] inputParts = {
					implodeATerm(node),
					StrategoTermPath.createPathFromParsedIStrategoTerm(node, runtime.getCompiledContext()),
					implodeATerm(getRoot(node)),
					factory.makeString(path),
					factory.makeString(absolutePath)
				};
			return factory.makeTuple(inputParts);
		} else {
			throw new org.spoofax.NotImplementedException();
		}
	}

	protected IStrategoTerm implodeATerm(IStrategoTerm term) {
		return implode_aterm_0_0.instance.invoke(runtime.getCompiledContext(), term);
	}

	public IStrategoTerm getImplodableNode(IStrategoTerm node) {
		if (node.isList() && node.getSubtermCount() == 1)
			node = node.getSubterm(0);
		for (; node != null; node = getParent(node)) {
			if (implodeATerm(node) != null)
				return node;
		}
		throw new IllegalStateException("Could not identify selected AST node from IStrategoTerm editor");
	}
}
