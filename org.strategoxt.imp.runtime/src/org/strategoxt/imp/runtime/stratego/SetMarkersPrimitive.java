package org.strategoxt.imp.runtime.stratego;

import org.eclipse.core.resources.IResource;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.library.ssl.SSLLibrary;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.services.StrategoAnalysisJob;
import org.strategoxt.imp.runtime.services.StrategoObserver;

public class SetMarkersPrimitive extends AbstractPrimitive {

	public SetMarkersPrimitive() {
		super("SSL_EXT_set_markers", 0, 1);
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {

		IResource resource = SourceAttachment.getResource(tvars[0]);
		if (resource == null)
			return false;

		IOAgent agent = SSLLibrary.instance(env).getIOAgent();
		if (!(agent instanceof EditorIOAgent)) return false;

		StrategoAnalysisJob job = ((EditorIOAgent)agent).getJob();

		StrategoObserver observer = job.getObserver();

		// HACK: presentToUser runs a feedback postprocess strategy which calls primitives witch set env.current()
		// (nested primitives...)
		IStrategoTerm previousTerm = env.current();
		observer.presentToUser(resource, env.current());
		env.setCurrent(previousTerm);

		return true;
	}

}
