package org.strategoxt.imp.runtime.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.services.views.properties.IPropertiesService;

/**
 * @author Oskar van Rest
 * 
 * Why implement IStructuredSelection? Because the properties view only takes IStructuredSelections.
 */
public class StrategoTermSelection extends TextSelection implements IStructuredSelection {

	private final EditorState editorState;

	public StrategoTermSelection(EditorState editorState, int offset, int length) {
		super(offset, length);
		this.editorState = editorState;
	}

	public IStrategoTerm getSelectionAst() {
		if (editorState != null) {
			try {
				return SelectionUtil.getSelectionAst(getOffset(), getLength(), false, editorState.getParseController());
			}
			catch (IndexOutOfBoundsException e) {
				// certain edits (e.g. undoing a change) result in the generation of a new textual selection before the text is parsed and a new AST is generated.
				// trying to obtain an AST selection in the old AST using the new selection offset and selection length may fail.
			}
		}
		return null;
	}
	
	public IStrategoTerm getSelectionAstAnalyzed() {
		if (editorState != null) {
			return SelectionUtil.getSelectionAstAnalyzed(getOffset(), getLength(), false, editorState.getParseController());
		}
		return null;
	}
	
	private IStrategoTerm properties;
	
	/**
	 * Return the properties for the properties view.
	 */
	@Override // IStructuredSelection (properties view)
	public IStrategoTerm getFirstElement() {
		if (properties == null) {
			
			if (editorState == null) {
				return null;
			}
			
			IPropertiesService propertiesService = null;
			try {
				propertiesService = editorState.getDescriptor().createService(IPropertiesService.class, editorState.getParseController());
			} catch (BadDescriptorException e) {
				e.printStackTrace();
			}
			properties = propertiesService.getProperties(getOffset(), getLength());
		}
		
		return properties;
	}

	@Override // IStructuredSelection (properties view)
	public Iterator<IStrategoTerm> iterator() {
		return toList().iterator();
	}

	@Override // IStructuredSelection (properties view)
	public int size() {
		return toList().size();
	}

	@Override // IStructuredSelection (properties view)
	public Object[] toArray() {
		return toList().toArray();
	}

	@Override // IStructuredSelection (properties view)
	public List<IStrategoTerm> toList() {
		List<IStrategoTerm> result = new ArrayList<IStrategoTerm>(1);
		if (getFirstElement() != null) {
			result.add(getFirstElement());
		}
		return result;
	}
}
