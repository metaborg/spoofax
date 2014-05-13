package org.strategoxt.imp.runtime.editor;

import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ISelectionValidator;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.services.views.outline.SpoofaxOutlinePage;
import org.strategoxt.imp.runtime.services.views.properties.PropertiesService;

/**
 * TODO: subclass {@link TextEditor} and delegate to {@link UniversalEditor}.
 * 
 * @author Oskar van Rest
 */
public class SpoofaxEditor extends UniversalEditor {
	
	public static final String EDITOR_ID = "org.eclipse.imp.runtime.editor.spoofaxEditor";
	private EditorState editorState;
	
	private SpoofaxViewer spoofaxViewer;
	private SelectionProvider selectionProvider = new SelectionProvider();
	
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
			return new SpoofaxOutlinePage(editorState);
		}

		return super.getAdapter(adapter);
	}
	
	@Override
	public ISelectionProvider getSelectionProvider() {
		return selectionProvider;
	}
	
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		editorState = EditorState.getEditorFor(this);
		
		spoofaxViewer.getSelectionProvider().addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ITextSelection textSelection = (ITextSelection) event.getSelection();
				updateSelection(textSelection.getOffset(), textSelection.getLength()); // generate new selection upon new TextSelection
			}
		});
		
		((StyledText) this.getAdapter(Control.class)).addCaretListener(new CaretListener() {

			@Override
			public void caretMoved(CaretEvent event) {
				ITextSelection textSelection = (ITextSelection) spoofaxViewer.getSelectionProvider().getSelection();
				int offset = textSelection.getLength() == 0 ? event.caretOffset : textSelection.getOffset();
				updateSelection(offset, textSelection.getLength()); // generate new selection when cursor position changes (non-default behavior for Eclipse Text Editors)
			}
		});
	}
	
	private boolean shouldCreatePropertiesView;
	private boolean shouldCreatePropertiesView() {
		if (editorState == null) {
			return false;
		}
			
		Display.getDefault().syncExec(new Runnable() {
		    @Override
		    public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window == null || window.getActivePage() == null || window.getActivePage().findView("org.eclipse.ui.views.PropertySheet") == null) {
					shouldCreatePropertiesView = false;
				}
				else {
					shouldCreatePropertiesView = true;
				}
		    }
		});
		if (shouldCreatePropertiesView == false) {
			return false;
		}
		
		try {
			editorState = new EditorState(this.editorState.getEditor()); // create new editorState to reload descriptor
			PropertiesService propertiesService = editorState.getDescriptor().createService(PropertiesService.class, editorState.getParseController());
			if (propertiesService.getPropertiesRule() == null) {
				return false;
			}
		} catch (BadDescriptorException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	public void updateSelection(final int offset, final int length) {
		
		if (editorState == null) {
			return; // language undefined
		}
		
		final Display display = Display.getCurrent();

		Job job = new Job("Updating properties view") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (shouldCreatePropertiesView()) {
					final StrategoTermSelection selection = new StrategoTermSelection(editorState, offset, length);
					selection.getFirstElement(); // do the heavy work here and not in the UI thread, or the UI will block
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							selectionProvider.setSelectionDontReveal(selection);
						}
					});
				}
				else {
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							selectionProvider.setSelectionDontReveal(new TextSelection(offset, length));
						}
					});
				}
				
				return Status.OK_STATUS;
			}
		};

		job.schedule();
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
		spoofaxViewer = new SpoofaxViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles, getParseController());
		getSourceViewerDecorationSupport(spoofaxViewer); // ensure decoration support has been created and configured.
		return spoofaxViewer;
	}
	
	protected class SelectionProvider implements IPostSelectionProvider, ISelectionValidator {
		List<ISelectionChangedListener> listeners = new CopyOnWriteArrayList<ISelectionChangedListener>();
		
		public void addSelectionChangedListener(ISelectionChangedListener listener) {
			listeners.add(listener);
		}

		public ISelection getSelection() {
			return spoofaxViewer.getSelectionProvider().getSelection(); // always return synchronized selection and not the asynchronous StrategoTermSelection
		}

		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			listeners.remove(listener);
		}

		public void setSelection(ISelection selection) {
				doSetSelection(selection);
		}
		
		private void setSelectionDontReveal(ISelection selection) {
			SelectionChangedEvent e = new SelectionChangedEvent(this, selection);
			for (ISelectionChangedListener listener : listeners) {
				listener.selectionChanged(e);
			}
			
			if (selection instanceof StrategoTermSelection) {
				spoofaxViewer.firePostSelectionChanged(selection);
			}
			else if (selection instanceof ITextSelection){
				ITextSelection textSelection = (ITextSelection) selection;
			 	spoofaxViewer.firePostSelectionChanged(textSelection.getStartLine(), textSelection.getEndLine());
			}
		}
		
		public void addPostSelectionChangedListener(ISelectionChangedListener listener) {
			if (spoofaxViewer != null) {
				if (spoofaxViewer.getSelectionProvider() instanceof IPostSelectionProvider)  {
					IPostSelectionProvider provider= (IPostSelectionProvider) spoofaxViewer.getSelectionProvider();
					provider.addPostSelectionChangedListener(listener);
				}
			}
		}

		public void removePostSelectionChangedListener(ISelectionChangedListener listener) {
			if (spoofaxViewer != null)  {
				if (spoofaxViewer.getSelectionProvider() instanceof IPostSelectionProvider)  {
					IPostSelectionProvider provider= (IPostSelectionProvider) spoofaxViewer.getSelectionProvider();
					provider.removePostSelectionChangedListener(listener);
				}
			}
		}

		public boolean isValid(ISelection postSelection) {
			return true;
		}
	}
}
