package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.library.ssl.SSLLibrary;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.services.StrategoAnalysisJob;
import org.strategoxt.imp.runtime.services.StrategoObserver;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;
import org.strategoxt.imp.runtime.stratego.adapter.WrappedAstNode;

public class SetMarkersPrimitive extends AbstractPrimitive {

	public SetMarkersPrimitive() {
		super("SSL_EXT_set_markers", 0, 2);
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {

		if (!(tvars[0] instanceof WrappedAstNode))
			return false;
		
		WrappedAstNode appl = (WrappedAstNode)tvars[0]; 
		IStrategoAstNode ast = appl.getNode();
		
		IOAgent agent = SSLLibrary.instance(env).getIOAgent();
		if (!(agent instanceof EditorIOAgent)) return false;
		
		StrategoAnalysisJob job = ((EditorIOAgent)agent).getJob();
		
		StrategoObserver observer = job.getObserver();
		observer.presentToUser(ast.getResource(), env.current());
		
		return true;
	}

}
