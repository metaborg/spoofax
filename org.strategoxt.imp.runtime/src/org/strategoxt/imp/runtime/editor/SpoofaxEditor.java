package org.strategoxt.imp.runtime.editor;

import java.util.ResourceBundle;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySource;
import org.strategoxt.imp.runtime.services.views.outline.SpoofaxOutlinePage;

/**
 * TODO: subclass {@link TextEditor} and delegate to {@link UniversalEditor}.
 * 
 * @author Oskar van Rest
 */
public class SpoofaxEditor extends UniversalEditor {
	
	@SuppressWarnings("hiding")
	public static final String EDITOR_ID = "org.eclipse.imp.runtime.editor.spoofaxEditor";
	
	public SpoofaxEditor() {
		setSourceViewerConfiguration(new SpoofaxSourceViewerConfiguration(new StructuredSourceViewerConfiguration()));
	}
	
	@Override
	public Object getAdapter(Class adapter) {
		
		// when opening the editor for a file with an unknown extension, let IMP handle it.
		if (getParseController() == null) {
			return super.getAdapter(adapter);
		}
		
		if (adapter.equals(IContentOutlinePage.class)) {
			return new SpoofaxOutlinePage(getParseController());
		}

		if (adapter == IPropertySource.class) {
			// TODO: Properties View
		}

		return super.getAdapter(adapter);
	}
	
	/**
	 * TODO: decouple from IMP.
	 */
	@Override
	protected void createActions() {
		final ResourceBundle bundle = ResourceBundle.getBundle(MESSAGE_BUNDLE);
		
		Action action= new TextOperationAction(bundle, "ShowOutline.", this, SpoofaxViewer.SHOW_OUTLINE);
		action.setActionDefinitionId(SHOW_OUTLINE_COMMAND);
		setAction(SHOW_OUTLINE_COMMAND, action);
		
		action= new TextOperationAction(bundle, "ToggleComment.", this, SpoofaxViewer.TOGGLE_COMMENT);
		action.setActionDefinitionId(TOGGLE_COMMENT_COMMAND);
		setAction(TOGGLE_COMMENT_COMMAND, action);
		
		action= new TextOperationAction(bundle, "IndentSelection.", this, SpoofaxViewer.INDENT_SELECTION);
		action.setActionDefinitionId(INDENT_SELECTION_COMMAND);
		setAction(INDENT_SELECTION_COMMAND, action);
		
		super.createActions();
	}
	
	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		ISourceViewer viewer = new SpoofaxViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles, getParseController());
		getSourceViewerDecorationSupport(viewer); // ensure decoration support has been created and configured.
		return viewer;
	}
}
