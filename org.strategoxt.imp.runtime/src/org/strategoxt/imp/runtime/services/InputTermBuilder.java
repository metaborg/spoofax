package org.strategoxt.imp.runtime.services;

import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getSort;
import static org.spoofax.terms.Term.tryGetConstructor;
import static org.spoofax.terms.attachments.OriginAttachment.tryGetOrigin;
import static org.spoofax.terms.attachments.ParentAttachment.getParent;
import static org.spoofax.terms.attachments.ParentAttachment.getRoot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.TermTreeFactory;
import org.spoofax.terms.StrategoSubList;
import org.strategoxt.HybridInterpreter;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;
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
	
	private final IStrategoTerm resultingAst;
	
	public InputTermBuilder(HybridInterpreter runtime, Map<IResource, IStrategoTerm> resultingAsts) {
		this.runtime = runtime;
		this.resultingAsts = resultingAsts;
		this.resultingAst = null;
	}
	
	public InputTermBuilder(HybridInterpreter runtime, IStrategoTerm resultingAst) {
		this.runtime = runtime;
		this.resultingAsts = EMPTY_MAP;
		this.resultingAst = resultingAst;
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
		if(useSourceAst)
			return makeInputTermSourceAst(node, includeSubNode);
		return makeInputTermResultingAst(node, includeSubNode);
	}

	public IStrategoTuple makeInputTermResultingAst(IStrategoTerm node, boolean includeSubNode) {
		IStrategoTerm resultingAst; 
		if (this.resultingAst != null) 
			resultingAst = this.resultingAst;
		else {
			IResource resource = SourceAttachment.getResource(node);
			resultingAst = resultingAsts.get(resource);
		}
		return makeInputTermResultingAst(resultingAst, node, includeSubNode);
	}

	public IStrategoTuple makeInputTermResultingAst(IStrategoTerm resultingAst,
			IStrategoTerm node, boolean includeSubNode) {
		Context context = runtime.getCompiledContext();
		IStrategoList termPath = StrategoTermPath.getTermPathWithOrigin(context, resultingAst, node);
		if (termPath == null)
			return makeInputTermSourceAst(node, includeSubNode);
		IStrategoTerm targetTerm = StrategoTermPath.getTermAtPath(context, resultingAst, termPath);
		if(node instanceof StrategoSubList){
			if(!(targetTerm instanceof IStrategoList))
				return makeInputTermSourceAst(node, includeSubNode);
			targetTerm = mkSubListTarget(resultingAst, (IStrategoList)targetTerm, (StrategoSubList)node);
			if(targetTerm == null) //only accept sublists that correspond to selection
				return makeInputTermSourceAst(node, includeSubNode); 
		}
		IStrategoTerm rootTerm = resultingAst;		
		return makeInputTerm(node, includeSubNode, termPath, targetTerm, rootTerm);
	}

	private IStrategoTerm mkSubListTarget(IStrategoTerm resultingAst, IStrategoList targetTerm, StrategoSubList node) {
		IStrategoTerm firstChild = getResultingTerm(resultingAst, node.getFirstChild());
		IStrategoTerm lastChild = getResultingTerm(resultingAst, node.getLastChild());
		if(firstChild == null || lastChild == null)
			return null;
		return new TermTreeFactory(Environment.getTermFactory()).createSublist(targetTerm, firstChild, lastChild); 
	}

	private IStrategoTerm getResultingTerm(IStrategoTerm resultingAst, IStrategoTerm originTerm) {
		Context context = runtime.getCompiledContext();
		IStrategoList pathFirstChild = StrategoTermPath.getTermPathWithOrigin(context, resultingAst, originTerm);
		IStrategoTerm firstChild = null;
		if(pathFirstChild != null)
			firstChild = StrategoTermPath.getTermAtPath(context, resultingAst, pathFirstChild);
		return firstChild;
	}

	public IStrategoTuple makeInputTermSourceAst(IStrategoTerm node, boolean includeSubNode) {
		IStrategoTerm targetTerm = node;
		IStrategoList termPath = StrategoTermPath.createPath(node);
		IStrategoTerm rootTerm = getRoot(node);
		return makeInputTerm(node, includeSubNode, termPath, targetTerm, rootTerm);
	}

	public IStrategoTuple makeInputTerm(IStrategoTerm node, boolean includeSubNode,
			IStrategoList termPath, IStrategoTerm targetTerm,
			IStrategoTerm rootTerm) {
		ITermFactory factory = Environment.getTermFactory();
		assert factory.getDefaultStorageType() == IStrategoTerm.MUTABLE;
		File file = SourceAttachment.getFile(node);
		IPath project = tryGetProjectPath(file);
		String path, projectPath;
		if (file != null && project != null) {
			projectPath = project.toPortableString();
			IPath fullPath = new Path(file.getAbsolutePath());
			path = fullPath.removeFirstSegments(fullPath.matchingFirstSegments(project)).setDevice("").toPortableString();
			assert !new File(path).isAbsolute();
		} else {
			projectPath = ".";
			path = "string";
		}
		
		if (includeSubNode) {
			IStrategoTerm[] inputParts = {
					targetTerm,
					termPath,
					rootTerm,
					factory.makeString(path),
					factory.makeString(projectPath)
				};
			return factory.makeTuple(inputParts);
		} else {
			IStrategoTerm[] inputParts = {
					node,
					factory.makeString(path),
					factory.makeString(projectPath)
				};
			return factory.makeTuple(inputParts);
		}
	}

	private IPath tryGetProjectPath(File file) {
		try {
			if (file == null) return null;
			return EditorIOAgent.getProject(file).getLocation();
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	public IStrategoTerm makeInputTermRefactoring(IStrategoTerm userInput, IStrategoTerm node, boolean includeSubNode, boolean source) {
		IStrategoTuple tuple = makeInputTerm(node, includeSubNode, source);
		ITermFactory factory = Environment.getTermFactory();
		IStrategoTerm[] inputParts = new IStrategoTerm[tuple.getSubtermCount() + 1];
		inputParts[0] = userInput;
		System.arraycopy(tuple.getAllSubterms(), 0, inputParts, 1, tuple.getSubtermCount());
		return factory.makeTuple(inputParts); 
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
		String absolutePath = resource.getProject().getLocation().toPortableString();
		
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

	public IStrategoTerm implodeATerm(IStrategoTerm term) {
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

	/**
	 * Gets the node furthest up the ancestor chain that
	 * has either the same character offsets or has only one
	 * child with the same character offsets as the node given.
	 * Won't traverse up list parents.
	 * 
	 * @param allowMultiChildParent
	 *             Also fetch the first parent if it has multiple children (e.g., Call("foo", "bar")).
	 */
	public static final IStrategoTerm getMatchingAncestor(IStrategoTerm oNode, boolean allowMultiChildParent) {
		if (oNode.isList()) return oNode;
		
		if (allowMultiChildParent && tryGetConstructor(oNode) == null && getParent(oNode) != null)
			return getParent(oNode);
		
		IStrategoTerm result = oNode;
		IToken left = getLeftToken(tryGetOrigin(result));
		if (left == null) return oNode;
		int startOffset = left.getStartOffset();
		int endOffset = getRightToken(tryGetOrigin(result)).getEndOffset();
		while (getParent(result) != null
				&& !getParent(result).isList()
				&& (getParent(result).getSubtermCount() <= 1 
						|| (getLeftToken(tryGetOrigin(getParent(result))).getStartOffset() >= startOffset
							&& getRightToken(tryGetOrigin(getParent(result))).getEndOffset() <= endOffset)))
			result = getParent(result);
		return result;
	}

	/**
	 * Gets a node that has either the same character offsets or has only one
	 * child with the same character offsets as the node given,
	 * meeting the additional criteria that this node matches the semantic nodes. 
	 * Returns null in case no match is found
	 * 
	 * @param semanticNodes
	 *             Define Sorts and/or Constructors that should apply. (example: Stm+ ID)
	 * @param allowMultiChildParent
	 *             Also fetch the first parent if it has multiple children (e.g., Call("foo", "bar")).
	 */
	public static final IStrategoTerm getMatchingNode(IStrategoTerm[] semanticNodes,
			IStrategoTerm node, boolean allowMultiChildParent) throws BadDescriptorException {
		if (node == null)
			return null;
		IStrategoTerm ancestor = InputTermBuilder.getMatchingAncestor(node, allowMultiChildParent);
		IStrategoTerm selectionNode = node;
		ArrayList<NodeMapping<String>> mappings = createNodeMappings(semanticNodes);
		if (mappings.size() == 0) {
			return ancestor; // no sort restriction specified, so use policy to
							 // return the node furthest up the ancestor
							 // chain
		}
		boolean isMatch = isMatchOnConstructorOrSort(mappings, selectionNode);
		while (!isMatch && selectionNode != null && selectionNode != getParent(ancestor)) {
			selectionNode = getParent(selectionNode);
			isMatch = isMatchOnConstructorOrSort(mappings, selectionNode);
		}
		// Creates a sublist with single element.
		// Usecase: extract refactoring is defined on a (sub)list (refactoring
		// X+: ...) and should be applicable when only one X is selected
		if (!isMatch && !ancestor.isList() && getParent(ancestor) != null && getParent(ancestor).isList()) {
			selectionNode = new TermTreeFactory(Environment.getTermFactory()).createSublist((IStrategoList) getParent(ancestor), ancestor, ancestor); 
			isMatch = isMatchOnConstructorOrSort(mappings, selectionNode);
		}
		//some tolerance for example when method name is selected instead of method
		while (!isMatch && selectionNode != null && !selectionNode.isList() && getParent(selectionNode) != null && !getParent(selectionNode).isList()) {
			selectionNode = getParent(selectionNode);
			isMatch = isMatchOnConstructorOrSort(mappings, selectionNode);
		}
		if (isMatch) {
			return selectionNode;
		}
		return null;
	}

	public static ArrayList<NodeMapping<String>> createNodeMappings(IStrategoTerm[] semanticNodes){
		ArrayList<NodeMapping<String>> mappings = new ArrayList<NodeMapping<String>>();
		for (IStrategoTerm semanticNode : semanticNodes) {
			NodeMapping<String> aMapping;
			try {
				aMapping = NodeMapping.create(semanticNode, "");
				mappings.add(aMapping);
			} catch (BadDescriptorException e) {
				e.printStackTrace();
			}
		}
		return mappings;
	}

	public static boolean isMatchOnConstructorOrSort(ArrayList<NodeMapping<String>> mappings, IStrategoTerm selectionNode) {
		return selectionNode != null &&
			NodeMapping.getFirstAttribute(mappings, tryGetName(selectionNode), getSort(selectionNode), 0) != null;
	}

	private static String tryGetName(IStrategoTerm term) {
		IStrategoConstructor cons = tryGetConstructor(term);
		return cons == null ? null : cons.getName();
	}
}