package org.strategoxt.imp.runtime.services.outline;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.attachments.OriginAttachment;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.services.StrategoObserver;
import org.strategoxt.imp.runtime.stratego.SourceAttachment;
import org.strategoxt.imp.runtime.stratego.StrategoTermPath;
import org.strategoxt.lang.Context;

/**
 * @author Oskar van Rest
 */
public class SpoofaxOutlinePage extends ContentOutlinePage implements IModelListener {

	public final static String OUTLINE_STRATEGY = "outline-strategy";
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
		String pluginPath = editorState.getDescriptor().getBasePath().toPortableString();
		getTreeViewer().setLabelProvider(new SpoofaxOutlineLabelProvider(pluginPath));
		
		if (editorState.getCurrentAst() != null) {
			update();
		}
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
			outline = observer.invokeSilent(OUTLINE_STRATEGY, editorState.getCurrentAst(), SourceAttachment.getFile(editorState.getCurrentAst()));
			
			if (outline == null) { // outline-strategy undefined or failed
				System.out.println("outline failed");
				return;
			}
			
			// ensures propagation of origin information
			factory.makeLink(outline, editorState.getCurrentAst());
			
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					getTreeViewer().setInput(outline);
					getTreeViewer().expandToLevel(2);
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
    		debounceSelectionChanged = false;
    		return;
    	}
    	else {
    		if (event.getSource() == getTreeViewer()) {
        		outlineSelectionToTextSelection();
        	}
        	else {
        		textSelectionToOutlineSelection();
        	}
    	}
    }
    
    public void outlineSelectionToTextSelection() {
    	TreeSelection treeSelection = (TreeSelection) getSelection();
    	if (treeSelection.isEmpty()) {
    		return;
    	}

    	IStrategoTerm firstElem = (IStrategoTerm) treeSelection.getFirstElement();
    	IStrategoTerm origin = OriginAttachment.getOrigin(firstElem.getSubterm(0));
    	if (origin == null) {
    		origin = firstElem.getSubterm(0);
    	}
    	
    	if (ImploderAttachment.hasImploderOrigin(origin)) {
    		int startOffset = (ImploderAttachment.getLeftToken(origin).getStartOffset());
    		int endOffset = (ImploderAttachment.getRightToken(origin).getEndOffset()) + 1;
        	
    		TextSelection textSelection = new TextSelection(startOffset, endOffset - startOffset);
    		debounceSelectionChanged = true;
    		editorState.getEditor().getSelectionProvider().setSelection(textSelection);
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
    	
    	if (textSelection == null) {
    		return;
    	}
    	
    	Context context = observer.getRuntime().getCompiledContext();
    	IStrategoList path = StrategoTermPath.getTermPathWithOrigin(context, outline, textSelection);
    	
    	if (path == null) {
    		return;
    	}
    	
		IStrategoTerm[] outlineNodes = StrategoTermPath.getTermAtPath(context, outline, path).getAllSubterms();
		
//		System.out.println(outline.getSubterm(1));
//		Object[] objectPath = {outline.getSubterm(1)};
//		TreeSelection selection = new TreeSelection(new TreePath(objectPath));
//		setSelection(selection);
    }
    
    @Override
	public void setFocus() {
    	super.setFocus();
    	outlineSelectionToTextSelection();
    }
}
