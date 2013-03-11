/**
 * 
 */
package org.strategoxt.imp.runtime.statistics;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.stratego.SourceAttachment;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 *
 */
public class RecordedEdit {

	public final long timestamp;
	public final IStrategoTerm ast;
	public final IResource resource;
	public final IPath project;
	
	public RecordedEdit(IStrategoTerm ast, long timestamp) {
		assert ast != null;
		this.ast = ast;
		this.timestamp = timestamp;
		this.resource = SourceAttachment.getResource(ast);
		this.project = this.resource.getProject().getLocation();
	}
	
	
}
