package org.strategoxt.imp.runtime.services;

import java.util.List;

import org.eclipse.imp.editor.AnnotationHoverBase;
import org.eclipse.imp.language.ServiceFactory;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.services.IDocumentationProvider;
import org.eclipse.imp.services.IHoverHelper;
import org.eclipse.imp.services.IReferenceResolver;
import org.eclipse.imp.services.base.HoverHelperBase;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.strategoxt.imp.runtime.Environment;

public class AnnotationHoverHelper extends HoverHelperBase implements IHoverHelper {
	
	public String getHoverHelpAt(IParseController parseController, ISourceViewer viewer, int offset) {
        try {
            int line = viewer.getDocument().getLineOfOffset(offset);
			List<Annotation> annotations = AnnotationHoverBase.getSourceAnnotationsForLine(viewer, line);
			AnnotationHover.removeLineDiffInfo(annotations);

            if (annotations != null && annotations.size() > 0)
                return AnnotationHover.formatMessages(annotations);
        } catch (BadLocationException e) {
        	Environment.logException("Could not display hover help", e);
            return "???";
        }

    	IReferenceResolver refResolver = ServiceFactory.getInstance().getReferenceResolver(fLanguage);
        Object root= parseController.getCurrentAst();
        ISourcePositionLocator nodeLocator = parseController.getSourcePositionLocator();

        if (root == null) return null;

        Object selNode = nodeLocator.findNode(root, offset);

        if (selNode == null) return null;

       	Object target = (refResolver != null) ? refResolver.getLinkTarget(selNode, parseController) : selNode;

       	if (target == null) target=selNode;

       	IDocumentationProvider docProvider= ServiceFactory.getInstance().getDocumentationProvider(fLanguage);
       	String doc= (docProvider != null) ? docProvider.getDocumentation(target, parseController) : null;			

       	return doc;
	}

}
