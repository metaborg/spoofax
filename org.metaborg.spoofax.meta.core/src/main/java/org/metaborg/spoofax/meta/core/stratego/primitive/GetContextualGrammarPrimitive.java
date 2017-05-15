package org.metaborg.spoofax.meta.core.stratego.primitive;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.resource.ResourceService;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpecService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class GetContextualGrammarPrimitive extends AbstractPrimitive {
    private static final ILogger logger = LoggerUtils.logger(GetContextualGrammarPrimitive.class);

    @Inject private static Provider<ISpoofaxLanguageSpecService> languageSpecServiceProvider;

    private final IProjectService projectService;

    @Inject public GetContextualGrammarPrimitive(IProjectService projectService) {
        super("SSL_EXT_get_contextual_grammar", 0, 1);

        this.projectService = projectService;
    }


    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
        org.metaborg.core.context.IContext context = (org.metaborg.core.context.IContext) env.contextObject();

        String path = ((IStrategoString) tvars[0]).stringValue();
        ResourceService rs = context.injector().getInstance(ResourceService.class);
        FileObject fo = rs.resolve(path + "/target/metaborg/ctxgrammar.aterm");

        InputStream inputStream;
        String text = "";

        try {
            inputStream = fo.getContent().getInputStream();
            text = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
        } catch(IOException e) {
            e.printStackTrace();
        }

        final ITermFactory tf = env.getFactory();
        env.setCurrent(tf.parseFromString(text));
        
        return true;
    }
}
