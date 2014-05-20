package org.strategoxt.imp.runtime.services.views.outline;

import java.util.LinkedList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.services.StrategoObserver;
import org.strategoxt.imp.runtime.services.views.StrategoLabelProvider;
import org.strategoxt.imp.runtime.services.views.StrategoTreeContentProvider;
import org.strategoxt.imp.runtime.stratego.StrategoTermPath;

/**
 * @author Oskar van Rest
 */
public class SpoofaxOutlinePage extends ContentOutlinePage implements IModelListener {

	private final EditorState editorState;
	private boolean debounceOutlineSelection;
	private boolean debounceTextSelection;
	private IStrategoTerm outline;

	public SpoofaxOutlinePage(EditorState editorState) {
		this.editorState = editorState;
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		getTreeViewer().setContentProvider(new StrategoTreeContentProvider());
		String pluginPath = editorState.getDescriptor().getBasePath().toOSString();
		getTreeViewer().setLabelProvider(new StrategoLabelProvider(pluginPath));
		
		registerToolbarActions();
		
		editorState.getEditor().addModelListener(this);
		editorState.getEditor().getSelectionProvider().addSelectionChangedListener(this);
		
		if (editorState.getParseController().getCurrentAst() != null) {
			// The editor sporadically manages to parse the file before our model listener gets added,
			// resulting in an empty outline on startup. We therefore perform a 'manual' update:
			if (!getOnselection()) {
				update();
			}
		}
	}

	private void registerToolbarActions() {
		IPageSite site = getSite();
		IActionBars actionBars= site.getActionBars();
		IToolBarManager toolBarManager= actionBars.getToolBarManager();
		
		toolBarManager.add(new LexicalSortingAction(getTreeViewer()));

		// TODO: Collapse All
		
		// TODO: Expand All?
	}

	public void update(IParseController arg0, IProgressMonitor arg1) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				if (!getOnselection()) {
					update();
				}
			}
		});
		
	}
	
	public AnalysisRequired getAnalysisRequired() {
		return AnalysisRequired.NONE;
	}
		
	@Override
	public void dispose() {
		editorState.getEditor().removeModelListener(this);
		editorState.getEditor().getSelectionProvider().addSelectionChangedListener(this);
	}
	
	public void update() {
		final Display display = Display.getCurrent();

		Job job = new Job("Updating outline view") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {		

				outline = getOutline();
				
				if (outline == null) {
					outline = SpoofaxOutlineUtil.factory.makeList();
				}
				// workaround for https://bugs.eclipse.org/9262
				if (outline.getTermType() == IStrategoTerm.APPL) {
					outline = SpoofaxOutlineUtil.factory.makeList(outline);
				}
				
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						if (getTreeViewer().getControl() != null && !getTreeViewer().getControl().isDisposed()) {
							getTreeViewer().setInput(outline);
							getTreeViewer().expandToLevel(getExpandToLevel());
						}
					}
				});
				return Status.OK_STATUS;
			}
		};

		job.schedule();
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
    	if (event.getSource() == getTreeViewer()) {
        	super.selectionChanged(event);
    	}
    	    	
    	if (event.getSource() == getTreeViewer()) {
        	if (debounceOutlineSelection) {
        		return;
        	}
        	debounceTextSelection = true;
    		
        	outlineSelectionToTextSelection();
        	new Thread(new Runnable() {
    			
    			@Override
    			public void run() {
    				try {
    					// this is rather ugly but the problem is that new text selections are generated asynchronously and we don't know how many
    					Thread.sleep(2000);
    				} catch (InterruptedException e) {
    					e.printStackTrace();
    				}
    				debounceTextSelection = false;
    			}
    		}).start();
       	}
       	else {
        	if (debounceTextSelection || debounceOutlineSelection) {
        		return;
        	}

        	if (getOnselection()) {
        		update();
        	}
        	else {
            	debounceOutlineSelection = true;
        		textSelectionToOutlineSelection();
        	}
       	}
    }
    
    public void outlineSelectionToTextSelection() {
    	TreeSelection treeSelection = (TreeSelection) getSelection();
    	if (treeSelection.isEmpty()) {
    		return;
    	}

    	Object[] selectedElems = treeSelection.toArray();
    	SpoofaxOutlineUtil.selectCorrespondingText(selectedElems, editorState);
    }
    
    public void textSelectionToOutlineSelection() {
    	if (outline == null) {
    		debounceOutlineSelection = false;
    		return;
    	}
    	
    	final IStrategoTerm textSelection = editorState.getSelectionAst(false);
		final Display display = Display.getCurrent();

		Job job = new Job("Updating outline view selection") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				ISelection newSelection = new TreeSelection(new TreePath[0]);
				
		    	if (textSelection != null) {
			    	IStrategoList path = null;
			    	
			    	StrategoObserver observer = SpoofaxOutlineUtil.getObserver(editorState);
			    	observer.getLock().lock();
			    	try {
				    	path = StrategoTermPath.getTermPathWithOrigin(observer.getRuntime().getCompiledContext(), (IStrategoTerm) outline, textSelection);
			    	}
			    	finally {
			    		observer.getLock().unlock();
			    	}
			    	
			    	if (path != null) {
				    	TreePath[] treePaths = termPathToTreePaths(path);
				    	newSelection = new TreeSelection(treePaths);
			    	}
		    	}
				
		    	final ISelection result = newSelection;
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						setSelection(result);
						debounceOutlineSelection = false;
					}
				});
				return Status.OK_STATUS;
			}
		};

		job.schedule();
    }
    
    private TreePath[] termPathToTreePaths(IStrategoList path) {
    	return termPathToTreePaths(path, (IStrategoTerm) outline, new LinkedList<IStrategoTerm>());
	}
    
    private TreePath[] termPathToTreePaths(IStrategoList path, IStrategoTerm current, LinkedList<IStrategoTerm> segments) {

    	if (current.getTermType()==IStrategoTerm.APPL) {
    		segments.add(current);
		}
    	
    	if (path.isEmpty()) {
    		if (current.getTermType()==IStrategoTerm.APPL || current.getTermType()==IStrategoTerm.STRING) {
    			TreePath[] result = new TreePath[1];
    			result[0] = new TreePath(segments.toArray());
    			return result;
    		}
    		else {
    			IStrategoTerm[] leaves = current.getAllSubterms();
    			TreePath[] result = new TreePath[leaves.length];
    			for (int i=0; i<leaves.length; i++) {
    				segments.add(leaves[i]);
    				result[i] = new TreePath(segments.toArray());
    				segments.removeLast();
    			}
    			return result;
    		}
    	}

		current = current.getSubterm(((IStrategoInt) path.head()).intValue());
		path = path.tail();
    	
		return termPathToTreePaths(path, current, segments);
    }

	@Override
	public void setFocus() {
    	super.setFocus();
    	outlineSelectionToTextSelection();
    }
	
	private boolean getOnselection() {
		EditorState editorState = new EditorState(this.editorState.getEditor()); // create new editorState to reload descriptor
		IOutlineService outlineService = getOutlineService(editorState);
		return outlineService != null && outlineService.getOnselection();
	}
	
	private int getExpandToLevel() {
		EditorState editorState = new EditorState(this.editorState.getEditor()); // create new editorState to reload descriptor
		IOutlineService outlineService = getOutlineService(editorState);
		return outlineService == null ? 0 : outlineService.getExpandToLevel();
	}
	
	private IStrategoTerm getOutline() {
		EditorState editorState = new EditorState(this.editorState.getEditor()); // create new editorState to reload descriptor
		IOutlineService outlineService = getOutlineService(editorState);
		return outlineService == null ? null : outlineService.getOutline(editorState);
	}
	
	private IOutlineService getOutlineService(EditorState editorState) {
		try {
			return editorState.getDescriptor().createService(IOutlineService.class, editorState.getParseController());
		} catch (BadDescriptorException e) {
			e.printStackTrace();
		}
		return null;
	}
}
