package org.metaborg.spoofax.core.language;

import com.google.inject.Inject;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Forwards the implementation to the new implementation.
 *
 * @deprecated This class is only used for the configuration system migration.
 */
@Deprecated
public class LegacyLanguageDiscoveryService implements ILanguageDiscoveryService {

    private final INewLanguageDiscoveryService newLanguageDiscoveryService;

    @Inject
    public LegacyLanguageDiscoveryService(final INewLanguageDiscoveryService newLanguageDiscoveryService) {
        this.newLanguageDiscoveryService = newLanguageDiscoveryService;
    }

    @Override
    public Iterable<ILanguageDiscoveryRequest> request(FileObject location) throws MetaborgException {
        return convertAllFromNew( this.newLanguageDiscoveryService.request(location));
    }

    @Override
    public ILanguageComponent discover(ILanguageDiscoveryRequest request) throws MetaborgException {
        return this.newLanguageDiscoveryService.discover(convertToNew(request));
    }

    @Override
    public Iterable<ILanguageComponent> discover(Iterable<ILanguageDiscoveryRequest> requests) throws MetaborgException {
        return this.newLanguageDiscoveryService.discover(convertAllToNew(requests));
    }

    @Override
    public Iterable<ILanguageComponent> discover(FileObject location) throws MetaborgException {
        return this.newLanguageDiscoveryService.discover(location);
    }

    private ILanguageDiscoveryRequest convertFromNew(INewLanguageDiscoveryRequest request) {
        return new LegacyLanguageDiscoveryRequest(request);
    }

    private INewLanguageDiscoveryRequest convertToNew(ILanguageDiscoveryRequest request) {
        return ((LegacyLanguageDiscoveryRequest)request).newLanguageDiscoveryRequest();
    }

    private List<ILanguageDiscoveryRequest> convertAllFromNew(Iterable<INewLanguageDiscoveryRequest> requests) {
        List<ILanguageDiscoveryRequest> results = new ArrayList<>();
        for (INewLanguageDiscoveryRequest request : requests) {
            results.add(convertFromNew(request));
        }
        return results;
    }

    private List<INewLanguageDiscoveryRequest> convertAllToNew(Iterable<ILanguageDiscoveryRequest> requests) {
        List<INewLanguageDiscoveryRequest> results = new ArrayList<>();
        for (ILanguageDiscoveryRequest request : requests) {
            results.add(convertToNew(request));
        }
        return results;
    }
}
