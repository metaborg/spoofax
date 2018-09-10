package org.metaborg.core.build;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.IdentifiedResource;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.resource.IdentifiedResourceChange;
import org.metaborg.core.resource.ResourceChange;
import org.metaborg.core.resource.ResourceChangeKind;
import org.metaborg.util.iterators.Iterables2;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class LanguageBuildState {
    private final IResourceService resourceService;
    private final ILanguageIdentifierService languageIdentifierService;
    private final ILanguageImpl language;

    private final FilesBuildState source = new FilesBuildState();
    private final FilesBuildState include = new FilesBuildState();


    public LanguageBuildState(IResourceService resourceService, ILanguageIdentifierService languageIdentifierService,
        ILanguageImpl language) {
        this.resourceService = resourceService;
        this.languageIdentifierService = languageIdentifierService;
        this.language = language;
    }

    public LanguageBuildDiff diff(Iterable<IdentifiedResourceChange> sourceFileChanges,
        Iterable<IdentifiedResource> newIncludeFiles) {
        final LanguageBuildState newState = copy();
        sourceDiff(newState, sourceFileChanges);
        final Iterable<IdentifiedResourceChange> includeFileChanges = includeDiff(newState, newIncludeFiles);
        return new LanguageBuildDiff(newState, sourceFileChanges, includeFileChanges);
    }

    private LanguageBuildState copy() {
        final LanguageBuildState newState =
            new LanguageBuildState(resourceService, languageIdentifierService, language);
        newState.source.add(this.source);
        newState.include.add(this.include);

        return newState;
    }

    private void sourceDiff(LanguageBuildState newState, Iterable<IdentifiedResourceChange> changes) {
        for(IdentifiedResourceChange identifiedChange : changes) {
            final ResourceChange change = identifiedChange.change;
            switch(change.kind) {
                case Create:
                    newState.source.add(change.resource);
                    break;
                case Delete:
                    newState.source.remove(change.resource.getName());
                    break;
                case Rename:
                    final FileObject from = change.from;
                    if(from != null) {
                        newState.source.remove(from.getName());
                    }
                    newState.source.add(change.to);
                    break;
                case Copy:
                    newState.source.add(change.to);
                    break;
                default:
                    break;
            }
        }
    }

    private Iterable<IdentifiedResourceChange> includeDiff(LanguageBuildState newState,
        Iterable<IdentifiedResource> newFiles) {
        final Collection<IdentifiedResourceChange> changes = Lists.newLinkedList();
        final Set<FileName> existingFiles = Sets.newHashSet(include.files);
        for(IdentifiedResource identifiedResource : newFiles) {
            final FileObject resource = identifiedResource.resource;
            final FileName name = resource.getName();
            final long newModification = newState.include.add(resource);
            existingFiles.remove(name);
            if(include.files.contains(name)) {
                final long existingModification = include.modification.get(name);
                if(existingModification != newModification) {
                    changes.add(new IdentifiedResourceChange(new ResourceChange(resource, ResourceChangeKind.Modify),
                        identifiedResource));
                }
            } else {
                changes.add(new IdentifiedResourceChange(new ResourceChange(resource, ResourceChangeKind.Create),
                    identifiedResource));
            }
        }

        for(FileName name : existingFiles) {
            newState.include.remove(name);
            final FileObject resource = resourceService.resolve(name.getURI());
            final IdentifiedResource identifiedResource =
                languageIdentifierService.identifyToResource(resource, Iterables2.singleton(language));
            if(identifiedResource != null) {
                changes.add(new IdentifiedResourceChange(new ResourceChange(resource, ResourceChangeKind.Delete),
                    identifiedResource));
            }
        }

        return changes;
    }
}

class FilesBuildState {
    public final Set<FileName> files = Sets.newHashSet();
    public final Map<FileName,Long> modification = Maps.newHashMap();


    public long add(FileObject resource) {
        final FileName name = resource.getName();
        files.add(name);
        long newModification;
        try {
            newModification = resource.getContent().getLastModifiedTime();
            modification.put(name, newModification);
        } catch(FileSystemException e) {
            newModification = Long.MAX_VALUE;
            modification.put(name, Long.MIN_VALUE);
        }
        return newModification;
    }

    public void add(FilesBuildState state) {
        this.files.addAll(state.files);
        this.modification.putAll(state.modification);
    }

    public void remove(FileName name) {
        files.remove(name);
        modification.remove(name);
    }
}