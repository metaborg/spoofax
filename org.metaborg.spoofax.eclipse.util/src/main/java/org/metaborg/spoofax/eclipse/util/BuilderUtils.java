package org.metaborg.spoofax.eclipse.util;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuilderUtils {
    private static final Logger logger = LoggerFactory.getLogger(BuilderUtils.class);

    /**
     * Adds builder to given project. Does nothing if builder has already been added to the project.
     * 
     * @param id
     *            Identifier of the builder to add.
     * @param project
     *            Project to add the builder to.
     * @param triggers
     *            Which triggers the builder should respond to. Only used if the builder allows its build kinds to be
     *            configured (isConfigurable="true" in plugin.xml).
     * @throws CoreException
     *             When {@link IProject#getDescription} throws a CoreException.
     */
    public static void addTo(String id, IProject project, int... triggers) throws CoreException {
        final IProjectDescription projectDesc = project.getDescription();
        final ICommand[] builders = projectDesc.getBuildSpec();
        if(index(id, builders) == -1) {
            final ICommand newBuilder = projectDesc.newCommand();
            newBuilder.setBuilderName(id);
            if(triggers.length > 0) {
                if(!newBuilder.isConfigurable()) {
                    logger.error(
                        "Trying to set build triggers for {}, but builder does not support configuring triggers. "
                            + "Set isConfigurable=\"true\" for that builder in plugin.xml", id);
                }
                newBuilder.setBuilding(IncrementalProjectBuilder.AUTO_BUILD,
                    ArrayUtils.contains(triggers, IncrementalProjectBuilder.AUTO_BUILD));
                newBuilder.setBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD,
                    ArrayUtils.contains(triggers, IncrementalProjectBuilder.INCREMENTAL_BUILD));
                newBuilder.setBuilding(IncrementalProjectBuilder.FULL_BUILD,
                    ArrayUtils.contains(triggers, IncrementalProjectBuilder.FULL_BUILD));
                newBuilder.setBuilding(IncrementalProjectBuilder.CLEAN_BUILD,
                    ArrayUtils.contains(triggers, IncrementalProjectBuilder.CLEAN_BUILD));
            }
            final ICommand[] newBuilders = ArrayUtils.add(builders, newBuilder);
            projectDesc.setBuildSpec(newBuilders);
            project.setDescription(projectDesc, null);
        }
    }

    /**
     * Removes builder from given project. Does nothing if the builder has not been added to the project.
     * 
     * @param id
     *            Identifier of the builder to remove.
     * @param project
     *            Project to remove the builder from.
     * @throws CoreException
     *             When {@link IProject#getDescription} or {@link IProject#setDescription} throws a CoreException.
     */
    public static void removeFrom(String id, IProject project) throws CoreException {
        final IProjectDescription projectDesc = project.getDescription();
        final ICommand[] builders = projectDesc.getBuildSpec();
        final int builderIndex = index(id, builders);
        if(builderIndex != -1) {
            final ICommand[] newBuilders = ArrayUtils.remove(builders, builderIndex);
            projectDesc.setBuildSpec(newBuilders);
            project.setDescription(projectDesc, null);
        }
    }

    private static int index(String id, ICommand[] builders) throws CoreException {
        for(int i = 0; i < builders.length; ++i) {
            final ICommand builder = builders[i];
            if(builder.getBuilderName().equals(id)) {
                return i;
            }
        }
        return -1;
    }
}
