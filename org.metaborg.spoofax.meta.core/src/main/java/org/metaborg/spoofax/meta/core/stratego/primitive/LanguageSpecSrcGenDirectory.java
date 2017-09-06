package org.metaborg.spoofax.meta.core.stratego.primitive;

import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.build.CommonPaths;
import org.metaborg.core.context.IContext;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpecService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.resource.ResourceUtils;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class LanguageSpecSrcGenDirectory extends ASpoofaxContextPrimitive {
    private static final ILogger logger = LoggerUtils.logger(LanguageSpecSrcGenDirectory.class);

    @Inject private static Provider<ISpoofaxLanguageSpecService> languageSpecServiceProvider;


    @Inject public LanguageSpecSrcGenDirectory() {
        super("language_spec_srcgen_dir", 0, 0);
    }


    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
            ITermFactory factory, IContext context) throws MetaborgException {
        final IProject project = context.project();
        if(project == null) {
            return null;
        }
        final CommonPaths paths = new CommonPaths(project.location());

        if(languageSpecServiceProvider == null) {
            // Indicates that meta-Spoofax is not available (ISpoofaxLanguageSpecService cannot be injected), but this
            // should never happen because this primitive is inside meta-Spoofax. Check for null just in case.
            logger.error("Language specification service is not available; static injection failed");
            return null;
        }

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
