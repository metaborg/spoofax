package org.strategoxt.imp.runtime.dynamicloading;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.language.Language;
import org.jboss.util.collection.WeakValueHashMap;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.jsglr.InvalidParseTableException;
import org.spoofax.jsglr.ParseTable;
import org.spoofax.jsglr.SGLRException;
import org.strategoxt.imp.generator.sdf2imp;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.JSGLRI;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DescriptorFactory {
	
	private static final DescriptorRegistry registry = new DescriptorRegistry();
	
	private static JSGLRI descriptorParser;
	
	private static Map<IResource, Descriptor> asyncOldDescriptors = new WeakValueHashMap<IResource, Descriptor>();
	
	private static void init() {
		if (descriptorParser != null) return;
		try {
			InputStream stream = sdf2imp.class.getResourceAsStream("/EditorService.tbl");
			ParseTable table = Environment.registerParseTable(Descriptor.DESCRIPTOR_LANGUAGE, stream);
			descriptorParser = new JSGLRI(table, "Module");
		} catch (Throwable e) {
			Environment.logException("Could not initialize the Descriptor class.", e);
			throw new IllegalStateException(e);
		}
	}
	
	public static Descriptor load(IFile descriptor, IResource source) throws CoreException, BadDescriptorException, IOException {
		IPath basePath = descriptor.getLocation();
		basePath = basePath.removeLastSegments(2); // strip off /include/filename.main.esv
		Debug.log("Loading editor services for ", descriptor.getName());
		
		Descriptor result = load(descriptor.getContents(true), null, basePath);
		synchronized (asyncOldDescriptors) {
			assert source.getName().endsWith(".main.esv");
			asyncOldDescriptors.put(source, result);
		}
		return result;
	}
	
	public static void prepareForReload(IResource descriptor) {
		assert descriptor.getName().endsWith(".main.esv");
		synchronized (asyncOldDescriptors) {
			Descriptor oldDescriptor = asyncOldDescriptors.get(descriptor);
			if (oldDescriptor != null)
				oldDescriptor.prepareForReinitialize();
		}
	}
	
	/**
	 * Creates a new {@link Descriptor} instance.
	 * 
	 * @param descriptor  The descriptor stream to load
	 * @param parseTable  An associated parse table stream, or null
	 * @param basePath    A relative path of the descriptor, or null
	 *
	 * @throws BadDescriptorException
	 */
	public static Descriptor load(InputStream descriptor, InputStream parseTable, IPath basePath)
			throws BadDescriptorException, IOException {
		
		Debug.startTimer();
		Descriptor result = parse(descriptor);
		result.setBasePath(basePath);
		Language language = result.getLanguage();
		
		if (parseTable == null) parseTable = result.openParseTableStream();
		registerParseTable(language, parseTable);
		
		Environment.registerDescriptor(language, result);
		registry.register(result);
		
		Debug.stopTimer("Editor services loaded: " + result.getLanguage().getName());
		return result;
	}
	
	public static Descriptor parse(InputStream input) throws BadDescriptorException, IOException {
		try {
			init();
			synchronized (Environment.getSyncRoot()) {
				IStrategoAppl document = descriptorParser.parse(input, "(descriptor)").getTerm();
				return new Descriptor(document);
			}
		} catch (SGLRException e) {
			throw new BadDescriptorException("Could not parse descriptor file", e);
		}
	}

	private static void registerParseTable(Language language, InputStream table) throws BadDescriptorException {
		try {
			Environment.registerParseTable(language, table);
		} catch (IOException e) {
			throw new BadDescriptorException("Could not load editor service parse table", e);
		} catch (InvalidParseTableException e) {
			throw new BadDescriptorException("Could not load editor service parse table", e);
		}
	}
}
