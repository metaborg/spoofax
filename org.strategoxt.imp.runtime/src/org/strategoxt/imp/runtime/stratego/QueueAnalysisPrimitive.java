/**
 * 
 */
package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.interpreter.core.Tools.isTermList;
import static org.spoofax.interpreter.core.Tools.isTermString;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.imp.language.LanguageRegistry;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.library.ssl.SSLLibrary;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.services.StrategoAnalysisQueueFactory;

/**
 * @author Nathan Bruning
 *
 */
public class QueueAnalysisPrimitive extends AbstractPrimitive {

	public static final String NAME = "SSL_EXT_queue_analysis";
	
	QueueAnalysisPrimitive() {
		super(NAME, 0, 0);
	}
	
	/**
	 * @see org.spoofax.interpreter.library.AbstractPrimitive#call(org.spoofax.interpreter.core.IContext, org.spoofax.interpreter.stratego.Strategy[], org.spoofax.interpreter.terms.IStrategoTerm[])
	 */
	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {
		
		IStrategoTerm term = env.current();
		if(isTermString(term))
			queue(env, (IStrategoString)term);
		else if(isTermList(term))
			queueMultiple(env, (IStrategoList)term);
		else
			return false;
		
		return true;
	}

	private IPath relativePath(IStrategoString fileString, IProject project) throws InterpreterException {
		try {
			URI file = new URI(fileString.stringValue());
			assert file.isAbsolute();
			IPath path = new Path(file.toString());
			if(LanguageRegistry.findLanguage(path, null) != null)
			{
				IPath relPath = path.removeFirstSegments(path.matchingFirstSegments(project.getLocation()));
				assert !relPath.isAbsolute();
				return relPath;
			}
		} catch (URISyntaxException e) {
			throw new InterpreterException(e);
		}

		return null;
	}
	
	private IProject project(IContext env) throws InterpreterException {
		IOAgent agent = SSLLibrary.instance(env).getIOAgent();
		if (!(agent instanceof EditorIOAgent) || ((EditorIOAgent) agent).getProjectPath() == null) {
			throw new InterpreterException("Agent not instanceof EditorIOAgent or not in project.");
		}
		EditorIOAgent editorAgent = (EditorIOAgent)agent;
		IProject project = editorAgent.getProject();
		return project;
	}
	
	private void queue(IContext env, IStrategoString file) throws InterpreterException {
		IProject project = project(env);
		StrategoAnalysisQueueFactory.getInstance().queueAnalysis(relativePath(file, project), project, false);
	}
	
	private void queueMultiple(IContext env, IStrategoList files) throws InterpreterException {
		IProject project = project(env);
		List<IPath> relativePaths = new ArrayList<IPath>(files.size());
		IStrategoList filesLoop = files;
		while(filesLoop.tail() != null) {
			IStrategoTerm fileTerm = filesLoop.head();
			if(!isTermString(fileTerm))
				break; // TODO: Error?
			
			IPath path = relativePath((IStrategoString)fileTerm, project);
			if(path != null)
				relativePaths.add(path);
			
			filesLoop = filesLoop.tail();
		}
		
		StrategoAnalysisQueueFactory.getInstance().queueAnalysis(relativePaths.toArray(new IPath[0]), project, false);
	}
}
