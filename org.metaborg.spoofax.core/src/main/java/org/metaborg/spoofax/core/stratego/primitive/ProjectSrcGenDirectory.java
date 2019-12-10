package org.metaborg.spoofax.core.stratego.primitive;

import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.build.CommonPaths;
import org.metaborg.core.context.IContext;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.metaborg.util.resource.ResourceUtils;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Inject;

public class ProjectSrcGenDirectory extends ASpoofaxContextPrimitive {

    @Inject public ProjectSrcGenDirectory() {
        super("project_srcgen_dir", 0, 0);
    }


    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
            ITermFactory factory, IContext context) throws MetaborgException {
        final IProject project = context.project();
        if(project == null) {
            return null;
        }
        final CommonPaths paths = new CommonPaths(project.location());

        if(!Tools.isTermString(current)) {
            throw new MetaborgException("Expect a string as argument term, got " + current);
        }
        String name = Tools.asJavaString(current);

        final String srcGenDir;
        try {
            srcGenDir = ResourceUtils.relativeName(paths.srcGenDir().resolveFile(name).getName(),
                    project.location().getName(), true);
        } catch(FileSystemException e) {
            return null;
        }

        return factory.makeString(srcGenDir);
    }

}