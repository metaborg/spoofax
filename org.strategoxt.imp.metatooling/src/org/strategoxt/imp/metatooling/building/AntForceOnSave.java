package org.strategoxt.imp.metatooling.building;

import java.io.FileNotFoundException;

import org.eclipse.core.runtime.Path;
import org.eclipse.imp.model.ModelFactory.ModelException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.FileState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.services.OnSaveService;

public class AntForceOnSave {
    public static void main(String[] args) throws FileNotFoundException, BadDescriptorException, ModelException {
    	for(String arg : args) {
    		FileState fileState = FileState.getFile(new Path(arg), null);
    		IStrategoTerm ast = fileState.getAnalyzedAst();
    		OnSaveService onSave = fileState.getDescriptor().createService(OnSaveService.class, fileState.getParseController());
    		onSave.invokeOnSave(ast);
    	}
    }
}
