package org.strategoxt.imp.runtime.stratego;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;

/**
 * @author Adil Akhter
 */
public class GetAllProjectsPrimitive extends AbstractPrimitive {

	private static String NAME = "SSL_EXT_get_all_projects_in_workspace";
	
	public GetAllProjectsPrimitive() {
		super(NAME, 0, 0);
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		ITermFactory factory = env.getFactory();
		IStrategoList results = factory.makeList();
		
		for (IProject project: projects) {
			IStrategoString projectName =factory.makeString( project.getName());
			IStrategoString projectPath =factory.makeString( project.getLocation().toString());
			
			// Creating tuple for each project entry. Tuple contains project name and project path.
			IStrategoTuple result = factory.makeTuple(
					projectName ,
					projectPath
			);		
			// Adding it to the head of the list 
			results = factory.makeListCons(result, results);
		}
		env.setCurrent(results);
		return true;
	}

}
