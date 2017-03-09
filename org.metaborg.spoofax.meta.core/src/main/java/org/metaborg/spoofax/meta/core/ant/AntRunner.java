package org.metaborg.spoofax.meta.core.ant;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.PropertyHelper;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.util.task.ICancel;

public class AntRunner implements IAntRunner {
    private final Project antProject;


    public AntRunner(IResourceService resourceService, FileObject antFile, FileObject baseDir,
        Map<String, String> properties, @SuppressWarnings("unused") @Nullable URL[] classpaths,
        @Nullable BuildListener listener) {
        this.antProject = new Project();

        final File localAntFile = resourceService.localFile(antFile);
        final File localBaseDir = resourceService.localPath(baseDir);

        // TODO: use classpaths

        antProject.setProperty(MagicNames.ANT_FILE, localAntFile.getPath());
        antProject.setBaseDir(localBaseDir);
        antProject.init();
        if(listener != null) {
            antProject.addBuildListener(listener);
        }

        final PropertyHelper propHelper = PropertyHelper.getPropertyHelper(antProject);
        antProject.addReference(MagicNames.REFID_PROPERTY_HELPER, propHelper);
        for(Entry<String, String> property : properties.entrySet()) {
            propHelper.setUserProperty(property.getKey(), property.getValue());
        }

        final ProjectHelper projectHelper = ProjectHelper.getProjectHelper();
        antProject.addReference(MagicNames.REFID_PROJECT_HELPER, projectHelper);
        projectHelper.parse(antProject, localAntFile);
    }


    @Override public void execute(String target, @Nullable ICancel cancel) {
        // TODO: use cancel
        antProject.executeTarget(target);
    }
}
