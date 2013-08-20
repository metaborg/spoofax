package org.strategoxt.imp.runtime.services.outline;

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

	public final static String OUTLINE_STRATEGY = "outline";
	public final static String OUTLINE_EXPAND_TO_LEVEL = "outline-expand-to-level";
	public final static int DEFAULT_OUTLINE_EXPAND_TO_LEVEL = 3;
	
	private static ImploderOriginTermFactory factory = new ImploderOriginTermFactory(new TermFactory());

	public static Object getOutline(IParseController parseController) {
		EditorState editorState = EditorState.getEditorFor(parseController);
		StrategoObserver observer = getObserver(editorState);
		observer.getLock().lock();
		try {
			if (observer.getRuntime().lookupUncifiedSVar(OUTLINE_STRATEGY) == null) {
				return messageToOutlineNode("Can't find strategy '" + OUTLINE_STRATEGY + "'. Did you import 'editor/" + editorState.getLanguage().getName() + "-Outliner.str'?");
			}
			
			if (editorState.getCurrentAst() == null) {
				return null;
			}
			
			IStrategoTerm outline = observer.invokeSilent(OUTLINE_STRATEGY, editorState.getCurrentAst(), editorState.getResource().getFullPath().toFile());
			
			if (outline == null) {
				observer.reportRewritingFailed();
				return messageToOutlineNode("Strategy '" + OUTLINE_STRATEGY + "' failed.");
			}
			
			// workaround for https://bugs.eclipse.org/9262
			if (outline.getTermType() == IStrategoTerm.APPL) {
				outline = factory.makeList(outline);
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
	
	public static int getOutline_expand_to_level(IParseController parseController) {
		EditorState editorState = EditorState.getEditorFor(parseController);
		StrategoObserver observer = getObserver(editorState);
		observer.getLock().lock();
		try {
			if (observer.getRuntime().lookupUncifiedSVar(OUTLINE_EXPAND_TO_LEVEL) != null) {
				IStrategoTerm outline_expand_to_level = observer.invokeSilent(OUTLINE_EXPAND_TO_LEVEL, editorState.getCurrentAst(), editorState.getResource().getFullPath().toFile());
				if (outline_expand_to_level == null) {
					Environment.logException(OUTLINE_EXPAND_TO_LEVEL + " failed.");
				}
				else if (outline_expand_to_level.getTermType() != IStrategoTerm.INT) {
					Environment.logException(OUTLINE_EXPAND_TO_LEVEL + " returned " + outline_expand_to_level + ", but should return an integer instead.");
				}
				else {
					return ((IStrategoInt) outline_expand_to_level).intValue();
				}
			}
		}
		finally {
			observer.getLock().unlock();
		}
		
    	return DEFAULT_OUTLINE_EXPAND_TO_LEVEL;
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
		IStrategoTerm origin = getOutlineNodeOrigin(outlineNode);
    	
    	if (origin != null) {
    		int startOffset = (ImploderAttachment.getLeftToken(origin).getStartOffset());
    		int endOffset = (ImploderAttachment.getRightToken(origin).getEndOffset()) + 1;
        	
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
