package org.strategoxt.imp.metatooling.building;

import java.io.FileNotFoundException;

import org.eclipse.core.runtime.Path;
import org.eclipse.imp.model.ModelFactory.ModelException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.FileState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.services.OnSaveService;

public class AntForceOnSave {
    public static void main(String[] args) {
    	final String[] files = args[0].split(";;;");
    	for(String file : files) {
			try {
				System.out.println("Calling on-save handler for: " + file);
				FileState fileState = FileState.getFile(new Path(file), null);
				if(fileState == null) {
					Environment.logException("Could not call on-save handler. File state could not be retrieved.");
					continue;
				}
	    		IStrategoTerm ast = fileState.getAnalyzedAst();
	    		OnSaveService onSave = fileState.getDescriptor().createService(OnSaveService.class, fileState.getParseController());
	    		onSave.invokeOnSave(ast);
			} catch (FileNotFoundException e) {
				Environment.logException("Could not call on-save handler.", e);
			} catch (BadDescriptorException e) {
				Environment.logException("Could not call on-save handler.", e);
			} catch (ModelException e) {
				Environment.logException("Could not call on-save handler.", e);
			}
    	}
    }
}
