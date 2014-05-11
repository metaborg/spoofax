package org.strategoxt.imp.runtime.services.views.outline;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.services.views.FilteringInfoPopup;
import org.strategoxt.imp.runtime.services.views.StrategoLabelProvider;
import org.strategoxt.imp.runtime.services.views.StrategoTreeContentProvider;

public class SpoofaxOutlinePopup extends FilteringInfoPopup {

	private static int WIDTH = 400;
	private static int HEIGHT = 322;
	
	private final IParseController parseController;
	private final StrategoTreeContentProvider contentProvider;
	private final StrategoLabelProvider labelProvider;
	private final Composite editorComposite;

	public SpoofaxOutlinePopup(Shell parent, int shellStyle, int treeStyle, IParseController parseController, Composite editorComposite) {
		super(parent, shellStyle, treeStyle);
		this.parseController = parseController;
		this.editorComposite = editorComposite;
		
		contentProvider = new StrategoTreeContentProvider();
		String pluginPath = EditorState.getEditorFor(parseController).getDescriptor().getBasePath().toOSString();
		labelProvider = new StrategoLabelProvider(pluginPath);
	}

	@Override
	protected TreeViewer createTreeViewer(Composite parent, int style) {
		TreeViewer treeViewer = new TreeViewer(parent, style);
		treeViewer.setContentProvider(contentProvider);
		treeViewer.setLabelProvider(labelProvider);
		treeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);

		EditorState editorState = new EditorState(EditorState.getEditorFor(parseController).getEditor()); // create new editorState to reload descriptor
		IOutlineService outlineService = null;
		try {
			outlineService = editorState.getDescriptor().createService(IOutlineService.class, editorState.getParseController());
		} catch (BadDescriptorException e) {
			e.printStackTrace();
		}
		IStrategoTerm outline = outlineService.getOutline(editorState);
		
		// workaround for https://bugs.eclipse.org/9262
		if (outline.getTermType() == IStrategoTerm.APPL) {
			outline = SpoofaxOutlineUtil.factory.makeList(outline);
		}
		
		treeViewer.setInput(outline);
		return treeViewer;
	}

	@Override
	protected String getId() {
		return getClass().toString();
	}

	@Override
	protected void handleElementSelected(Object selectedElement) {
		if (selectedElement != null) {
			SpoofaxOutlineUtil.selectCorrespondingText(selectedElement, EditorState.getEditorFor(parseController));
			setMatcherString("", false);
		}
	}

	@Override
	public void setInput(Object input) {
		getTreeViewer().setInput(input);
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(WIDTH, HEIGHT);
	}
	
	/**
	 * See comments at {@link FilteringInfoPopup#setLocation(Point)}.
	 */
	@Override
	protected Point getInitialLocation(Point initialSize) {
		Point location = editorComposite.toDisplay(0, 0);
		Point editorSize = editorComposite.getSize();
		return new Point(location.x + editorSize.x/2 - initialSize.x/2, location.y + editorSize.y/2 - initialSize.y/2);
	}
}
