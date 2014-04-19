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
	
	private SpoofaxViewer spoofaxViewer;
	private ISelectionProvider selectionProvider = new SelectionProvider();
	
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
	public ISelectionProvider getSelectionProvider() {
		return selectionProvider;
	}
	
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		// temporary workaround for Spoofax/812
		EditorState editorState = EditorState.getEditorFor(this);
		if (editorState != null) {
			try {
				PropertiesService propertiesService = editorState.getDescriptor().createService(PropertiesService.class, editorState.getParseController());
				if (propertiesService.getPropertiesRule() == null) {
					spoofaxViewer.getSelectionProvider().addSelectionChangedListener(new ISelectionChangedListener() {

						@Override
						public void selectionChanged(SelectionChangedEvent event) {
							((SelectionProvider) selectionProvider).setSelection2((ITextSelection) spoofaxViewer.getSelectionProvider().getSelection());
						}
					});
					return;
				}
			} catch (BadDescriptorException e) {
				e.printStackTrace();
			}
		}
		
		spoofaxViewer.getSelectionProvider().addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ITextSelection textSelection = (ITextSelection) event.getSelection();
				updateSelection(textSelection.getOffset(), textSelection.getLength()); // generate new StrategoTermSelection when TextSelection changes
			}
		});
		
		((StyledText) this.getAdapter(Control.class)).addCaretListener(new CaretListener() {

			@Override
			public void caretMoved(CaretEvent event) {
				ITextSelection textSelection = (ITextSelection) spoofaxViewer.getSelectionProvider().getSelection();
				int offset = textSelection.getLength() == 0 ? event.caretOffset : textSelection.getOffset();
				updateSelection(offset, textSelection.getLength()); // generate new StrategoTermSelection when cursor position changes
			}
		});
	}
	
	public void updateSelection(final int offset, final int length) {
		final SpoofaxEditor spoofaxEditor = this;
		final Display display = Display.getCurrent();

		Job job = new Job("Updating properties view") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				final StrategoTermSelection selection = new StrategoTermSelection(spoofaxEditor, offset, length);
				selection.getFirstElement(); // do the heavy work here and not in the UI thread, or the UI will block
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						selectionProvider.setSelection(selection);
					}
				});
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
		if (getParseController() == null) {
			return super.createSourceViewer(parent, ruler, styles);
		}
		
		spoofaxViewer = new SpoofaxViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles, getParseController());
		getSourceViewerDecorationSupport(spoofaxViewer); // ensure decoration support has been created and configured.
		return spoofaxViewer;
	}
	
	protected class SelectionProvider implements IPostSelectionProvider, ISelectionValidator {
		StrategoTermSelection selection;
		
		List<ISelectionChangedListener> listeners = new CopyOnWriteArrayList<ISelectionChangedListener>();
		
		public void addSelectionChangedListener(ISelectionChangedListener listener) {
			listeners.add(listener);
		}

		public ISelection getSelection() {
			return selection != null? selection : 
				spoofaxViewer.getSelectionProvider().getSelection(); // temporary workaround for Spoofax/812
		}

		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			listeners.remove(listener);
		}

		public void setSelection(ISelection selection) {
			if (selection instanceof StrategoTermSelection) {
				this.selection = (StrategoTermSelection) selection;

				SelectionChangedEvent e = new SelectionChangedEvent(this, selection);
				for (ISelectionChangedListener listener : listeners) {
					listener.selectionChanged(e);
				}
				
				spoofaxViewer.firePostSelectionChanged(this.selection);
			}
			else {
				doSetSelection(selection);
			}
		}
		
		// temporary workaround for Spoofax/812
		public void setSelection2(ITextSelection selection) {
		 	SelectionChangedEvent e = new SelectionChangedEvent(this, selection);
		 	for (ISelectionChangedListener listener : listeners) {
		 		listener.selectionChanged(e);
		 	}
		 	spoofaxViewer.firePostSelectionChanged(selection.getStartLine(), selection.getEndLine());
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
