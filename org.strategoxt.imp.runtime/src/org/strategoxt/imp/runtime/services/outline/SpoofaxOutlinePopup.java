package org.strategoxt.imp.runtime.services.outline;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.strategoxt.imp.runtime.EditorState;

public class SpoofaxOutlinePopup extends FilteringInfoPopup {

	private IParseController parseController;

	public SpoofaxOutlinePopup(Shell parent, int shellStyle, int treeStyle, IParseController parseController) {
		super(parent, shellStyle, treeStyle);
		this.parseController = parseController;
	}

	@Override
	protected TreeViewer createTreeViewer(Composite parent, int style) {
		TreeViewer treeViewer = new TreeViewer(parent, style);
		treeViewer.setContentProvider(new SpoofaxOutlineContentProvider());
		
		// Because the super constructor calls this method, the parse controller will be null the first time.
		// We therefore create a fake plugin path. Note: we need to pass a pluginPath to the label provider, because
		// it is not able to figure out the plugin path itself. We cannot use origin or imploder attachments because
		// outline nodes or labels do not necessarily have such attachments.
		String pluginPath = parseController == null? "" : EditorState.getEditorFor(parseController).getDescriptor().getBasePath().toOSString();
		treeViewer.setLabelProvider(new SpoofaxOutlineLabelProvider(pluginPath));
		treeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
		return treeViewer;
	}

	@Override
	protected String getId() {
		return null; // TODO
	}

	@Override
	protected void handleElementSelected(Object selectedElement) {
		if (selectedElement != null) {
			SpoofaxOutlineUtil.selectCorrespondingText(selectedElement, parseController);
		}
	}

	@Override
	public void setInput(Object input) {
		getTreeViewer().setInput(input);
	}
}
