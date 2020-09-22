package org.metaborg.spoofax.core.stratego.primitive.statix;

import java.io.IOException;

import javax.annotation.Nullable;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.spoofax.core.config.ISpoofaxProjectConfig;
import org.metaborg.spoofax.core.config.ISpoofaxProjectConfigService;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Inject;

public class STX_is_concurrent_enabled extends ASpoofaxContextPrimitive {

    private final ISpoofaxProjectConfigService configService;

    @Inject public STX_is_concurrent_enabled(ISpoofaxProjectConfigService configService) {
        super(STX_is_concurrent_enabled.class.getSimpleName(), 0, 0);
        this.configService = configService;
    }

    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
            ITermFactory factory, IContext context) throws MetaborgException, IOException {
        @Nullable ISpoofaxProjectConfig spoofaxConfig = configService.get(context.project());
        if(spoofaxConfig != null && spoofaxConfig.statixConcurrent()) {
            return current;
        }
        return null;
    }

}