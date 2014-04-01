package org.strategoxt.imp.runtime.services.views.outline;

import java.util.LinkedList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.jface.action.IToolBarManager;
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
import org.strategoxt.imp.runtime.services.views.StrategoLabelProvider;
import org.strategoxt.imp.runtime.services.views.StrategoTreeContentProvider;
import org.strategoxt.imp.runtime.stratego.StrategoTermPath;

/**
 * @author Oskar van Rest
 */
public class SpoofaxOutlinePage extends ContentOutlinePage implements IModelListener {

	private final IParseController parseController;
	private boolean debounceSelectionChanged;
	private IStrategoTerm outline;

	public SpoofaxOutlinePage(IParseController parseController) {
		this.parseController = parseController;
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		getTreeViewer().setContentProvider(new StrategoTreeContentProvider());
		String pluginPath = EditorState.getEditorFor(parseController).getDescriptor().getBasePath().toOSString();
		getTreeViewer().setLabelProvider(new StrategoLabelProvider(pluginPath));
		
		registerToolbarActions();
		
		EditorState editorState = EditorState.getEditorFor(parseController);
		editorState.getEditor().addModelListener(this);
		editorState.getEditor().getSelectionProvider().addSelectionChangedListener(this);
		
		if (parseController.getCurrentAst() != null) {
			// The editor sporadically manages to parse the file before our model listener gets added,
			// resulting in an empty outline on startup. We therefore perform a 'manual' update:
			update();
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
		update();
	}
	
	public AnalysisRequired getAnalysisRequired() {
		return AnalysisRequired.NONE;
	}
		
	@Override
	public void dispose() {
		EditorState.getEditorFor(parseController).getEditor().removeModelListener(this);
	}
	
	public void update() {
		EditorState editorState = new EditorState(EditorState.getEditorFor(parseController).getEditor()); // create new editorState to reload descriptor
		IOutlineService outlineService = null;
		try {
			outlineService = editorState.getDescriptor().createService(IOutlineService.class, editorState.getParseController());
		} catch (BadDescriptorException e) {
			e.printStackTrace();
		}
		
		outline = outlineService.getOutline();
		final int outline_expand_to_level = outlineService.getExpandToLevel();
		
		if (outline == null) {
			outline = SpoofaxOutlineUtil.factory.makeList();
		}
		// workaround for https://bugs.eclipse.org/9262
		if (outline.getTermType() == IStrategoTerm.APPL) {
			outline = SpoofaxOutlineUtil.factory.makeList(outline);
		}
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (getTreeViewer().getControl() != null && !getTreeViewer().getControl().isDisposed()) {
					getTreeViewer().setInput(outline);
					getTreeViewer().expandToLevel(outline_expand_to_level);
				}
			}
		});
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
    	if (event.getSource() == getTreeViewer()) {
        	super.selectionChanged(event);
    	}
    	    	
    	if (debounceSelectionChanged) {
    		return;
    	}
    	
    	debounceSelectionChanged = true;
    	if (event.getSource() == getTreeViewer()) {
        	outlineSelectionToTextSelection();
       	}
       	else {
       		textSelectionToOutlineSelection();
       	}
    	debounceSelectionChanged = false;
    }
    
    public void outlineSelectionToTextSelection() {
    	TreeSelection treeSelection = (TreeSelection) getSelection();
    	if (treeSelection.isEmpty()) {
    		return;
    	}

    	Object[] selectedElems = treeSelection.toArray();
    	SpoofaxOutlineUtil.selectCorrespondingText(selectedElems, parseController);
    }
    
    public void textSelectionToOutlineSelection() {
    	if (outline == null) {
    		return;
    	}
    	
    	EditorState editorState = EditorState.getEditorFor(parseController);
    	IStrategoTerm textSelection = editorState.getSelectionAst(true);
    	
    	if (textSelection != null) {
	    	IStrategoList path = null;
	    	path = StrategoTermPath.getTermPathWithOrigin(SpoofaxOutlineUtil.getObserver(editorState), (IStrategoTerm) outline, textSelection);
	    	
	    	if (path != null) {
		    	TreePath[] treePaths = termPathToTreePaths(path);
				TreeSelection selection = new TreeSelection(treePaths);
				setSelection(selection);
				return;
	    	}
    	}
    	
    	setSelection(new TreeSelection(new TreePath[0]));
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
}
