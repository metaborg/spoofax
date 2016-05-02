package org.metaborg.spoofax.meta.core.pluto.build.misc;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.sugarj.common.FileCommands;

import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.Out;
import build.pluto.output.OutputPersisted;
import build.pluto.output.OutputTransient;
import build.pluto.stamp.FileHashStamper;
import build.pluto.stamp.Stamper;

public class ParseFile extends SpoofaxBuilder<ParseFile.Input, Out<IStrategoTerm>> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -4790160594622807382L;

        public final File file;
        public final boolean persistResult;
        public final Origin requiredUnits;


        public Input(SpoofaxContext context, File file, boolean persistResult, Origin requiredUnits) {
            super(context);
            this.file = file;
            this.persistResult = persistResult;
            this.requiredUnits = requiredUnits;
        }
    }


    public final static SpoofaxBuilderFactory<Input, Out<IStrategoTerm>, ParseFile> factory =
        SpoofaxBuilderFactoryFactory.of(ParseFile.class, Input.class);


    public ParseFile(Input input) {
        super(input);
    }


    public static
        BuildRequest<Input, Out<IStrategoTerm>, ParseFile, SpoofaxBuilderFactory<Input, Out<IStrategoTerm>, ParseFile>>
        request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        return "Parse file " + input.file;
    }

    @Override protected Stamper defaultStamper() {
        return FileHashStamper.instance;
    }

    @Override public File persistentPath(Input input) {
        final String rel = input.file.getPath();
        final String relname = rel.replace(File.separatorChar, '_');
        return context.depPath("parse." + relname + ".dep");
    }

    @Override protected Out<IStrategoTerm> build(Input input) throws Throwable {
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
        final ISpoofaxInputUnit inputUnit = context.unitService().inputUnit(resource, text, language, null);
        final ISpoofaxParseUnit result = context.syntaxService().parse(inputUnit);
        if(!result.valid()) {
            return null;
        }

        return input.persistResult ? OutputPersisted.of(result.ast()) : OutputTransient.of(result.ast());
    }
}
