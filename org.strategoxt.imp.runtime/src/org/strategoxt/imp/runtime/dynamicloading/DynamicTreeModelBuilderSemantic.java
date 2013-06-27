package org.strategoxt.imp.runtime.dynamicloading;

import org.eclipse.imp.editor.ModelTreeNode;
import org.eclipse.imp.services.base.TreeModelBuilderBase;

/**
 * @author Oskar van Rest
 */
public class DynamicTreeModelBuilderSemantic extends TreeModelBuilderBase {

	private final DynamicTreeModelBuilder dynamicTreeModelBuilder = new DynamicTreeModelBuilder();

	@Override
	public ModelTreeNode buildTree(Object root) {
		return dynamicTreeModelBuilder.buildTree(root);
	}
	
	@Override
	protected void visitTree(Object root) {
		dynamicTreeModelBuilder.visitTree(root);		
	}
}