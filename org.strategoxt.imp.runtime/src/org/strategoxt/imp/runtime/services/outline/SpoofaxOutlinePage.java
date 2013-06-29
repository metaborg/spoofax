package org.strategoxt.imp.runtime.services.outline;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.attachments.OriginAttachment;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.services.StrategoObserver;

/**
 * @author Oskar van Rest
 */
public class SpoofaxOutlinePage extends ContentOutlinePage implements IModelListener {

	private final static String OUTLINE_STRATEGY = "outline-strategy";
	private EditorState editorState;
	private ImploderOriginTermFactory factory = new ImploderOriginTermFactory(new TermFactory());
	private StrategoObserver observer;

	public SpoofaxOutlinePage(EditorState editorState) {
		this.editorState = editorState;
		
		try {
			observer = editorState.getDescriptor().createService(StrategoObserver.class, editorState.getParseController());
		}
		catch (BadDescriptorException e) {
			e.printStackTrace();
		}
		
		editorState.getEditor().addModelListener(this);
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
			final IStrategoTerm result = observer.invokeSilent(OUTLINE_STRATEGY, editorState.getCurrentAst());
			
			if (result == null) { // outline-strategy undefined or failed
				return;
			}
			
			// ensures propagation of origin information
			factory.makeLink(result, editorState.getCurrentAst());
			
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					getTreeViewer().setInput(result);
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
    	super.selectionChanged(event);
    	
    	TreeSelection treeSelection = ((TreeSelection) event.getSelection());
    	if (treeSelection.isEmpty()) {
    		return;
    	}

    	IStrategoTerm firstElem = (IStrategoTerm) treeSelection.getFirstElement();
    	IStrategoTerm origin = OriginAttachment.getOrigin(firstElem.getSubterm(0));
    	
    	if (origin != null) {
    		int startOffset = (ImploderAttachment.getLeftToken(origin).getStartOffset());
    		int endOffset = (ImploderAttachment.getRightToken(origin).getEndOffset()) + 1;
        	
    		TextSelection textSelection = new TextSelection(startOffset, endOffset - startOffset);
    		editorState.getEditor().getSelectionProvider().setSelection(textSelection);
    	}
    }
}
