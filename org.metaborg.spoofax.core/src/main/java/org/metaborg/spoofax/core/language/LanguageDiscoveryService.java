package org.metaborg.spoofax.core.language;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ComponentCreationConfig;
import org.metaborg.core.language.IComponentCreationConfigRequest;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageComponentFactory;
import org.metaborg.core.language.ILanguageDiscoveryRequest;
import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.LanguageUtils;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class LanguageDiscoveryService implements ILanguageDiscoveryService {
    private static final ILogger logger = LoggerUtils.logger(LanguageDiscoveryService.class);

    private final ILanguageService languageService;
    private final ILanguageComponentFactory componentFactory;


    @Inject public LanguageDiscoveryService(ILanguageService languageService,
        ILanguageComponentFactory componentFactory) {
        this.languageService = languageService;
        this.componentFactory = componentFactory;
    }


    @Override public ILanguageImpl languageFromDirectory(FileObject directory) throws MetaborgException {
        final Iterable<ILanguageImpl> languages = languagesFromDirectory(directory);
        if(Iterables.size(languages) > 1) {
            throw new MetaborgException("Attempted to load a single language implementation from directory " + directory
                + " but got multiple: " + Joiner.on(", ").join(languages));
        }
        return Iterables.get(languages, 0);
    }

    @Override public Set<ILanguageImpl> languagesFromDirectory(FileObject directory) throws MetaborgException {
        final ILanguageComponent component = componentFromDirectory(directory);
        return Sets.newHashSet(component.contributesTo());
    }


    @Override public ILanguageImpl languageFromArchive(FileObject archiveFile) throws MetaborgException {
        final Iterable<ILanguageImpl> languages = languagesFromArchive(archiveFile);
        if(Iterables.size(languages) > 1) {
            throw new MetaborgException("Attempted to load a single language implementation from archive " + archiveFile
                + " but got multiple: " + Joiner.on(", ").join(languages));
        }
        return Iterables.get(languages, 0);
    }

    @Override public Set<ILanguageImpl> languagesFromArchive(FileObject archiveFile) throws MetaborgException {
        final ILanguageComponent component = componentFromArchive(archiveFile);
        return Sets.newHashSet(component.contributesTo());
    }


    @Override public Set<ILanguageImpl> scanLanguagesInDirectory(FileObject directory) throws MetaborgException {
        final Iterable<ILanguageComponent> components = scanComponentsInDirectory(directory);
        return LanguageUtils.toImpls(components);
    }


    @Override public ILanguageComponent componentFromDirectory(FileObject directory) throws MetaborgException {
        final IComponentCreationConfigRequest request = componentFactory.requestFromDirectory(directory);
        return create(request);
    }

    @Override public ILanguageComponent componentFromArchive(FileObject archiveFile) throws MetaborgException {
        final IComponentCreationConfigRequest request = componentFactory.requestFromArchive(archiveFile);
        return create(request);
    }

    @Override public Set<ILanguageComponent> scanComponentsInDirectory(FileObject directory) throws MetaborgException {
        final Collection<IComponentCreationConfigRequest> requests = componentFactory.requestAllInDirectory(directory);
        final Collection<ComponentCreationConfig> configs = componentFactory.createConfigs(requests);
        final Set<ILanguageComponent> components = Sets.newHashSet();
        for(ComponentCreationConfig config : configs) {
            final ILanguageComponent component = languageService.add(config);
            components.add(component);
        }
        return components;
    }


    private ILanguageComponent create(IComponentCreationConfigRequest request) throws MetaborgException {
        final ComponentCreationConfig config = componentFactory.createConfig(request);
        final ILanguageComponent component = languageService.add(config);
        return component;
    }


    @Deprecated @Override public Iterable<ILanguageDiscoveryRequest> request(FileObject location)
        throws MetaborgException {
        final Collection<ILanguageDiscoveryRequest> requests = Lists.newLinkedList();
        final FileObject[] configFiles;
        try {
            configFiles = location.findFiles(new FileSelector() {
                @Override public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
                    final String baseName = fileInfo.getFile().getName().getBaseName();
                    return !baseName.equals("bin") && !baseName.equals("target");
                }

                @Override public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
                    return fileInfo.getFile().getName().getBaseName().equals(MetaborgConstants.FILE_COMPONENT_CONFIG);
                }
            });
        } catch(FileSystemException e) {
            throw new MetaborgException("Searching for language components failed unexpectedly", e);
        }

        if(configFiles == null || configFiles.length == 0) {
            return requests;
        }

        for(FileObject configFile : configFiles) {
            try {
                final FileObject directory = configFile.getParent().getParent();
                final IComponentCreationConfigRequest request = componentFactory.requestFromDirectory(directory);
                requests.add(request);
            } catch(FileSystemException e) {
                logger.error("Could not resolve parent directory of config file {}, skipping", e, configFile);
                continue;
            }
        }

        return requests;
    }

    @Deprecated @Override public ILanguageComponent discover(ILanguageDiscoveryRequest request)
        throws MetaborgException {
        final IComponentCreationConfigRequest realRequest = (IComponentCreationConfigRequest) request;
        return create(realRequest);
    }

    @Deprecated @Override public Iterable<ILanguageComponent> discover(Iterable<ILanguageDiscoveryRequest> requests)
        throws MetaborgException {
        final Collection<ILanguageComponent> components = Lists.newArrayList();
        for(ILanguageDiscoveryRequest request : requests) {
            components.add(discover(request));
        }
        return components;
    }
}
