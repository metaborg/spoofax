package org.strategoxt.imp.runtime.services.outline;

import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * @author Oskar van Rest
 *
 */
public class LexicalSortingAction extends Action {
	
	private TreeViewer treeViewer;

	public LexicalSortingAction(TreeViewer treeViewer) {
		this.treeViewer = treeViewer;
		
        setText("Sort");
        setToolTipText("Sort by name");
        setDescription("Sort entries lexically by name");
        
        ImageDescriptor desc= RuntimePlugin.getImageDescriptor("icons/alphab_sort_co.gif");
        this.setHoverImageDescriptor(desc);
        this.setImageDescriptor(desc); 
	}
	
	public void run() {
		setChecked(!isChecked());
		
		if (isChecked()) {
			treeViewer.setSorter(new ViewerSorter());
		}
		else {
			treeViewer.setSorter(null);
		}
	}
}
