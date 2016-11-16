package org.metaborg.meta.core.signature.generate;

import java.io.IOException;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.meta.core.signature.ISig;
import org.metaborg.util.file.IFileAccess;

import com.google.inject.Inject;

public class SigGenService implements ISigGenService {
    private final Set<IRawSigGen> rawSigGens;
    private final Set<ISigGen> sigGens;


    @Inject public SigGenService(Set<IRawSigGen> rawSigGens, Set<ISigGen> sigGens) {
        this.rawSigGens = rawSigGens;
        this.sigGens = sigGens;
    }


    @Override public void generate(Iterable<ISig> signatures, FileObject dir, @Nullable IFileAccess access)
        throws IOException {
        for(IRawSigGen sigGen : rawSigGens) {
            sigGen.generate(signatures, dir, access);
        }
        final SigGenHelper helper = new SigGenHelper(sigGens);
        helper.generate(signatures, dir, access);
    }
}
