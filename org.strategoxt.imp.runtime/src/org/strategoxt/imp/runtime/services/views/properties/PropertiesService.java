package org.strategoxt.imp.runtime.services.views.properties;

import org.eclipse.imp.parser.IParseController;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermFactory;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.editor.SelectionUtil;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.InputTermBuilder;
import org.strategoxt.imp.runtime.services.StrategoObserver;

/**
 * @author Oskar van Rest
 */
public class PropertiesService implements IPropertiesService {
	
	private String propertiesRule;

	private boolean source;
	
	private IParseController controller;
	
	public PropertiesService(String propertiesRule, boolean source, IParseController controller) {
		this.propertiesRule = propertiesRule;
		this.source = source;
		this.controller = controller;
	}
	
	public PropertiesService(IParseController controller) {
		this.controller = controller;
	}

	@Override
	public IStrategoTerm getProperties(int selectionOffset, int selectionLength) {
		if (propertiesRule == null) {
			return new TermFactory().makeList();
		}
		
		EditorState editorState = EditorState.getEditorFor(controller);
		StrategoObserver observer = getObserver(editorState);
		observer.getLock().lock();
		try {
			if (observer.getRuntime().lookupUncifiedSVar(propertiesRule) == null) {
				Environment.logException("Rule '" + propertiesRule + "' is undefined");
				return new TermFactory().makeList();
			}
			
			if (editorState.getCurrentAst() == null) {
				return new TermFactory().makeList();
			}
			
			IStrategoTerm selectionAst = SelectionUtil.getSelectionAst(selectionOffset, selectionLength, false, (SGLRParseController) controller);
			IStrategoTerm ast = null;
			if (source) {
				ast = editorState.getCurrentAst();
			}
			else {
				try {
					ast = editorState.getCurrentAnalyzedAst() == null? editorState.getAnalyzedAst() : editorState.getCurrentAnalyzedAst();
				} catch (BadDescriptorException e) {
					e.printStackTrace();
				}
			}
			
			IStrategoTerm input = new InputTermBuilder(observer.getRuntime(), ast).makeInputTerm(selectionAst, true, source);
			IStrategoTerm properties = observer.invokeSilent(propertiesRule, input, editorState.getResource().getFullPath().toFile());
			if (properties == null) {
				observer.reportRewritingFailed();
			}
			
			return properties;
		}

		finally {
			observer.getLock().unlock();
		}
	}
	
	public static StrategoObserver getObserver(EditorState editorState) {
		try {
			return editorState.getDescriptor().createService(StrategoObserver.class, editorState.getParseController());
		}
		catch (BadDescriptorException e) {
			e.printStackTrace();
		}
		return null;
	}
}
