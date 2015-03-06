package org.metaborg.spoofax.core.resource;

import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.spoofax.core.language.AllLanguagesFileSelector;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;

import rx.Observable;
import rx.subjects.PublishSubject;

import com.google.inject.Inject;

public class OfflineResourceChangeService implements IResourceChangeService {
    private final PublishSubject<IResourceChange> subject = PublishSubject.create();

    private final OfflineResourceChangeMonitor monitor;


    @Inject public OfflineResourceChangeService(IResourceService resourceService,
        ILanguageIdentifierService langaugeIdentifierService) {
        monitor =
            new OfflineResourceChangeMonitor(resourceService.root(), resourceService.userStorage(),
                new AllLanguagesFileSelector(langaugeIdentifierService), resourceService);
    }


    @Override public Observable<IResourceChange> changes() {
        return subject;
    }

    public void update() throws FileSystemException {
        for(IResourceChange change : monitor.update()) {
            subject.onNext(change);
        }
    }

    public void read() throws FileSystemException {
        monitor.read();
    }

    public void write() throws FileSystemException {
        monitor.write();
    }
}
