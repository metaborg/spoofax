package org.strategoxt.imp.runtime.dynamicloading;

import org.eclipse.imp.language.ILanguageService;
import org.eclipse.jface.text.IDocumentListener;
import org.strategoxt.imp.runtime.EditorState;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public interface IOnSaveService extends IDocumentListener, ILanguageService {
	
	 void initialize(EditorState editor);
}
