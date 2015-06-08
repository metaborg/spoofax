package org.metaborg.spoofax.meta.core;

import com.google.inject.Inject;
import java.io.File;
import javax.annotation.Nullable;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.maven.project.DefaultProjectBuilder;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingResult;
import org.metaborg.spoofax.core.project.IProject;
import org.metaborg.spoofax.core.project.IProjectService;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultMavenProjectService implements IProjectService, IMavenProjectService {
    private static final Logger log = LoggerFactory.getLogger(DefaultMavenProjectService.class);
    
    private final IResourceService resourceService;

    @Inject public DefaultMavenProjectService(IResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public @Nullable MavenProject get(IProject project) {
        FileObject pom = null;
        try {
            pom = project.location().resolveFile("pom.xml");
        } catch (FileSystemException ex) {
        }
        if ( pom == null ) {
            log.warn("Cound not find {}/pom.xml.", project.location());
            return null;
        }
        File pomFile = resourceService.localFile(pom);
        if ( pomFile == null ) {
            return null;
        }
        DefaultProjectBuildingRequest request = new DefaultProjectBuildingRequest();
        DefaultProjectBuilder builder = new DefaultProjectBuilder();
        try {
            ProjectBuildingResult result = builder.build(pomFile, request);
            return result.getProject();
        } catch (ProjectBuildingException ex) {
            log.warn("Problem building project model.", ex);
            return null;
        }
    }

    @Override
    public IProject get(FileObject resource) {
        try {
            if ( !FileType.FOLDER.equals(resource.getType()) ) {
                    resource = resource.getParent();
            }
        } catch (FileSystemException ex) {
            log.error("Problem getting type of {}.",resource,ex);
            return null;
        }
        while ( resource != null ) {
            try {
                final FileObject pom = resource.resolveFile("pom.xml");
                if ( pom != null ) {
                    return new IProject() {
                        @Override
                        public FileObject location() {
                            return pom;
                        }
                    };
                }
            } catch (FileSystemException ex) {
                log.error("Problem resolving {}/pom.xml.",resource,ex);
                return null;
            }
            try {
                resource = resource.getParent();
            } catch (FileSystemException ex) {
                log.error("Problem getting parent of {}.",resource,ex);
                return null;
            }
        }
        return null;
    }

}
