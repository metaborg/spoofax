package org.metaborg.spoofax.core.stratego.primitive.nabl2;

import java.io.IOException;
import java.util.Optional;

import javax.annotation.Nullable;

import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.core.config.ISpoofaxProjectConfigService;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxPrimitive;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import mb.nabl2.config.NaBL2Config;
import mb.nabl2.config.NaBL2ConfigReaderWriter;

public abstract class ScopeGraphConfigPrimitive extends ASpoofaxPrimitive {

    private final ISpoofaxProjectConfigService configService;

    public ScopeGraphConfigPrimitive(String name, ISpoofaxProjectConfigService configService) {
        super(name, 0, 0);
        this.configService = configService;
    }

    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
            ITermFactory factory, IContext context) throws MetaborgException, IOException {

        // @formatter:off
        final NaBL2Config nabl2Config = Optional
                .ofNullable(metaborgContext(context))
                .map(ctx -> configService.get(ctx.project()))
                .map(cfg -> cfg.runtimeConfig().get("nabl2"))
                .map(NaBL2ConfigReaderWriter::read)
                .orElse(NaBL2Config.DEFAULT);
        // @formatter:on

        return call(nabl2Config, current);
    }

    protected abstract @Nullable IStrategoTerm call(NaBL2Config config, IStrategoTerm term);

}