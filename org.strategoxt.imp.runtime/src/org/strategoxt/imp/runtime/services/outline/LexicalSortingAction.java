package org.strategoxt.imp.runtime.services.outline;

import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * @author Oskar van Rest
 */
public class LexicalSortingAction extends Action {

	private final TreeViewer treeViewer;
	private final ViewerSorter viewerSorter = new ViewerSorter();

	public LexicalSortingAction(TreeViewer treeViewer) {
		super("Sort", IAction.AS_CHECK_BOX);
		this.treeViewer = treeViewer;

		setToolTipText("Sort by name");
		setDescription("Sort entries lexically by name");
		ImageDescriptor desc = RuntimePlugin.getImageDescriptor("icons/alphab_sort_co.gif");
		this.setHoverImageDescriptor(desc);
		this.setImageDescriptor(desc);
	}

	public void run() {
		treeViewer.setSorter(isChecked() ? viewerSorter : null);
	}
}
