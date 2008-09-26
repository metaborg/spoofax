package org.strategoxt.imp.runtime.stratego.adapter;

import java.io.IOException;
import java.io.InputStream;

import org.spoofax.interpreter.terms.BasicTermFactory;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.interpreter.terms.TermConverter;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.ast.IntAstNode;
import org.strategoxt.imp.runtime.parser.ast.ListAstNode;
import org.strategoxt.imp.runtime.parser.ast.StringAstNode;

/**
 * A factory creating ATerms from AST nodes.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 * @author Karl Trygve Kalleberg <karltk add strategoxt.org>
 */
public class WrappedAstNodeFactory extends BasicTermFactory implements ITermFactory {	
	private final TermConverter converter = new TermConverter(this);

	public IStrategoTerm wrap(IStrategoAstNode node) {
		if(node instanceof IntAstNode) {
			return new WrappedAstNodeInt(this, (IntAstNode) node);
		} else if(node instanceof StringAstNode) {
			return new WrappedAstNodeString(this, (StringAstNode) node);
		} else if (node instanceof ListAstNode) {
			return new WrappedAstNodeList(this, node);
		} else {
			return new WrappedAstNodeAppl(this, node);
		}
	}
	
	// Parsing
	
	@Override
	public IStrategoTerm parseFromStream(InputStream inputStream) throws IOException {
		// BasicTermFactory does not support binary aterms atm
		IStrategoTerm result = Environment.getWrappedATermFactory().parseFromStream(inputStream);
		return converter.convert(result);
	}
	
	@Override
	public IStrategoTerm parseFromFile(String path) throws IOException {
		// BasicTermFactory does not support binary aterms atm
		IStrategoTerm result = Environment.getWrappedATermFactory().parseFromFile(path);
		return converter.convert(result);
	}
	
	@Override
	public IStrategoTerm parseFromString(String path) {
		// BasicTermFactory does not support binary aterms atm
		IStrategoTerm result = Environment.getWrappedATermFactory().parseFromString(path);
		return converter.convert(result);
	}
	
	// Annotations
	
	@Override
	public IStrategoTerm annotateTerm(IStrategoTerm term, IStrategoList annotations) {
		if (term instanceof WrappedAstNode) {
			WrappedAstNode result = ((WrappedAstNode) term).clone();
			result.internalSetAnnotations(annotations);
			return result;
		} else {
			return super.annotateTerm(term, annotations);
		}
	}
	
	// Origin tracking
	
	@Override
	public IStrategoAppl replaceAppl(IStrategoConstructor constructor, IStrategoTerm[] kids,
			IStrategoAppl oldTerm) {
		
		IStrategoAppl result = makeAppl(constructor, ensureLinks(kids, oldTerm));
		
		return (IStrategoAppl) ensureLink(result, oldTerm);
	}
	
	@Override
	public IStrategoTuple replaceTuple(IStrategoTerm[] kids, IStrategoTuple old) {
		return (IStrategoTuple) ensureLink(makeTuple(ensureLinks(kids, old)), old);
	}
	
	@Override
	public IStrategoList replaceList(IStrategoTerm[] kids, IStrategoList old) {
		return (IStrategoList) ensureLink(makeList(ensureLinks(kids, old)), old);
	}

	private IStrategoTerm[] ensureLinks(IStrategoTerm[] kids, IStrategoTerm oldTerm) {
		IStrategoTerm[] linkedKids = new IStrategoTerm[kids.length];
		
		for (int i = 0; i < kids.length; i++) {
			linkedKids[i] = ensureLink(kids[i], oldTerm.getSubterm(i));
		}
		return linkedKids;
	}
	
	private IStrategoTerm ensureLink(IStrategoTerm term, IStrategoTerm oldTerm) {
		if (term instanceof IWrappedAstNode) {
			return (IWrappedAstNode) term;
		} else if (oldTerm instanceof IWrappedAstNode) {
			return new WrappedAstNodeLink(this, term, ((IWrappedAstNode) oldTerm).getNode());
		} else {
			// TODO: Add a link to the parent node for children that do not have tracking info?
			return term;
		}
	}
}
