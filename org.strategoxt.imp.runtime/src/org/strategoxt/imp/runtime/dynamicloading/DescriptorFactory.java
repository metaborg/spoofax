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
import org.strategoxt.imp.runtime.services.StrategoRuntimeFactory;
import org.strategoxt.lang.WeakValueHashMap;

import com.google.inject.Inject;

public class DescriptorFactory {
    private static DescriptorFactory instance;

    private final DescriptorRegistry registry;
    private final JSGLRI descriptorParser;
    private final Map<IResource, Descriptor> oldDescriptors = Collections
        .synchronizedMap(new WeakValueHashMap<IResource, Descriptor>());


    @Inject public DescriptorFactory(DescriptorRegistry registry) {
        this.registry = registry;
        DescriptorFactory.instance = this;

        try {
            InputStream stream = sdf2imp.class.getResourceAsStream("/EditorService.tbl");
            ParseTable table = Environment.loadParseTable(stream);
            Environment.registerParseTable(Descriptor.DESCRIPTOR_LANGUAGE, new ParseTableProvider(table));
            descriptorParser = new JSGLRI(table, "Module");
        } catch(Throwable e) {
            Environment.logException("Could not initialize the Descriptor class.", e);
            throw new IllegalStateException(e);
        }
    }


    public static void prepareForReload(IResource descriptor) {
        // HACK: keep static method to be compatible with existing code.
        instance.prepareForReloadInternal(descriptor);
    }

    /**
     * Creates a new {@link Descriptor} instance.
     * 
     * @param descriptor
     *            The descriptor stream to load
     * @param parseTable
     *            An associated parse table stream, or null
     * @param basePath
     *            A relative path of the descriptor, or null
     *
     * @throws BadDescriptorException
     */
    public static Descriptor load(InputStream descriptor, InputStream parseTable, IPath basePath)
        throws BadDescriptorException, IOException {
        // HACK: keep static method to be compatible with existing code.
        return instance.loadInternal(descriptor, parseTable, basePath);
    }

    public static Descriptor load(IFile descriptor, IResource source) throws CoreException, BadDescriptorException,
        IOException {
        return instance.loadInternal(descriptor, source);
    }

    private Descriptor loadInternal(InputStream descriptor, InputStream parseTable, IPath basePath)
        throws BadDescriptorException, IOException {

        Debug.startTimer();
        Descriptor result = parse(descriptor, basePath == null ? null : basePath.toPortableString());
        result.setBasePath(basePath);
        Language language = result.getLanguage();

        if(parseTable == null)
            parseTable = result.openParseTableStream();
        Environment.registerParseTable(language, new ParseTableProvider(result));
        Environment.registerDescriptor(language, result);
        if(registry != null)
            registry.register(result);

        Debug.stopTimer("Editor services loaded: " + result.getLanguage().getName());
        return result;
    }

    private Descriptor loadInternal(IFile descriptor, IResource source) throws CoreException, BadDescriptorException,
        IOException {
        IPath basePath = descriptor.getLocation();
        basePath = basePath.removeLastSegments(2); // strip off /include/filename.main.esv
        Debug.log("Loading editor services for ", descriptor.getName());

        // TODO: Optimize - lazily load parse table using ParseTableProvider?
        Descriptor result = load(descriptor.getContents(true), null, basePath);
        assert source.getName().endsWith(".main.esv");
        oldDescriptors.put(source, result);
        return result;
    }

    private Descriptor parse(InputStream input, String filename) throws BadDescriptorException, IOException {
        try {
            input = new PushbackInputStream(input, 100);

            IStrategoAppl document = tryReadTerm((PushbackInputStream) input);
            if(document == null) {
                document = (IStrategoAppl) descriptorParser.parse(input, filename);
            }
            return new Descriptor(document);
        } catch(SGLRException e) {
            throw new BadDescriptorException("Could not parse descriptor file", e);
        } catch(InterruptedException e) {
            throw new BadDescriptorException("Could not parse descriptor file", e);
        } finally {
            input.close();
        }
    }

    private void prepareForReloadInternal(IResource descriptor) {
        assert descriptor.getName().endsWith(".main.esv");
        Descriptor oldDescriptor = oldDescriptors.get(descriptor);
        if(oldDescriptor != null)
            oldDescriptor.prepareForReinitialize();
    }

    private static IStrategoAppl tryReadTerm(PushbackInputStream input) throws IOException {
        byte[] buffer = new byte[6];
        int bufferSize = input.read(buffer);
        if(bufferSize != -1)
            input.unread(buffer, 0, bufferSize);
        if((bufferSize == 6 && new String(buffer).equals("Module")) /* || BAFReader.isBinaryATerm(input) */) {
            TermReader reader = new TermReader(StrategoRuntimeFactory.BASE_TERM_FACTORY);
            return (IStrategoAppl) reader.parseFromStream(input);
        } else {
            return null;
        }
    }
}
