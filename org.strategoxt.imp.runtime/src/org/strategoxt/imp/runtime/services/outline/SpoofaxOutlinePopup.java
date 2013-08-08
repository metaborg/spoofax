package org.strategoxt.imp.runtime.services.outline;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class SpoofaxOutlinePopup extends FilteringInfoPopup {

	public SpoofaxOutlinePopup(Shell parent, int shellStyle, int treeStyle) {
		super(parent, shellStyle, treeStyle);
	}

	@Override
	protected TreeViewer createTreeViewer(Composite parent, int style) {
		TreeViewer treeViewer = new TreeViewer(parent, style);
		treeViewer.setContentProvider(new SpoofaxOutlineContentProvider());
		treeViewer.setLabelProvider(new SpoofaxOutlineLabelProvider());
		treeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
		return treeViewer;
	}

	@Override
	protected String getId() {
		// TODO
		return null;
	}

	@Override
	protected void handleElementSelected(Object selectedElement) {
		System.out.println(selectedElement.getClass());
	}

	@Override
	public void setInput(Object input) {
		getTreeViewer().setInput(input);
	}
}
