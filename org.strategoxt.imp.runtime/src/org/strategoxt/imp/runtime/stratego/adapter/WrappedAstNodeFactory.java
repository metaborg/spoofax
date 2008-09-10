package org.strategoxt.imp.runtime.stratego.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.WeakHashMap;

import org.spoofax.interpreter.terms.BasicTermFactory;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.interpreter.terms.TermConverter;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.ast.IntAstNode;
import org.strategoxt.imp.runtime.parser.ast.StringAstNode;

import lpg.runtime.IAst;

/**
 * A factory creating ATerms from AST nodes.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 * @author Karl Trygve Kalleberg <karltk add strategoxt.org>
 */
public class WrappedAstNodeFactory extends BasicTermFactory implements ITermFactory {	
	private static final WeakHashMap<IAst, WrappedAstNode> cache
		= new WeakHashMap<IAst, WrappedAstNode>();
	
	private final TermConverter converter = new TermConverter(this);

	public IStrategoTerm wrap(IAst node) {
		if (node instanceof IStrategoAstNode) {
			// Get associated term from IStrategoAstNode, may be cached
			return ((IStrategoAstNode) node).getTerm();
		} else {
			WrappedAstNode result = cache.get(node);
			if (result != null) return result;
		}
		
		return wrapNew(node);
	}

	public IStrategoTerm wrapNew(IAst node) {
		// TODO: Wrap lists??
		if(node instanceof IntAstNode) {
			return new WrappedAstNodeInt(this, (IntAstNode)node);
		} else if(node instanceof WrappedAstNodeString) {
			return new WrappedAstNodeString(this, (StringAstNode)node);
		} else {
			return new WrappedAstNodeAppl(this, node);
		}
	}
	
	@Override
	public IStrategoTerm parseFromStream(InputStream inputStream) throws IOException {
		// BasicTermFactory does not support binary aterms atm
		IStrategoTerm result = Environment.getWrappedTermFactory().parseFromStream(inputStream);
		return converter.convert(result);
	}
	
	@Override
	public IStrategoTerm parseFromFile(String path) throws IOException {
		// BasicTermFactory does not support binary aterms atm
		IStrategoTerm result = Environment.getWrappedTermFactory().parseFromFile(path);
		return converter.convert(result);
	}
	
	@Override
	public IStrategoTerm parseFromString(String path) {
		// BasicTermFactory does not support binary aterms atm
		IStrategoTerm result = Environment.getWrappedTermFactory().parseFromString(path);
		return converter.convert(result);
	}

	public IStrategoTerm wrapNew(IStrategoAstNode node) {
		if(node instanceof IntAstNode) {
			return new WrappedAstNodeInt(this, (IntAstNode)node);
		} else if(node instanceof StringAstNode) {
			return new WrappedAstNodeString(this, (StringAstNode)node);
		} else {
			return new WrappedAstNodeAppl(this, node);
		}
	}
	
	@Override
	public IStrategoTerm annotate(IStrategoTerm term, IStrategoList annotations) {
		if (term instanceof AnnotatedAstNode)
			return annotate(((AnnotatedAstNode) term).getWrapped(), annotations);
		
		return new AnnotatedAstNode(term, annotations);
	}
}
