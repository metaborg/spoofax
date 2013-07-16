package org.strategoxt.imp.runtime.services.outline;

import java.util.LinkedList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.attachments.OriginAttachment;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.services.StrategoObserver;
import org.strategoxt.imp.runtime.stratego.StrategoTermPath;
import org.strategoxt.lang.Context;

/**
 * @author Oskar van Rest
 */
public class SpoofaxOutlinePage extends ContentOutlinePage implements IModelListener {

	public final static String OUTLINE_STRATEGY = "outline-strategy";
	public final static String OUTLINE_EXPAND_TO_LEVEL = "outline-expand-to-level";
	private int outline_expand_to_level = 3;
	private EditorState editorState;
	private ImploderOriginTermFactory factory = new ImploderOriginTermFactory(new TermFactory());
	private StrategoObserver observer;
	private boolean debounceSelectionChanged;
	private IStrategoTerm outline;

	public SpoofaxOutlinePage(EditorState editorState) {
		this.editorState = editorState;
		
		try {
			observer = editorState.getDescriptor().createService(StrategoObserver.class, editorState.getParseController());
		}
		catch (BadDescriptorException e) {
			e.printStackTrace();
		}
		
		editorState.getEditor().addModelListener(this);
		editorState.getEditor().getSelectionProvider().addSelectionChangedListener(this);
	}
	
	@Override
	public void dispose() {
		editorState.getEditor().removeModelListener(this);
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		getTreeViewer().setContentProvider(new SpoofaxOutlineContentProvider());
		String pluginPath = editorState.getDescriptor().getBasePath().toOSString();
		getTreeViewer().setLabelProvider(new SpoofaxOutlineLabelProvider(pluginPath));
		
		observer.getLock().lock();
		try {
			IStrategoTerm outline_expand_to_level = observer.invokeSilent(OUTLINE_EXPAND_TO_LEVEL, editorState.getCurrentAst(), editorState.getResource().getFullPath().toFile());
			if (outline_expand_to_level != null) {
				this.outline_expand_to_level = ((IStrategoInt) outline_expand_to_level).intValue();
			}
		}
		finally {
			observer.getLock().unlock();
		}
		
		update();
	}

	public AnalysisRequired getAnalysisRequired() {
		return AnalysisRequired.NONE;
	}

	public void update(IParseController controller, IProgressMonitor monitor) {
		update();
	}
	
	public void update() {
		observer.getLock().lock();
		try {
			outline = observer.invokeSilent(OUTLINE_STRATEGY, editorState.getCurrentAst(), editorState.getResource().getFullPath().toFile());
			
			if (outline == null) {
				outline = factory.makeAppl(factory.makeConstructor("Node", 2), factory.makeString(OUTLINE_STRATEGY + " failed"), factory.makeList());
			}
			
			// ensures propagation of origin information
			factory.makeLink(outline, editorState.getCurrentAst());
			
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					getTreeViewer().setInput(outline);
					getTreeViewer().expandToLevel(outline_expand_to_level);
				}
			});
		}
		finally {
			observer.getLock().unlock();
		}
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

    	IStrategoTerm firstElem = (IStrategoTerm) treeSelection.getFirstElement();
    	IStrategoTerm origin = OriginAttachment.getOrigin(firstElem.getSubterm(0)); // use origin of label
    	if (origin == null) {
    		origin = OriginAttachment.getOrigin(firstElem); // use origin of node
    	}
    	if (origin == null) {
    		origin = firstElem.getSubterm(0); // assume label is origin
    	}
    	
    	if (ImploderAttachment.hasImploderOrigin(origin)) {
    		int startOffset = (ImploderAttachment.getLeftToken(origin).getStartOffset());
    		int endOffset = (ImploderAttachment.getRightToken(origin).getEndOffset()) + 1;
        	
    		TextSelection newSelection = new TextSelection(startOffset, endOffset - startOffset);
    		editorState.getEditor().getSelectionProvider().setSelection(newSelection);
    	}
    }
    
    public void textSelectionToOutlineSelection() {
    	IStrategoTerm textSelection = null;
    	
    	try {
    		textSelection = editorState.getSelectionAst(true);
    	}
    	catch (IndexOutOfBoundsException e) {
    		// hack: happens when user selects text, deletes it and then chooses 'undo'
    		// TODO: fix EditorState.getSelectionAst()
    	}
    	
    	if (textSelection != null) {
	    	Context context = observer.getRuntime().getCompiledContext();
	    	IStrategoList path = StrategoTermPath.getTermPathWithOrigin(context, outline, textSelection);
	    	
	    	if (path != null) {
		    	TreePath[] treePaths = termPathToTreePaths(path);
				TreeSelection selection = new TreeSelection(treePaths);
				setSelection(selection);
	    	}
    	}
    	
    	setSelection(new TreeSelection(new TreePath[0]));
    }
    
    private TreePath[] termPathToTreePaths(IStrategoList path) {
    	return termPathToTreePaths(path, outline, new LinkedList<IStrategoTerm>());
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
