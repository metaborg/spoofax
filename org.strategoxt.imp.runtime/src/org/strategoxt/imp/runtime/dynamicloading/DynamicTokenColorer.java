package org.strategoxt.imp.runtime.dynamicloading;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.ITokenColorer;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.strategoxt.imp.runtime.EditorState;

/**
 * Dynamic proxy class to a token colorer.
 * 
 * @see AbstractService
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicTokenColorer extends AbstractService<ITokenColorer> implements ITokenColorer {
	
	private static final int GRAY_COMPONENT = 96;
	
	private IParseController lastParseController;
	
	private Color gray;
	
	private volatile boolean isReinitializing;

	public DynamicTokenColorer() {
		super(ITokenColorer.class);
	}
	
	public IRegion calculateDamageExtent(IRegion seed, IParseController controller) {
		if (!isInitialized()) return seed;
		
		return getWrapped().calculateDamageExtent(seed, controller);
	}

	public TextAttribute getColoring(IParseController controller, Object token) {
		if (!isInitialized()) initialize(controller.getLanguage());
		lastParseController = controller;
		TextAttribute result = getWrapped().getColoring(controller, token);
		if (isReinitializing) result = toGray(result);
		return result;
	}
	
	@Override
	public void prepareForReinitialize() {
		isReinitializing = true;
		UniversalEditor lastEditor = null;
		if (lastParseController instanceof DynamicParseController) {
			EditorState editorState = ((DynamicParseController) lastParseController).getLastEditor();
			if (editorState != null) lastEditor = editorState.getEditor();
		}

		if (lastEditor != null && !lastEditor.getTitleImage().isDisposed())
			lastEditor.updateColoring(new Region(0, lastEditor.getServiceControllerManager().getSourceViewer().getDocument().getLength()));
	}
	
	@Override
	public void reinitialize(Descriptor newDescriptor) throws BadDescriptorException {
		super.reinitialize(newDescriptor);
		isReinitializing = false;
	}

	private TextAttribute toGray(TextAttribute attribute) {
		return attribute == null
				? new TextAttribute(getGrayColor())
				: new TextAttribute(getGrayColor(), attribute.getBackground(), attribute.getStyle(), attribute.getFont());
	}
	
	private Color getGrayColor() {
		if (gray == null)
			gray = new Color(Display.getCurrent(), GRAY_COMPONENT, GRAY_COMPONENT, GRAY_COMPONENT);
		return gray;
	}
}
