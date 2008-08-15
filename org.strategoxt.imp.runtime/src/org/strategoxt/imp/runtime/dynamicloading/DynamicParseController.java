package org.strategoxt.imp.runtime.dynamicloading;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.ServiceFactory;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.services.IAnnotationTypeInfo;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.jface.text.IRegion;
import org.spoofax.jsglr.InvalidParseTableException;
import org.strategoxt.imp.runtime.Environment;

/**
 * This class functions as a dynamic proxy to a parse controller.
 * All dynamically loaded languages inherit from the "Root" language, which
 * specifies this class as its parse controller, allowing it to be
 * adapted at run time.  
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicParseController implements IParseController {
	private IParseController wrapped;

	public void setWrapped(IParseController value) {
		wrapped = value;
	}
	
	public DynamicParseController() {}
	
	public DynamicParseController(InputStream descriptor, InputStream parseTable) throws BadDescriptorException, IOException, InvalidParseTableException {
		Language language = LanguageLoader.register(descriptor, false);
		Environment.registerParseTable(language.getName(), parseTable);
		setWrapped(ServiceFactory.getInstance().getParseController(language));
	}

	public IAnnotationTypeInfo getAnnotationTypeInfo() {
		return wrapped.getAnnotationTypeInfo();
	}

	public Object getCurrentAst() {
		return wrapped.getCurrentAst();
	}

	public Language getLanguage() {
		return wrapped.getLanguage();
	}

	public ISourcePositionLocator getNodeLocator() {
		return wrapped.getNodeLocator();
	}

	public IPath getPath() {
		return wrapped.getPath();
	}

	public ISourceProject getProject() {
		return wrapped.getProject();
	}

	public ILanguageSyntaxProperties getSyntaxProperties() {
		return wrapped.getSyntaxProperties();
	}

	public Iterator getTokenIterator(IRegion region) {
		return wrapped.getTokenIterator(region);
	}

	public void initialize(IPath filePath, ISourceProject project, IMessageHandler handler) {
		if (wrapped == null) throw new IllegalStateException("Dynamic parse controller not initialized yet");
		wrapped.initialize(filePath, project, handler);
	}

	public Object parse(String input, boolean scanOnly, IProgressMonitor monitor) {
		return wrapped.parse(input, scanOnly, monitor);
	}
}
