/**
 * 
 */
package org.strategoxt.imp.runtime.stratego;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.ssl.SSLLibrary;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.services.StrategoAnalysisQueueFactory;
import org.strategoxt.imp.runtime.services.StrategoObserverBackgroundJob;
import org.strategoxt.imp.runtime.services.StrategoAnalysisQueue.UpdateJob;

/**
 * @author Nathan Bruning
 *
 */
public class QueueStrategyPrimitive extends AbstractPrimitive {

	QueueStrategyPrimitive() {
		super("SSL_EXT_queue_strategy", 0, 2);
	}
	
	/**
	 * @see org.spoofax.interpreter.library.AbstractPrimitive#call(org.spoofax.interpreter.core.IContext, org.spoofax.interpreter.stratego.Strategy[], org.spoofax.interpreter.terms.IStrategoTerm[])
	 */
	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {

		try {
			
			// Get descriptor
			String strategyName = ((IStrategoString)tvars[0]).stringValue();
			String jobDescription = ((IStrategoString)tvars[1]).stringValue();
			EditorIOAgent agent = (EditorIOAgent) SSLLibrary.instance(env).getIOAgent();
			
			IStrategoTerm term = env.current();
			Descriptor descriptor = agent.getDescriptor();
			
			IPath projectPath = new Path(agent.getProjectPath());
			IPath workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
			IPath workspaceLocalPath = projectPath.removeFirstSegments(projectPath.matchingFirstSegments(workspacePath));
			IProject project = ResourcesPlugin.getWorkspace().getRoot().findMember(workspaceLocalPath).getProject();
			
			StrategoObserverBackgroundJob job = new StrategoObserverBackgroundJob(strategyName, term, descriptor);
			job.setup(project);
			
			UpdateJob updateJob = StrategoAnalysisQueueFactory.getInstance().queue(job, project);
			updateJob.setName(jobDescription);
			
			return true;
			
		} catch (ClassCastException e) {
			// Wrong term input.
		}
		return false;
		
	}
	
}
