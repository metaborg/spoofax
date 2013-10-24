package org.strategoxt.imp.runtime.services.views.outline;

import static org.strategoxt.imp.runtime.dynamicloading.TermReader.findTerm;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.termContents;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.services.StrategoObserver;

public class SpoofaxOutlineUtil {

	public final static int DEFAULT_OUTLINE_EXPAND_LEVEL = 3;
	
	public static ImploderOriginTermFactory factory = new ImploderOriginTermFactory(new TermFactory());

	public static IStrategoTerm getOutline(IParseController parseController) {
		EditorState editorState = EditorState.getEditorFor(parseController);
		StrategoObserver observer = getObserver(editorState);
		observer.getLock().lock();
		try {
			String outliner = termContents(findTerm(editorState.getDescriptor().getDocument(), "Outliner"));
			if (outliner == null) {
				outliner = "outline"; // for backwards compatibility
			}
			
			if (observer.getRuntime().lookupUncifiedSVar(outliner) == null) {
				return messageToOutlineNode("Can't find strategy '" + outliner + "'. Did you import 'editor/" + editorState.getLanguage().getName() + "-Outliner.str'?");
			}
			
			if (editorState.getCurrentAst() == null) {
				return null;
			}
			
			IStrategoTerm outline = observer.invokeSilent(outliner, editorState.getCurrentAst(), editorState.getResource().getFullPath().toFile());
			
			if (outline == null) {
				observer.reportRewritingFailed();
				return messageToOutlineNode("Strategy '" + outliner + "' failed.");
			}
			
			// ensure propagation of origin information
			factory.makeLink(outline, editorState.getCurrentAst());
			
			return outline;
		}

		finally {
			observer.getLock().unlock();
		}
	}
	
	private static IStrategoTerm messageToOutlineNode(String message) {
		return factory.makeAppl(factory.makeConstructor("Node", 2), factory.makeString(message), factory.makeList());
	}
	
	public static int getOutlineExpandLevel(IParseController parseController, IStrategoTerm outline) {
		EditorState editorState = EditorState.getEditorFor(parseController);
		String level = termContents(findTerm(editorState.getDescriptor().getDocument(), "OutlineExpandLevel"));
		if (level != null) {
			return Integer.parseInt(level);
		}
		
		// START: backwards compatibility (previously, the outline expand level was defined in Stratego using a fixed strategy name. 
		StrategoObserver observer = getObserver(editorState);
		observer.getLock().lock();
		try {
			if (observer.getRuntime().lookupUncifiedSVar(level) != null) {
				IStrategoTerm outline_expand_to_level = observer.invokeSilent("outline-expand-to-level", outline, editorState.getResource().getFullPath().toFile());
				if (outline_expand_to_level == null) {
					Environment.logException("outline-expand-to-level" + " failed.");
				}
				else if (outline_expand_to_level.getTermType() != IStrategoTerm.INT) {
					Environment.logException("outline-expand-to-level" + " returned " + outline_expand_to_level + ", but should return an integer instead.");
				}
				else {
					return ((IStrategoInt) outline_expand_to_level).intValue();
				}
			}
		}
		finally {
			observer.getLock().unlock();
		}
		// END: backwards compatibility
		
    	return DEFAULT_OUTLINE_EXPAND_LEVEL;
	}
	
	public static boolean isWellFormedOutlineNode(Object object) {
		if (object instanceof IStrategoAppl) {
			IStrategoAppl node = (IStrategoAppl) object;
			if (node.getConstructor().getName().equals("Node") && node.getSubtermCount() == 2) {
				IStrategoTerm label = node.getSubterm(0);
				IStrategoTerm children = node.getSubterm(1);
				if (children.getTermType() == IStrategoTerm.LIST) {
					if (label.getAnnotations().isEmpty()) {
						return true;
					}
					else {
						return label.getAnnotations().size() == 1 && label.getAnnotations().head().getTermType() == IStrategoTerm.STRING;
					}
				}
			}
		}
		return false;
	}
	
	public static IStrategoTerm getOutlineNodeOrigin(Object outlineNode) {
		IStrategoTerm node = (IStrategoTerm) outlineNode;
		IStrategoTerm label = node.getSubterm(0);
		
		if (ImploderAttachment.hasImploderOrigin(label)) {
			return ImploderAttachment.getImploderOrigin(label);
		}
		else if (ImploderAttachment.hasImploderOrigin(node)) {
			return ImploderAttachment.getImploderOrigin(node);
		}
		else {
			return null;
		}
	}
	
	public static void selectCorrespondingText(Object outlineNode, IParseController parseController) {
		selectCorrespondingText(new Object[]{outlineNode}, parseController);
	}
	
	public static void selectCorrespondingText(Object[] outlineNodes, IParseController parseController) {
		int startOffset = Integer.MAX_VALUE;
		int endOffset = Integer.MIN_VALUE;
		
		for (int i=0; i<outlineNodes.length; i++) {
			IStrategoTerm origin = getOutlineNodeOrigin(outlineNodes[i]);
			
			if (origin != null) {
	    		int startOffst = (ImploderAttachment.getLeftToken(origin).getStartOffset());
	    		int endOffst = (ImploderAttachment.getRightToken(origin).getEndOffset()) + 1;
				
				if (startOffst < startOffset) {
					startOffset = startOffst;
				}
				if (endOffst > endOffset) {
					endOffset = endOffst;
				}
			}
		}
    	
    	if (startOffset != Integer.MAX_VALUE && endOffset != Integer.MIN_VALUE) {
    		TextSelection newSelection = new TextSelection(startOffset, endOffset - startOffset);
    		ISelectionProvider selectionProvider = EditorState.getEditorFor(parseController).getEditor().getSelectionProvider();
    		selectionProvider.setSelection(newSelection);
    	}
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
