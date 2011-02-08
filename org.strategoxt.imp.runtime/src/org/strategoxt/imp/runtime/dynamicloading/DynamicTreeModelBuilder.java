package org.strategoxt.imp.runtime.dynamicloading;

import static org.strategoxt.imp.runtime.stratego.SourceAttachment.*;

import org.eclipse.imp.editor.ModelTreeNode;
import org.eclipse.imp.services.base.TreeModelBuilderBase;
import org.strategoxt.imp.runtime.parser.ast.AstNodeLocator;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicTreeModelBuilder extends TreeModelBuilderBase implements IDynamicLanguageService {

	private final DynamicService service = new DynamicService();

	@Override
	public ModelTreeNode buildTree(Object root) {
		if (root == null) return super.buildTree(root); // HACK
		service.initialize(getParseController(AstNodeLocator.impObjectToAstNode(root)));
		return service.getWrapped().buildTree(root);
	}
	
	@Override
	protected void visitTree(Object root) {
		if (root != null)
			throw new IllegalStateException("Method call not expected");		
	}

	public void prepareForReinitialize() {
		service.prepareForReinitialize();
	}

	public void reinitialize(Descriptor newDescriptor) throws BadDescriptorException {
		service.reinitialize(newDescriptor);
	}
	
	/**
	 * The dynamic wrapper class that houses the implementation of this service.
	 * (Since TreeModelBuilderBase burned our base class, we use an inner class for this.)
	 * 
	 * @author Lennart Kats <lennart add lclnet.nl>
	 */
	private static class DynamicService extends AbstractService<TreeModelBuilderBase> {

		public DynamicService() {
			super(TreeModelBuilderBase.class);
		}
	}

}