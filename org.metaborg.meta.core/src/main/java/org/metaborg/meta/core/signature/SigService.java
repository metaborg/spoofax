package org.metaborg.meta.core.signature;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.build.CommonPaths;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.meta.core.project.ILanguageSpec;
import org.metaborg.util.file.IFileAccess;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class SigService implements ISigService, ISigSerializer {
    private static final ILogger logger = LoggerUtils.logger(SigService.class);

    private final Set<ISigExtractor> extractors;


    @Inject public SigService(Set<ISigExtractor> extractors) {
        this.extractors = extractors;
    }


    @Override public Iterable<ISig> extract(ILanguageSpec languageSpec, @Nullable IFileAccess access) {
        final Collection<ISig> allSignatures = Lists.newArrayList();
        for(ISigExtractor extractor : extractors) {
            try {
                final Collection<ISig> signatures = extractor.extract(languageSpec, access);
                allSignatures.addAll(signatures);
            } catch(IOException | ParseException e) {
                logger.error("Extracting {} signatures for language specification {} failed; skipping", e, extractor,
                    languageSpec);
            }
        }
        return allSignatures;
    }


    @Override public Iterable<ISig> get(ILanguageSpec languageSpec) {
        try {
            final FileObject location = languageSpec.location();
            final FileObject serializeFile = signaturesFile(location);
            if(!serializeFile.exists()) {
                return Iterables2.empty();
            }
            return read(location, null);
        } catch(IOException e) {
            return Iterables2.empty();
        }
    }

    @Override public Iterable<ISig> get(ILanguageImpl langImpl) {
        final Collection<ISig> allSignatures = Lists.newArrayList();
        for(ILanguageComponent langComponent : langImpl.components()) {
            final Iterable<ISig> signatures = get(langComponent);
            Iterables.addAll(allSignatures, signatures);
        }
        return allSignatures;
    }

    @Override public Iterable<ISig> get(ILanguageComponent langComponent) {
        try {
            final FileObject location = langComponent.location();
            final FileObject serializeFile = signaturesFile(location);
            if(!serializeFile.exists()) {
                return Iterables2.empty();
            }
            return read(location, null);
        } catch(IOException e) {
            return Iterables2.empty();
        }
    }


    @SuppressWarnings("unchecked") public Iterable<ISig> read(FileObject location, @Nullable IFileAccess access)
        throws IOException {
        try {
            final FileObject serializeFile = signaturesFile(location);
            if(access != null) {
                access.read(serializeFile);
            }
            final InputStream inputStream = serializeFile.getContent().getInputStream();
            try(final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
                return (Iterable<ISig>) objectInputStream.readObject();
            }
        } catch(ClassCastException | ClassNotFoundException e) {
            throw new MetaborgRuntimeException("Deserializing signatures to file failed unexpectedly", e);
        }
    }

    @Override public void write(FileObject location, Iterable<ISig> signatures, @Nullable IFileAccess access)
        throws IOException {
        final FileObject serializeFile = signaturesFile(location);
        final OutputStream outputStream = serializeFile.getContent().getOutputStream();
        try(final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            objectOutputStream.writeObject(signatures);
        }
        if(access != null) {
            access.write(serializeFile);
        }
    }


    private FileObject signaturesFile(FileObject location) throws FileSystemException {
        final CommonPaths paths = new CommonPaths(location);
        final FileObject serializeFile = paths.targetMetaborgDir().resolveFile("signature.dat");
        return serializeFile;
    }
}
