package org.strategoxt.imp.runtime.services.outline;

import java.io.File;
import java.io.FileNotFoundException;

import org.eclipse.core.resources.IResource;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.parser.IParseController;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.attachments.OriginAttachment;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.services.StrategoObserver;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;

public class SpoofaxOutlineUtil {

	public final static String OUTLINE_STRATEGY = "outline-strategy";
	public final static String OUTLINE_EXPAND_TO_LEVEL = "outline-expand-to-level";
	public final static int DEFAULT_OUTLINE_EXPAND_TO_LEVEL = 3;
	
	private static ImploderOriginTermFactory factory = new ImploderOriginTermFactory(new TermFactory());

	public static IStrategoTerm getOutline(IParseController parseController) {
		EditorState editorState = EditorState.getEditorFor(parseController);
		StrategoObserver observer = getObserver(editorState);
		observer.getLock().lock();
		try {
			if (observer.getRuntime().lookupUncifiedSVar(OUTLINE_STRATEGY) == null) {
				return messageToOutlineNode(OUTLINE_STRATEGY + " undefined");
			}
			
			IStrategoTerm outline = observer.invokeSilent(OUTLINE_STRATEGY, editorState.getCurrentAst(), editorState.getResource().getFullPath().toFile());
			
			if (outline == null) {
				return messageToOutlineNode(OUTLINE_STRATEGY + " failed");
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
	
	public static String getPluginPath(Object outlineNode) {
		String file = ImploderAttachment.getFilename(getOutlineNodeOrigin(outlineNode));
		IResource resource = null;
		try {
			resource = EditorIOAgent.getResource(new File(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Language language = LanguageRegistry.findLanguage(resource.getFullPath(), null);
		Descriptor descriptor = Environment.getDescriptor(language);
		return descriptor.getBasePath().toOSString();
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

	public static IStrategoTerm getOutlineNodeOrigin(Object outlineNode) {
		assert isOutlineNode(outlineNode);
		
		IStrategoTerm node = (IStrategoTerm) outlineNode;
		
		IStrategoTerm origin = OriginAttachment.getOrigin(node.getSubterm(0)); // use origin of label
    	if (origin == null) {
    		origin = OriginAttachment.getOrigin(node); // use origin of node
    	}
    	if (origin == null) {
    		origin = node.getSubterm(0); // assume label is origin
    	}
    	return origin;
	}
	
	public static boolean isOutlineNode(Object object) {
		if (!(object instanceof IStrategoAppl)) {
			return false;
		}
		
		IStrategoAppl node = (IStrategoAppl) object;
		IStrategoTerm[] subterms = node.getAllSubterms();
		
		return
			node.getConstructor().getName().equals("Node")
			&& subterms.length == 2
			&& subterms[0].getTermType() == IStrategoTerm.STRING
			&& subterms[1].getTermType() == IStrategoTerm.LIST;
	}
}
