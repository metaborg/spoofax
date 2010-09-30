/**
 * 
 */
package org.strategoxt.imp.runtime.stratego;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.library.ssl.SSLLibrary;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.services.StrategoAnalysisQueueFactory;

/**
 * @author Nathan Bruning
 *
 */
public class QueueAnalysisPrimitive extends AbstractPrimitive {

	QueueAnalysisPrimitive() {
		super("SSL_EXT_queue_analysis", 0, 0);
	}
	
	/**
	 * @see org.spoofax.interpreter.library.AbstractPrimitive#call(org.spoofax.interpreter.core.IContext, org.spoofax.interpreter.stratego.Strategy[], org.spoofax.interpreter.terms.IStrategoTerm[])
	 */
	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {

		try {
			
			IStrategoTerm term = env.current();
			queue(env, (IStrategoString)term);
			
			return true;
			
		} catch (ClassCastException e) {
			// Wrong term input.
		}
		return false;
		
	}

	private IPath stringToPath(IStrategoString string) {
		return new Path(string.stringValue());
	}
	
	protected void queue(IContext env, IStrategoString fullPath) throws InterpreterException {
		
		IPath filePath = stringToPath(fullPath);
		
		IOAgent agent = SSLLibrary.instance(env).getIOAgent();
		if (!(agent instanceof EditorIOAgent) || ((EditorIOAgent) agent).getProjectPath() == null) {
			throw new InterpreterException("Agent not instanceof EditorIOAgent or not in project.");
		}
		EditorIOAgent editorAgent = (EditorIOAgent)agent;
		
		IProject project = editorAgent.getProject();
		
		String projectPathStr = editorAgent.getProjectPath();
		IPath projectPath = new Path(projectPathStr); 
		
		if (filePath.isAbsolute()) {
			// Test if in project; can't parse it otherwise
			if (!projectPath.isPrefixOf(filePath)) {
				Environment.logException("Trying to analyze out-of-project file: " + filePath);
				return;
			}
		}
		
		// Make path project local
		IPath projectRelativePath = filePath.removeFirstSegments(filePath.matchingFirstSegments(projectPath)); 
		StrategoAnalysisQueueFactory.getInstance().queueAnalysis(projectRelativePath, project);
		
	}
	
}
