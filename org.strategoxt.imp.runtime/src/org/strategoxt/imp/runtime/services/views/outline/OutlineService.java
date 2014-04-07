package org.strategoxt.imp.runtime.services.views.outline;

import org.eclipse.imp.parser.IParseController;
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
	
	private String outlineRule;
	
	private int expandToLevel;
	
	private ImploderOriginTermFactory factory = new ImploderOriginTermFactory(new TermFactory());
	
	private IParseController controller;
	
	public OutlineService(String outlineRule, int expandToLevel, IParseController controller) {
		this.outlineRule = outlineRule;
		this.expandToLevel = expandToLevel;
		this.controller = controller;
	}

	@Override
	public void setOutlineRule(String rule) {
		this.outlineRule = rule;
	}

	@Override
	public void setExpandToLevel(int level) {
		this.expandToLevel = level;
	}

	@Override
	public IStrategoTerm getOutline() {
		EditorState editorState = EditorState.getEditorFor(controller);
		StrategoObserver observer = getObserver(editorState);
		observer.getLock().lock();
		try {
			if (observer.getRuntime().lookupUncifiedSVar(outlineRule) == null) {
				return messageToOutlineNode("Can't find strategy '" + outlineRule + "'");
			}
			
			if (editorState.getCurrentAst() == null) {
				return null;
			}
			
			IStrategoTerm outline = observer.invokeSilent(outlineRule, editorState.getCurrentAst(), editorState.getResource().getFullPath().toFile());
			
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
