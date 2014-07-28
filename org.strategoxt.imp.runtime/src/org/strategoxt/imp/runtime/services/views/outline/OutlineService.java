package org.strategoxt.imp.runtime.services.views.outline;

import org.eclipse.swt.widgets.Display;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.services.StrategoObserver;

/**
 * @author Oskar van Rest
 */
public class OutlineService implements IOutlineService {
	
	private final String outlineRule;
	private final boolean source;
	private final boolean onselection;
	private final int expandToLevel;
	private IStrategoTerm selectionAst;
	
	private ImploderOriginTermFactory factory = new ImploderOriginTermFactory(new TermFactory());
	
	
	public OutlineService(String outlineRule, boolean source, boolean onselection, int expandToLevel, EditorState editorState) {
		this.outlineRule = outlineRule;
		this.source = true; // TODO
		this.onselection = onselection;
		this.expandToLevel = expandToLevel;
	}

	@Override
	public IStrategoTerm getOutline(final EditorState editorState) {
		StrategoObserver observer = getObserver(editorState);
		observer.getLock().lock();
		try {
			if (observer.getRuntime().lookupUncifiedSVar(outlineRule) == null) {
				return messageToOutlineNode("Can't find strategy '" + outlineRule + "'");
			}
			
			if (onselection) {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						selectionAst = editorState.getSelectionAst(false);
					}
				});
			}
			
			if (editorState.getCurrentAst() == null) {
				return null;
			}
			
			IStrategoTerm outline = null;
			observer.getLock().lock();
			try {
				IStrategoTerm input = observer.getInputBuilder().makeInputTerm(selectionAst == null ? editorState.getCurrentAst() : selectionAst, true, source);
				outline = observer.invokeSilent(outlineRule, input, editorState.getResource().getFullPath().toFile());
			}
			finally {
				observer.getLock().unlock();
			}
			if (outline == null) {
				observer.reportRewritingFailed();
				return messageToOutlineNode("Strategy '" + outlineRule + "' failed");
			}
			
			// ensure propagation of origin information
			factory.makeLink(outline, editorState.getCurrentAst());
			
			return outline;
		}

		finally {
			observer.getLock().unlock();
		}
	}
	
	private IStrategoTerm messageToOutlineNode(String message) {
		return factory.makeAppl(factory.makeConstructor("Node", 2), factory.makeString(message), factory.makeList());
	}

	@Override
	public int getExpandToLevel() {
		return expandToLevel;
	}
	
	@Override
	public boolean getOnselection() {
		return onselection;
	}
	
	public static StrategoObserver getObserver(EditorState editorState) {
		try {
			return editorState.getDescriptor().createService(StrategoObserver.class, editorState.getParseController());
		}
		catch (BadDescriptorException e) {
			e.printStackTrace();
		}
		return null;
	}
}
