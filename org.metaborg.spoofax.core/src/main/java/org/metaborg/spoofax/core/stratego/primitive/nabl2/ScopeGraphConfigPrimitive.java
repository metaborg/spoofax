package org.metaborg.spoofax.core.stratego.primitive.nabl2;

import java.io.IOException;
import java.util.Optional;

import jakarta.annotation.Nullable;

import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.core.config.ISpoofaxProjectConfigService;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxPrimitive;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import mb.nabl2.config.NaBL2Config;

public abstract class ScopeGraphConfigPrimitive extends ASpoofaxPrimitive {

    private final ISpoofaxProjectConfigService configService;

    public ScopeGraphConfigPrimitive(String name, ISpoofaxProjectConfigService configService) {
        super(name, 0, 0);
        this.configService = configService;
    }

    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
            ITermFactory factory, IContext context) throws MetaborgException, IOException {
        final NaBL2Config config = Optional.ofNullable(metaborgContext(context))
                .flatMap(ctx -> Optional.ofNullable(configService.get(ctx.project()))).map(cfg -> cfg.nabl2Config())
                .orElse(NaBL2Config.DEFAULT);
        return call(config, current);
    }

    protected abstract @Nullable IStrategoTerm call(NaBL2Config config, IStrategoTerm term);

}
