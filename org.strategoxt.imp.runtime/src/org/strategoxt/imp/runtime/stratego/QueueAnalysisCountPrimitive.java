package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.imp.runtime.services.StrategoAnalysisQueueFactory;


/**
 * @author Gabriël Konat
 */
public class QueueAnalysisCountPrimitive extends AbstractPrimitive {

	public static final String NAME = "SSL_EXT_queue_analysis_count";
	
	QueueAnalysisCountPrimitive() {
		super(NAME, 0, 0);
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {
		final int count = StrategoAnalysisQueueFactory.getInstance().pendingUpdatesSize();
		final ITermFactory factory = env.getFactory();
		env.setCurrent(factory.makeInt(count));
		
		return true;
	}
}
