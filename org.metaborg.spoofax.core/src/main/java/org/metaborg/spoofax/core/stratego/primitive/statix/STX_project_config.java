package org.metaborg.spoofax.core.stratego.primitive.statix;

import java.io.IOException;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.spoofax.core.config.ISpoofaxProjectConfig;
import org.metaborg.spoofax.core.config.ISpoofaxProjectConfigService;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Inject;

import mb.flowspec.terms.B;
import mb.statix.spoofax.IStatixProjectConfig;

public class STX_project_config extends ASpoofaxContextPrimitive {

    final ISpoofaxProjectConfigService projectConfigService;

    @Inject public STX_project_config(ISpoofaxProjectConfigService projectConfigService) {
        super(STX_project_config.class.getSimpleName(), 0, 0);
        this.projectConfigService = projectConfigService;
    }

    @Override protected IStrategoTerm call(@SuppressWarnings("unused") IStrategoTerm current,
            @SuppressWarnings("unused") Strategy[] svars, @SuppressWarnings("unused") IStrategoTerm[] tvars,
            @SuppressWarnings("unused") ITermFactory factory, IContext context) throws MetaborgException, IOException {
        IStatixProjectConfig statixConfig;
        ISpoofaxProjectConfig config = projectConfigService.get(context.project());
        if(config != null) {
            statixConfig = config.statixConfig();
        } else {
            statixConfig = IStatixProjectConfig.NULL;
        }
        return B.blob(statixConfig);
    }

}