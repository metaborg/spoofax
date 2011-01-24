package org.strategoxt.imp.runtime.dynamicloading;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.language.Language;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.jsglr.shared.SGLRException;
import org.spoofax.terms.io.binary.TermReader;
import org.strategoxt.imp.generator.sdf2imp;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.JSGLRI;
import org.strategoxt.lang.WeakValueHashMap;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DescriptorFactory {
	
	private static final DescriptorRegistry registry;
	
	static {
		DescriptorRegistry newRegistry;
		try {
			newRegistry = new DescriptorRegistry();
		} catch (IllegalStateException e) {
			// Eclipse was not initialized; ignore
			Environment.logException("Could not initialize descriptor/editor registry", e);
			newRegistry = null;
		}
		registry = newRegistry;
	}
	
	private static JSGLRI descriptorParser;
	
	private static Map<IResource, Descriptor> oldDescriptors =
		Collections.synchronizedMap(new WeakValueHashMap<IResource, Descriptor>());
	
	private static void init() {
		if (descriptorParser != null) return;
		try {
			InputStream stream = sdf2imp.class.getResourceAsStream("/EditorService.tbl");
			ParseTable table = Environment.loadParseTable(stream);
			Environment.registerParseTable(Descriptor.DESCRIPTOR_LANGUAGE, new ParseTableProvider(table));
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
		
		// TODO: Optimize - lazily load parse table using ParseTableProvider?
		Descriptor result = load(descriptor.getContents(true), null, basePath);
		assert source.getName().endsWith(".main.esv");
		oldDescriptors.put(source, result);
		return result;
	}
	
	public static void prepareForReload(IResource descriptor) {
		assert descriptor.getName().endsWith(".main.esv");
		Descriptor oldDescriptor = oldDescriptors.get(descriptor);
		if (oldDescriptor != null)
			oldDescriptor.prepareForReinitialize();
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
		Descriptor result = parse(descriptor, basePath == null ? null : basePath.toPortableString());
		result.setBasePath(basePath);
		Language language = result.getLanguage();
		
		if (parseTable == null) parseTable = result.openParseTableStream();
		Environment.registerParseTable(language, new ParseTableProvider(result));		
		Environment.registerDescriptor(language, result);
		if (registry != null) registry.register(result);
		
		Debug.stopTimer("Editor services loaded: " + result.getLanguage().getName());
		return result;
	}
	
	public static Descriptor parse(InputStream input, String filename) throws BadDescriptorException, IOException {
		try {
			input = new PushbackInputStream(input, 100);
			
			IStrategoAppl document = tryReadTerm((PushbackInputStream) input);
			if (document == null) {
				init();
				document = (IStrategoAppl) descriptorParser.parse(input, filename);
			}
			return new Descriptor(document);
		} catch (SGLRException e) {
			throw new BadDescriptorException("Could not parse descriptor file", e);
		} finally {
			input.close();
		}
	}
	
	private static IStrategoAppl tryReadTerm(PushbackInputStream input) throws IOException {
		byte[] buffer = new byte[6];
		int bufferSize = input.read(buffer);
		if (bufferSize != -1) input.unread(buffer, 0, bufferSize);
		if ((bufferSize == 6 && new String(buffer).equals("Module")) /* || BAFReader.isBinaryATerm(input)*/) { 
			TermReader reader = new TermReader(Environment.getTermFactory());
			return (IStrategoAppl) reader.parseFromStream(input);
		} else {
			return null;
		}
	}
}
