package org.strategoxt.imp.runtime.stratego.adapter;

import lpg.runtime.IAst;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermPrinter;

/**
 * A constructor application AST node wrapped into an ATerm.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 * @author Karl Trygve Kalleberg <karltk add strategoxt.org>
 */
public class WrappedAstNodeAppl extends WrappedAstNode implements IStrategoAppl {
	private final IStrategoConstructor constructor;

	public IStrategoConstructor getConstructor() {
		return constructor;
	}
	
	protected WrappedAstNodeAppl(WrappedAstNodeFactory factory, IStrategoAstNode node) {
		super(factory, node);
		
		constructor = factory.makeConstructor(node.getConstructor(), node.getChildren().size());
	}
	
	protected WrappedAstNodeAppl(WrappedAstNodeFactory factory, IAst node) {
		super(factory, node);
		
		constructor = factory.makeConstructor(node.getClass().getSimpleName(), node.getChildren().size());
	}
	
	public IStrategoTerm[] getArguments() {
		return getAllSubterms();
	}

	@Override
    protected boolean slowCompare(IStrategoTerm second) {
        if(!(second instanceof IStrategoAppl))
            return false;
        IStrategoAppl snd = (IStrategoAppl) second;
        if(!snd.getConstructor().equals(getConstructor()))
            return false;
        for(int i = 0, sz = getSubtermCount(); i < sz; i++) {
            if(!snd.getSubterm(i).equals(getSubterm(i)))
                return false;
        }
        return true;
    }

    public void prettyPrint(ITermPrinter pp) {
    	final String name = constructor.getName() == null ? "<#null>" : constructor.getName();
    	if(name.equals("[]")) {
    		pp.print("[");
    	} else {
    		pp.print(name);
            pp.println("(");
    	}
        final IStrategoTerm[] kids = getAllSubterms();
        if(kids.length > 0) {
            pp.indent(name.length());
            if(kids[0] == null) {
            	pp.print("<#null>");
            } else {
            	kids[0].prettyPrint(pp);
            }
            for(int i = 1; i < kids.length; i++) {
                pp.print(", ");
                if(kids[1] == null)
                	pp.print("<#null>");
                else
                	kids[i].prettyPrint(pp);
            }
            pp.outdent(name.length());
        }
        if(name.equals("[]")) {
        	pp.println("]");
        } else {
        	pp.println(")");
        }
    }

	public int getTermType() {
		return IStrategoTerm.APPL;
	}
}
