package org.metaborg.spoofax.meta.core.pluto.build.misc;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.sugarj.common.FileCommands;

import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.OutputPersisted;
import build.pluto.stamp.FileHashStamper;
import build.pluto.stamp.Stamper;

public class ParseFile extends SpoofaxBuilder<ParseFile.Input, OutputPersisted<IStrategoTerm>> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -4790160594622807382L;

        public final File file;
        public final Origin requiredUnits;


        public Input(SpoofaxContext context, File defPath, Origin requiredUnits) {
            super(context);
            this.file = defPath;
            this.requiredUnits = requiredUnits;
        }
    }


    public final static SpoofaxBuilderFactory<Input, OutputPersisted<IStrategoTerm>, ParseFile> factory =
        SpoofaxBuilderFactoryFactory.of(ParseFile.class, Input.class);


    public ParseFile(Input input) {
        super(input);
    }


    public static
        BuildRequest<Input, OutputPersisted<IStrategoTerm>, ParseFile, SpoofaxBuilderFactory<Input, OutputPersisted<IStrategoTerm>, ParseFile>>
        request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        return "Parse SDF definition";
    }

    @Override protected Stamper defaultStamper() {
        return FileHashStamper.instance;
    }

    @Override public File persistentPath(Input input) {
        final String rel = input.file.getPath();
        final String relname = rel.replace(File.separatorChar, '_');
        return context.depPath("parse." + relname + ".dep");
    }

    @Override protected OutputPersisted<IStrategoTerm> build(Input input) throws Throwable {
        requireBuild(input.requiredUnits);

        require(input.file);
        if(!FileCommands.exists(input.file)) {
            return null;
        }

        final FileObject resource = context.resourceService().resolve(input.file);
        final ILanguageImpl language = context.languageIdentifierService().identify(resource);
        if(language == null) {
            return null;
        }
        final String text = context.sourceTextService().text(resource);
        final ParseResult<IStrategoTerm> result = context.syntaxService().parse(text, resource, language, null);
        if(result.result == null) {
            return null;
        }

        return OutputPersisted.of(result.result);
    }
}
