package org.strategoxt.imp.runtime.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.generator.construct_textual_change_4_0;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.dynamicloading.RefactoringFactory;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.stratego.SourceAttachment;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

/**
 * @author Oskar van Rest
 */
public class TextReplacer implements ITextReplacer {

	private Descriptor descriptor;
	private SGLRParseController controller;
	
	public TextReplacer(Descriptor descriptor, SGLRParseController controller) {
		this.descriptor = descriptor;
		this.controller = controller;
	}

	public void replaceText(IStrategoTerm resultTuple) {		
		IStrategoTerm textreplace = null;
		try {
			File file = SourceAttachment.getFile(controller.getCurrentAst());
			StrategoObserver observer = descriptor.createService(StrategoObserver.class, controller);
			textreplace = construct_textual_change_4_0.instance.invoke(
					observer.getRuntime().getCompiledContext(), 
					resultTuple, 
					createStrategy(RefactoringFactory.getPPStrategy(descriptor), file, observer),
					createStrategy(RefactoringFactory.getParenthesizeStrategy(descriptor), file, observer),
					createStrategy(RefactoringFactory.getOverrideReconstructionStrategy(descriptor), file, observer),
					createStrategy(RefactoringFactory.getResugarStrategy(descriptor), file, observer)
				);
		} catch (BadDescriptorException e) {
			e.printStackTrace();
		}
		
		writeToFile((IStrategoString) textreplace.getSubterm(0).getSubterm(2));
		queueAnalysisAffectedFile();
	}
	
	private Strategy createStrategy(final String sname, final File file, final StrategoObserver observer) {
		return new Strategy() {
			@Override
			public IStrategoTerm invoke(Context context, IStrategoTerm current) {
				if (sname!=null)
					return observer.invokeSilent(sname, current, file);
				return null;
			}
		};
	}
	
	// does this method exists already somewhere?
	private void writeToFile(IStrategoString term) {
		try {
			File file = SourceAttachment.getFile(controller.getCurrentAst());
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(term.stringValue());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		IResource resource = controller.getResource();
		try {
			resource.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	private void queueAnalysisAffectedFile() {
		IPath path = controller.getResource().getProjectRelativePath();
		IProject project = controller.getEditor().getProject().getRawProject();
		StrategoAnalysisQueueFactory.getInstance().queueAnalysis(path, project, true);
	}
}
