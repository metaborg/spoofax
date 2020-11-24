package org.metaborg.spoofax.meta.core.stratego.primitive;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.resource.ResourceService;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.io.binary.TermReader;

import com.google.inject.Inject;

public class GetContextualGrammarPrimitive extends AbstractPrimitive {
    @Inject public GetContextualGrammarPrimitive() {
        super("SSL_EXT_get_contextual_grammar", 0, 1);
    }


    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
        org.metaborg.core.context.IContext context = (org.metaborg.core.context.IContext) env.contextObject();

        String path = ((IStrategoString) tvars[0]).stringValue();
        ResourceService rs = context.injector().getInstance(ResourceService.class);
        FileObject fo = rs.resolve(path + "/target/metaborg/ctxgrammar.aterm");
        final ITermFactory tf = env.getFactory();

        try {
            InputStream inputStream = fo.getContent().getInputStream();
            env.setCurrent(new TermReader(tf).parseFromStream(inputStream));
        } catch(IOException e) {
            e.printStackTrace();
        }

        return true;
    }
}
