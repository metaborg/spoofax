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
		IStrategoTerm emptyList = new TermFactory().makeList();
		if (propertiesRule == null) {
			return emptyList;
		}

		EditorState editorState = EditorState.getEditorFor(controller);
		StrategoObserver observer = getObserver(editorState);
		observer.getLock().lock();
		try {
			if (observer.getRuntime().lookupUncifiedSVar(propertiesRule) == null) {
				Environment.logException("Rule '" + propertiesRule + "' is undefined");
				return emptyList;
			}
		}
		finally {
			observer.getLock().unlock();
		}

		if (editorState.getCurrentAst() == null) {
			return emptyList;
		}
		
		IStrategoTerm selectionAst = null;
		try {
		  selectionAst = SelectionUtil.getSelectionAst(selectionOffset, selectionLength, false, (SGLRParseController) controller);
		  if (selectionAst == null) {
			  return emptyList;
		  }
		}
		catch (IndexOutOfBoundsException e) {
			// certain edits (e.g. undoing a change) result in the generation of a new textual selection before the text is parsed and a new AST is generated.
			// trying to obtain an AST selection in the old AST using the new selection offset and selection length may fail.
			return emptyList;
		}

		observer.getLock().lock();
		try {
			selectionAst = InputTermBuilder.getMatchingAncestor(selectionAst, false);
		}
		finally {
			observer.getLock().unlock();
		}
		IStrategoTerm ast = null;
		if (source) {
			ast = editorState.getCurrentAst();
		}
		else {
			try {
				ast = editorState.getCurrentAnalyzedAst();
				if (ast == null) {
					ast = editorState.getAnalyzedAst(); // TODO Spoofax/839
				}
			} catch (BadDescriptorException e) {
				e.printStackTrace();
			}
		}
		
		IStrategoTerm properties = null;
		observer.getLock().lock();
		try {
			IStrategoTerm input = new InputTermBuilder(observer.getRuntime(), ast).makeInputTerm(selectionAst, true, source);
			properties = observer.invokeSilent(propertiesRule, input, editorState.getResource().getFullPath().toFile());
		}
		finally {
			observer.getLock().unlock();
		}
		if (properties == null) {
			observer.reportRewritingFailed();
		}
		
		return properties;
	}
	
	public String getPropertiesRule() {
		return propertiesRule;
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
