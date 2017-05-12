package org.metaborg.core.project;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.config.IProjectConfig;
import org.metaborg.core.config.IProjectConfigService;

import com.google.inject.Inject;

/**
 * Creates a project from a single file with the parent directory as project location (if possible, otherwise just the
 * file) and no config (== null). Never returns null for the project. Doesn't cache projects.
 */
public class SingleFileProjectService implements IProjectService {

    private final IProjectConfigService projectConfigService;


    @Inject public SingleFileProjectService(IProjectConfigService projectConfigService) {
        this.projectConfigService = projectConfigService;
    }


    @Override public IProject get(FileObject resource) {
        FileObject rootFolder = resource;
        try {
            // project location should be a directory (otherwise building gave errors), so take parent dir (if possible)
            if(resource.isFile()) {
                rootFolder = resource.getParent();
            }
        } catch(FileSystemException e) {
            // ignore
        }
        final IProjectConfig config = projectConfigService.defaultConfig(rootFolder);
        return new Project(rootFolder, config);
    }
}
