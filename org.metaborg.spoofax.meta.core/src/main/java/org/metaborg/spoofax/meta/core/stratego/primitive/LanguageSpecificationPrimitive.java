package org.metaborg.spoofax.meta.core.stratego.primitive;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.config.ConfigException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpec;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpecService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class LanguageSpecificationPrimitive extends ASpoofaxContextPrimitive {
    private static final ILogger logger = LoggerUtils.logger(LanguageSpecificationPrimitive.class);

    @Inject private static Provider<ISpoofaxLanguageSpecService> languageSpecServiceProvider;


    @Inject public LanguageSpecificationPrimitive() {
        super("language_specification", 0, 0);
    }


    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
        ITermFactory factory, IContext context) throws MetaborgException {
        final IProject project = context.project();
        if(project == null) {
            return null;
        }

        if(languageSpecServiceProvider == null) {
            // Indicates that meta-Spoofax is not available (ISpoofaxLanguageSpecService cannot be injected), but this
            // should never happen because this primitive is inside meta-Spoofax. Check for null just in case.
            logger.error("Language specification service is not available; static injection failed");
            return null;
        }
        final ISpoofaxLanguageSpecService languageSpecService = languageSpecServiceProvider.get();
        if(!languageSpecService.available(project)) {
            return null;
        }
        final ISpoofaxLanguageSpec languageSpec;
        try {
            languageSpec = languageSpecService.get(project);
        } catch(ConfigException e) {
            throw new MetaborgException("Unable to get language specification configuration for " + project, e);
        }
        if(languageSpec == null) {
            return null;
        }

        final IStrategoString nameTerm = factory.makeString(languageSpec.config().name());
        final LanguageIdentifier id = languageSpec.config().identifier();
        final IStrategoString groupIdTerm = factory.makeString(id.groupId);
        final IStrategoString idTerm = factory.makeString(id.id);
        final IStrategoString versionTerm = factory.makeString(id.version.toString());
        final IStrategoString locationTerm = factory.makeString(languageSpec.location().getName().getURI());
        return factory.makeTuple(nameTerm, groupIdTerm, idTerm, versionTerm, locationTerm);
    }
}
