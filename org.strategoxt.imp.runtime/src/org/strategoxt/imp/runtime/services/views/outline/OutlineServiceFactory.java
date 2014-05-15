package org.strategoxt.imp.runtime.services.views.outline;

import static org.strategoxt.imp.runtime.dynamicloading.TermReader.findTerm;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.termContents;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.termAt;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.cons;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.AbstractServiceFactory;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

/**
 * @author Oskar van Rest
 */
public class OutlineServiceFactory extends AbstractServiceFactory<IOutlineService> {

	private static final int DEFAULT_EXPAND_TO_LEVEL = 3;
	
	public OutlineServiceFactory() {
		super(IOutlineService.class, false);
	}
	
	@Override
	public IOutlineService create(Descriptor descriptor, SGLRParseController controller) throws BadDescriptorException {
		EditorState editorState = EditorState.getEditorFor(controller);
		
		IStrategoTerm outliner = findTerm(descriptor.getDocument(), "OutlineView");
		
		// BEGIN backwards compatibility
		if (outliner == null) {
			return new OutlineService("outline", true, false, DEFAULT_EXPAND_TO_LEVEL, editorState);
		}
		// END backwards compatibility
		
		String outlineRule = termContents(termAt(outliner, 0));
		
		IStrategoList options = termAt(outliner, 1);
		boolean source = false;
		boolean onselection = false;

		for (IStrategoTerm option : options.getAllSubterms()) {
			String type = cons(option);
			if (type.equals("Source")) {
				source = true;
			} else if (type.equals("OnSelection")) {
				onselection = true;
			} else {
				throw new BadDescriptorException("Unknown builder annotation: " + type);
			}
		}
		
		String expandToLevelS = termContents(termAt(outliner, 2));
		int expandToLevel = DEFAULT_EXPAND_TO_LEVEL;
		if (expandToLevelS != null) {
			expandToLevel = Integer.parseInt(expandToLevelS);
		}
		
		return new OutlineService(outlineRule, source, onselection, expandToLevel, editorState);
	}
}
