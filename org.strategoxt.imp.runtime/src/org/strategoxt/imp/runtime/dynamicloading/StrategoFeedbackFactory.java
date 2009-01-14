package org.strategoxt.imp.runtime.dynamicloading;

import java.io.File;
import java.io.IOException;

import org.spoofax.interpreter.core.Interpreter;
import org.spoofax.interpreter.core.InterpreterException;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.services.StrategoFeedback;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoFeedbackFactory extends AbstractServiceFactory<StrategoFeedback> {
	
	@Override
	public Class<StrategoFeedback> getCreatedType() {
		return StrategoFeedback.class;
	}
	
	@Override
	public StrategoFeedback create(Descriptor descriptor) throws BadDescriptorException {
		Interpreter interpreter;
		
		// TODO: Sharing of FeedBack instances
		
		try {
			interpreter = Environment.createInterpreter();
		} catch (Exception e) {
			Environment.logException("Could not create interpreter", e);
			return null;
		}
		
		for (File file : descriptor.getAttachedFiles()) {
			String filename = file.toString();
			if (filename.endsWith(".ctree")) {
				try {
					Debug.startTimer("Loading Stratego module ", filename);
					interpreter.load(descriptor.openAttachment(filename));
					Debug.stopTimer("Successfully loaded " +  filename);
				} catch (InterpreterException e) {
					throw new BadDescriptorException("Error loading compiler service provider " + filename, e);
				} catch (IOException e) {
					throw new BadDescriptorException("Could not load compiler service provider" + filename, e);
				}
			}
		}
		
		String observerFunction = descriptor.getProperty("SemanticObserver", null);
		
		return new StrategoFeedback(descriptor, interpreter, observerFunction);
	}

}
