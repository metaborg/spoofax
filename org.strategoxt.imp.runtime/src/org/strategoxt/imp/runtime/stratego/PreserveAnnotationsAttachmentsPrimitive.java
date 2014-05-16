package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

/**
 * Preserves the annotations and attachments of the current term after applying given strategy.
 */
public class PreserveAnnotationsAttachmentsPrimitive extends AbstractPrimitive {

	public PreserveAnnotationsAttachmentsPrimitive() {
		super("SSL_EXT_preserve_annotations_attachments", 1, 0);
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
		final IStrategoTerm before = env.current();
		final boolean result = svars[0].evaluate(env);
		if(!result)
			return false;
		final ITermFactory factory = env.getFactory();
		IStrategoTerm after = env.current();
		after = factory.annotateTerm(after, before.getAnnotations());
		after = factory.copyAttachments(before, after);
		env.setCurrent(after);
		return true;
	}

}
