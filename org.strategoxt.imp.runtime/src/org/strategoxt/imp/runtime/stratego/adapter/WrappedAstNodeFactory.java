package org.strategoxt.imp.runtime.stratego.adapter;

import java.io.IOException;
import java.io.InputStream;

import org.spoofax.interpreter.terms.BasicTermFactory;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
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
			return new WrappedAstNodeInt(this, (IntAstNode)node);
		} else if(node instanceof StringAstNode) {
			return new WrappedAstNodeString(this, (StringAstNode)node);
		} else if (node instanceof ListAstNode) {
			return new WrappedAstNodeList(this, node);
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
	
	@Override
	public IStrategoTerm replaceAppl(IStrategoConstructor constructor, final IStrategoTerm[] kids,
			IStrategoTerm old) {
		
		if (old instanceof IWrappedAstNode) {
			return new WrappedAstNodeAppl(this, ((IWrappedAstNode) old).getNode()) {
				@Override
				public IStrategoTerm getSubterm(int index) {
					return kids[index];
				}
				
				@Override
				public int getSubtermCount() {
					return kids.length;
				}
			};
		} else {
			return super.replaceAppl(constructor, kids, old);
		}
	}
}
