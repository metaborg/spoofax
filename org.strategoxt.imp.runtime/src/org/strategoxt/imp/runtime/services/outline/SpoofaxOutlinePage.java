package org.strategoxt.imp.runtime.services.outline;

import java.util.LinkedList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.stratego.StrategoTermPath;
import org.strategoxt.lang.Context;

/**
 * @author Oskar van Rest
 */
public class SpoofaxOutlinePage extends ContentOutlinePage implements IModelListener {

	private final IParseController parseController;
	private boolean debounceSelectionChanged;
	private Object outline;

	public SpoofaxOutlinePage(IParseController parseController) {
		this.parseController = parseController;
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		getTreeViewer().setContentProvider(new SpoofaxOutlineContentProvider());
		String pluginPath = EditorState.getEditorFor(parseController).getDescriptor().getBasePath().toOSString();
		getTreeViewer().setLabelProvider(new SpoofaxOutlineLabelProvider(pluginPath));
		
		EditorState editorState = EditorState.getEditorFor(parseController);
		editorState.getEditor().addModelListener(this);
		editorState.getEditor().getSelectionProvider().addSelectionChangedListener(this);
		
		if (parseController.getCurrentAst() != null) {
			// The editor sporadically manages to parse the file before our model listener gets added,
			// resulting in an empty outline on startup. We therefore perform a 'manual' update:
			update();
		}
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
		final Object outline = SpoofaxOutlineUtil.getOutline(parseController);
		this.outline = outline;
		final int outline_expand_to_level = SpoofaxOutlineUtil.getOutline_expand_to_level(parseController);
						
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				getTreeViewer().setInput(outline);
				getTreeViewer().expandToLevel(outline_expand_to_level);
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

    	Object firstElem = treeSelection.getFirstElement();
    	SpoofaxOutlineUtil.selectCorrespondingText(firstElem, parseController);
    }
    
    public void textSelectionToOutlineSelection() {
    	if (outline == null) {
    		return;
    	}
    	
    	EditorState editorState = EditorState.getEditorFor(parseController);
    	IStrategoTerm textSelection = null;
    	
    	try {
    		textSelection = editorState.getSelectionAst(true);
    	}
    	catch (IndexOutOfBoundsException e) {
    		// bug in EditorState.getSelectionAst()
    	}
    	
    	if (textSelection != null) {
	    	Context context = SpoofaxOutlineUtil.getObserver(editorState).getRuntime().getCompiledContext();
	    	IStrategoList path = null;
	    	try {
	    		path = StrategoTermPath.getTermPathWithOrigin(context, (IStrategoTerm) outline, textSelection);
	    	}
	    	catch (IndexOutOfBoundsException e) {
	    		// bug in StrategoTermPath.getTermPathWithOrigin(..)
	    	}
	    	
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
