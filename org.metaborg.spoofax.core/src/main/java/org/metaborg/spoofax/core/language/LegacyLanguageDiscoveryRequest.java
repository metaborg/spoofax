package org.metaborg.spoofax.core.language;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageDiscoveryRequest;
import org.metaborg.core.language.INewLanguageDiscoveryRequest;

/**
 * Forwards the implementation to the new implementation.
 *
 * @deprecated This class is only used for the configuration system migration.
 */
@Deprecated
public class LegacyLanguageDiscoveryRequest implements ILanguageDiscoveryRequest {

    private final INewLanguageDiscoveryRequest newLanguageDiscoveryRequest;

    public INewLanguageDiscoveryRequest newLanguageDiscoveryRequest() {
        return this.newLanguageDiscoveryRequest;
    }

    public LegacyLanguageDiscoveryRequest(final INewLanguageDiscoveryRequest newLanguageDiscoveryRequest) {
        this.newLanguageDiscoveryRequest = newLanguageDiscoveryRequest;
    }

    @Override
    public boolean available() {
        return this.newLanguageDiscoveryRequest.available();
    }

    @Override
    public FileObject location() {
        return this.newLanguageDiscoveryRequest.location();
    }

    @Override
    public Iterable<String> errors() {
        return this.newLanguageDiscoveryRequest.errors();
    }

    @Override
    public Iterable<Throwable> exceptions() {
        return this.newLanguageDiscoveryRequest.exceptions();
    }

    @Nullable
    @Override
    public String errorSummary() {
        return this.newLanguageDiscoveryRequest.toString();
    }
}
