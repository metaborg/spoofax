package org.strategoxt.imp.runtime.services.views.outline;

import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.services.StrategoObserver;

public class SpoofaxOutlineUtil {

	public static ImploderOriginTermFactory factory = new ImploderOriginTermFactory(new TermFactory());

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
	
	public static void selectCorrespondingText(Object outlineNode, EditorState editorState) {
		selectCorrespondingText(new Object[]{outlineNode}, editorState);
	}
	
	public static void selectCorrespondingText(Object[] outlineNodes, EditorState editorState) {
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
    		ISelectionProvider selectionProvider = editorState.getEditor().getSelectionProvider();
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
