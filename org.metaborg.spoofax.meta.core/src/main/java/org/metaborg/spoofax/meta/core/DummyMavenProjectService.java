package org.metaborg.spoofax.meta.core;

import javax.annotation.Nullable;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.maven.project.MavenProject;
import org.metaborg.spoofax.core.project.IProject;
import org.metaborg.spoofax.core.project.IProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyMavenProjectService implements IProjectService, IMavenProjectService {
    private static final Logger log = LoggerFactory.getLogger(DummyMavenProjectService.class);
    
    public @Nullable MavenProject get(IProject project) {
        log.warn("Consider adding an implementation to get the MavenProject instance.");
        return null;
    }

    @Override
    public IProject get(final FileObject resource) {
        FileObject search = resource;
        try {
            if ( !FileType.FOLDER.equals(resource.getType()) ) {
                    search = search.getParent();
            }
        } catch (FileSystemException ex) {
            log.error("Problem getting type of {}.",resource,ex);
            return null;
        }
        while ( search != null ) {
            try {
                if ( resource.resolveFile("pom.xml") != null ) {
                    final FileObject location = search;
                    return new IProject() {
                        @Override
                        public FileObject location() {
                            return location;
                        }
                    };
                }
            } catch (FileSystemException ex) {
                log.error("Problem resolving {}/pom.xml.",resource,ex);
                return null;
            }
            try {
                search = search.getParent();
            } catch (FileSystemException ex) {
                log.error("Problem getting parent of {}.",resource,ex);
                return null;
            }
        }
        return null;
    }

}
