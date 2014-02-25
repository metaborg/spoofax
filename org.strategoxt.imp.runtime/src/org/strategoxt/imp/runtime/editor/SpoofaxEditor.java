package org.strategoxt.imp.runtime.editor;

import java.util.ResourceBundle;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.strategoxt.imp.runtime.services.views.outline.SpoofaxOutlinePage;

/**
 * TODO: subclass {@link TextEditor} and delegate to {@link UniversalEditor}.
 * 
 * @author Oskar van Rest
 */
public class SpoofaxEditor extends UniversalEditor {
	
	public static final String EDITOR_ID = "org.eclipse.imp.runtime.editor.spoofaxEditor";
	
	public SpoofaxEditor() {
		setSourceViewerConfiguration(new SpoofaxSourceViewerConfiguration(new StructuredSourceViewerConfiguration()));
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		
		// when opening the editor for a file with an unknown extension, let IMP handle it.
		if (getParseController() == null) {
			return super.getAdapter(adapter);
		}
		
		if (adapter.equals(IContentOutlinePage.class)) {
			return new SpoofaxOutlinePage(getParseController());
		}

		return super.getAdapter(adapter);
	}
	
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		final SpoofaxEditor spoofaxEditor = this;
		final ISelectionProvider textSelectionProvider = getSite().getSelectionProvider();
		final ISelectionProvider strategoTermSelectionProvider = new org.strategoxt.imp.runtime.editor.SelectionProvider();
		getSite().setSelectionProvider(strategoTermSelectionProvider);
		
		textSelectionProvider.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ITextSelection textSelection = (ITextSelection) event.getSelection();
				ISelection strategoTermSelection = new StrategoTermSelection(spoofaxEditor, textSelection.getOffset(), textSelection.getLength());
				strategoTermSelectionProvider.setSelection(strategoTermSelection); // generate new StrategoTermSelection when TextSelection changes
			}
		});

		((StyledText) this.getAdapter(Control.class)).addCaretListener(new CaretListener() {

			@Override
			public void caretMoved(CaretEvent event) {
				ITextSelection textSelection = (ITextSelection) textSelectionProvider.getSelection();
				int offset = textSelection.getLength() == 0 ? event.caretOffset : textSelection.getOffset();
				ISelection strategoTermSelection = new StrategoTermSelection(spoofaxEditor, offset, textSelection.getLength());
				strategoTermSelectionProvider.setSelection(strategoTermSelection); // generate new StrategoTermSelection when cursor position changes (because TextSelection doesn't change when selection stays empty).
			}
		});
	}
	
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
		if (getParseController() == null) {
			return super.createSourceViewer(parent, ruler, styles);
		}
		
		ISourceViewer viewer = new SpoofaxViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles, getParseController());
		getSourceViewerDecorationSupport(viewer); // ensure decoration support has been created and configured.
		return viewer;
	}
}
