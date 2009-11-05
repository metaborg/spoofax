package org.strategoxt.imp.runtime.services;

import static org.eclipse.imp.utils.HTMLPrinter.*;

import java.util.Iterator;
import java.util.List;

import org.eclipse.imp.services.IAnnotationHover;
import org.eclipse.imp.utils.AnnotationUtils;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.ILineDiffInfo;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AnnotationHover extends DefaultAnnotationHover implements IAnnotationHover {
    
	@SuppressWarnings("unchecked")
	@Override
	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
    	List<Annotation> annotations = AnnotationUtils.getAnnotationsForLine(sourceViewer, lineNumber);
    	
    	for (Iterator<Annotation> iter = annotations.iterator(); iter.hasNext(); ) {
    		Annotation annotation = iter.next();
    		if (annotation instanceof ILineDiffInfo)
    			iter.remove();
    	}
    	
    	if (annotations.size() == 0) return "";
    	if (annotations.size() == 1) return formatSingle(annotations);
    	return formatMultiple(annotations);
	}

	private String formatSingle(List<Annotation> annotations) {
		StringBuffer result = new StringBuffer();
		addPageProlog(result);
		result.append("<p>");
		result.append(convertToHTMLContent(annotations.get(0).getText()));
		result.append("</p>");
		addPageEpilog(result);
    	return result.toString();
	}
	
	private String formatMultiple(List<Annotation> annotations) {
		StringBuffer result = new StringBuffer();
		addPageProlog(result);
		result.append("Multiple messages:<ul>");
		for (Annotation annotation : annotations) {
			result.append("<li> ");
			result.append(convertToHTMLContent(annotation.getText()));
			result.append("</li>");
		}
		addPageEpilog(result);
    	return result.toString();
    }

}
