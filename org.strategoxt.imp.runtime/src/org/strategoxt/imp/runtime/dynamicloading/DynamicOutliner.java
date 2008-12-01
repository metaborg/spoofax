package org.strategoxt.imp.runtime.dynamicloading;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IOutliner;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicOutliner extends DynamicService<IOutliner> implements IOutliner {
	
	private ITextEditor editor;
	
	private Tree tree; 

	public DynamicOutliner() {
		super(IOutliner.class);
	}

	public void createOutlinePresentation(IParseController controller, int offset) {
		initialize(controller.getLanguage());
		
		if (editor != null) getWrapped().setEditor(editor);
		if (tree != null) getWrapped().setTree(tree);
		
		getWrapped().createOutlinePresentation(controller, offset);
	}

	public void setEditor(ITextEditor editor) {
		this.editor = editor;
	}

	public void setTree(Tree tree) {
		this.tree = tree;
	}

}
