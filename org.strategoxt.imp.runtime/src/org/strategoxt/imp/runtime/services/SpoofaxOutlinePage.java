package org.strategoxt.imp.runtime.services;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;

/**
 * @author Oskar van Rest
 */
public class SpoofaxOutlinePage extends ContentOutlinePage implements IModelListener {

	private final static String OUTLINE_STRATEGY = "outline-strategy";
	private EditorState editorState;
	private ImploderOriginTermFactory factory = new ImploderOriginTermFactory(new TermFactory());

	public SpoofaxOutlinePage(EditorState editorState) {
		this.editorState = editorState;
		editorState.getEditor().addModelListener(this);
	}
	
	@Override
	public void init(IPageSite pageSite) {
		super.init(pageSite);
	}

	public AnalysisRequired getAnalysisRequired() {
		return AnalysisRequired.NONE;
	}

	public void update(IParseController controller, IProgressMonitor monitor) {
		IStrategoTerm result = null;
		
		StrategoObserver observer = null;
		try {
			observer = editorState.getDescriptor().createService(StrategoObserver.class, editorState.getParseController());
		}
		catch (BadDescriptorException e) {
			e.printStackTrace();
		}
		
		observer.getLock().lock();
		try {
			result = observer.invokeSilent(OUTLINE_STRATEGY, (IStrategoTerm) controller.getCurrentAst());
		} finally {
			observer.getLock().unlock();
		}
		
		// ensures propagation of origin information
		factory.makeLink(result, (IStrategoTerm) controller.getCurrentAst());
		
//		getTreeViewer().setInput(result);
	}
}
