package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.interpreter.core.Tools.asJavaString;
import static org.spoofax.interpreter.core.Tools.isTermString;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * A primitive used to programmaticaly override the next user input dialog
 * to return a fixed value rather than show the dialog and ask the user.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class OverrideInputPrimitive extends AbstractPrimitive {

	public static final String NAME = "SSL_EXT_overrideinput";
	
	private String lastOverride;
	
	public OverrideInputPrimitive() {
		super(NAME, 0, 1);
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		if (isTermString(tvars[0])) {
			lastOverride = asJavaString(tvars[0]);
			return true;
		} else {
			return false;
		}
	}
	
	public String getOverrideValue() {
		String result = lastOverride;
		lastOverride = null;
		return result;
	}

}
