package org.metaborg.meta.core.signature;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.build.CommonPaths;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.meta.core.project.ILanguageSpec;
import org.metaborg.util.file.IFileAccess;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class SignatureService implements ISignatureService, ISignatureWriter {
    private static final ILogger logger = LoggerUtils.logger(SignatureService.class);

    private final Set<ISignatureExtractor> extractors;


    @Inject public SignatureService(Set<ISignatureExtractor> extractors) {
        this.extractors = extractors;
    }


    @Override public Iterable<Signature> get(ILanguageSpec languageSpec) {
        final Collection<Signature> allSignatures = Lists.newArrayList();
        for(ISignatureExtractor extractor : extractors) {
            try {
                final Collection<Signature> signatures = extractor.extract(languageSpec);
                allSignatures.addAll(signatures);
            } catch(IOException | ParseException e) {
                logger.error("Extracting {} signatures for language specification {} failed; skipping", e, extractor,
                    languageSpec);
            }
        }
        return allSignatures;
    }

    @Override public Iterable<Signature> get(ILanguageImpl languageSpec) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override public Iterable<Signature> get(ILanguageComponent languageSpec) {
        // TODO Auto-generated method stub
        return null;
    }


    private Iterable<Signature> read(FileObject location) {
        final FileObject serializeFile = signaturesFile(languageSpec.location());
        final InputStream inputStream 
    }

    @Override public void write(ILanguageSpec languageSpec, Iterable<Signature> signatures,
        @Nullable IFileAccess access) throws MetaborgException {
        try {
            final FileObject serializeFile = signaturesFile(languageSpec.location());
            final OutputStream outputStream = serializeFile.getContent().getOutputStream();
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(signatures);
        } catch(IOException e) {
            throw new MetaborgException("Serializing signatures to file failed unexpectedly", e);
        }
    }


    private FileObject signaturesFile(FileObject location) throws FileSystemException {
        final CommonPaths paths = new CommonPaths(location);
        final FileObject serializeFile = paths.targetMetaborgDir().getChild("signature.dat");
        return serializeFile;
    }
}
