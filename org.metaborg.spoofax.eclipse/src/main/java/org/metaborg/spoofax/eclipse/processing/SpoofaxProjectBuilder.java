package org.metaborg.spoofax.eclipse.processing;

import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class SpoofaxProjectBuilder extends IncrementalProjectBuilder {
    private static final String qualifiedId = "org.metaborg.spoofax.eclipse.builder";


    /**
     * Adds this builder to given project. Does nothing if builder has already been added to the
     * project.
     * 
     * @param project
     *            Project to add the builder to.
     * @throws CoreException
     *             when {@link IProject#getDescription} throws a CoreException.
     */
    public static void addTo(IProject project) throws CoreException {
        final IProjectDescription projectDesc = project.getDescription();
        final ICommand[] builders = projectDesc.getBuildSpec();
        if(builderIndex(builders) == -1) {
            final ICommand newBuilder = projectDesc.newCommand();
            newBuilder.setBuilderName(qualifiedId);
            final ICommand[] newBuilders = ArrayUtils.add(builders, 0, newBuilder);
            projectDesc.setBuildSpec(newBuilders);
            project.setDescription(projectDesc, null);
        }
    }

    /**
     * Removes this builder from given project. Does nothing if the builder has not been added to
     * the project.
     * 
     * @param project
     *            Project to remove the builder from.
     * @throws CoreException
     *             when {@link IProject#getDescription} or {@link IProject#setDescription} throws a
     *             CoreException.
     */
    public static void removeFrom(IProject project) throws CoreException {
        final IProjectDescription projectDesc = project.getDescription();
        final ICommand[] builders = projectDesc.getBuildSpec();
        final int builderIndex = builderIndex(builders);
        if(builderIndex != -1) {
            final ICommand[] newBuilders = ArrayUtils.remove(builders, builderIndex);
            projectDesc.setBuildSpec(newBuilders);
            project.setDescription(projectDesc, null);
        }
    }

    private static int builderIndex(ICommand[] builders) throws CoreException {
        for(int i = 0; i < builders.length; ++i) {
            final ICommand builder = builders[i];
            if(builder.getBuilderName().equals(qualifiedId)) {
                return i;
            }
        }
        return -1;
    }


    @Override protected IProject[] build(int kind, Map<String, String> args,
        IProgressMonitor monitor) throws CoreException {
        if(kind == FULL_BUILD) {
            fullBuild(getProject(), monitor);
        } else {
            final IResourceDelta delta = getDelta(getProject());
            if(delta == null) {
                fullBuild(getProject(), monitor);
            } else {
                incrementalBuild(getProject(), delta, monitor);
            }
        }

        // Return value is be used to declare dependencies on other projects, but right now this is
        // not possible in Spoofax, so always return null.
        return null;
    }

    @Override protected void clean(IProgressMonitor monitor) throws CoreException {
        clean(getProject(), monitor);
    }

    private void clean(IProject project, IProgressMonitor monitor) {

    }

    private void fullBuild(IProject project, IProgressMonitor monitor) {

    }

    private void incrementalBuild(IProject project, IResourceDelta delta, IProgressMonitor monitor) {

    }
}
