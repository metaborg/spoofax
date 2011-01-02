package org.strategoxt.imp.runtime.services;

import static org.eclipse.imp.utils.HTMLPrinter.addPageEpilog;
import static org.eclipse.imp.utils.HTMLPrinter.addPageProlog;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.imp.services.IAnnotationHover;
import org.eclipse.imp.utils.AnnotationUtils;
import org.eclipse.imp.utils.HTMLPrinter;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.ILineDiffInfo;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AnnotationHover extends DefaultAnnotationHover implements IAnnotationHover {
    
	@Override
	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
    	List<Annotation> annotations = AnnotationUtils.getAnnotationsForLine(sourceViewer, lineNumber);
    	
    	removeLineDiffInfo(annotations);
    	
    	if (annotations.size() == 0) return "";
    	return formatMessages(annotations);
	}

	protected static String formatMessages(List<Annotation> annotations) {
		return (annotations.size() == 1)
				? formatSingle(annotations)
    			: formatMultiple(annotations);
	}

	protected static void removeLineDiffInfo(List<Annotation> annotations) {
		if (annotations == null) return;
		for (Iterator<Annotation> iter = annotations.iterator(); iter.hasNext(); ) {
    		Annotation annotation = iter.next();
    		if (annotation.getText() == null || annotation instanceof ILineDiffInfo)
    			iter.remove();
    	}
	}

	private static String formatSingle(List<Annotation> annotations) {
		StringBuffer result = new StringBuffer();
		addPageProlog(result);
		result.append("<p>");
		result.append(convertToHTMLContent(annotations.get(0).getText()));
		result.append("</p>");
		addPageEpilog(result);
    	return result.toString();
	}
	
	private static String formatMultiple(List<Annotation> annotations) {
		StringBuffer result = new StringBuffer();
		addPageProlog(result);
		result.append("Multiple messages:<ul>");
		Collections.reverse(annotations);
		for (Annotation annotation : annotations) {
			result.append("<li> ");
			result.append(convertToHTMLContent(annotation.getText()));
			result.append("</li>");
		}
		result.append("</ul>");
		addPageEpilog(result);
    	return result.toString();
    }
	
	public static String convertToHTMLContent(String content) {
		return HTMLPrinter.convertToHTMLContent(content).replace("\n", "<br />");
	}

}
