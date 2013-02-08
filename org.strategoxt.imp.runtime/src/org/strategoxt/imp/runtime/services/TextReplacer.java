package org.strategoxt.imp.runtime.services;

import java.io.File;

import org.eclipse.swt.widgets.Display;
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
		try {
			File file = SourceAttachment.getFile(controller.getCurrentAst());
			StrategoObserver observer = descriptor.createService(StrategoObserver.class, controller);
			IStrategoTerm textreplace = construct_textual_change_4_0.instance.invoke(
					observer.getRuntime().getCompiledContext(), 
					resultTuple, 
					createStrategy(RefactoringFactory.getPPStrategy(descriptor), file, observer),
					createStrategy(RefactoringFactory.getParenthesizeStrategy(descriptor), file, observer),
					createStrategy(RefactoringFactory.getOverrideReconstructionStrategy(descriptor), file, observer),
					createStrategy(RefactoringFactory.getResugarStrategy(descriptor), file, observer)
				);
			final String result = ((IStrategoString) textreplace.getSubterm(0).getSubterm(2)).stringValue();
			
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					controller.getEditor().getDocument().set(result);
				}
			});
		} catch (BadDescriptorException e) {
			e.printStackTrace();
		}
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
}
