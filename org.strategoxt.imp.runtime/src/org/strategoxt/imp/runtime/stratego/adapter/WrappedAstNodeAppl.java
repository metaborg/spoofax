package org.strategoxt.imp.runtime.stratego.adapter;

import lpg.runtime.IAst;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoTerm;

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
	
	public boolean match(IStrategoTerm second) {
		// TODO Auto-generated method stub
		return false;
	}
    
    @Override
    public boolean equals(Object other) {
        if(other instanceof WrappedAstNode) {
            if(other instanceof WrappedAstNodeAppl) {
                return ((WrappedAstNodeAppl) other).getNode().equals(getNode());
            }
            return false;
        }
        return slowCompare(other);
    }

    protected boolean slowCompare(Object second) {
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
}
