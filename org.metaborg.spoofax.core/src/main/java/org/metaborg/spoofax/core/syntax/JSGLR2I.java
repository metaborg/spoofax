package org.metaborg.spoofax.core.syntax;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.spoofax.core.unit.ParseContrib;
import org.metaborg.util.time.Timer;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.shared.BadTokenException;
import org.spoofax.jsglr2.JSGLR2;
import org.spoofax.jsglr2.parsetable.IParseTable;
import org.spoofax.jsglr2.parsetable.ParseTableReadException;
import org.spoofax.jsglr2.parsetable.ParseTableReader;

public class JSGLR2I extends JSGLRI<IParseTable> {
    private final JSGLR2<?, ?, IStrategoTerm> parser;

    public JSGLR2I(IParserConfig config, ITermFactory termFactory, ILanguageImpl language, ILanguageImpl dialect, @Nullable FileObject resource, String input) throws IOException, ParseTableReadException {
        super(config, termFactory, language, dialect, resource, input);

        IParseTableTermProvider parseTableTermProvider = config.getParseTableProvider();
        FileObject grammar = resource.getParent().resolveFile("normgrammar.bin");
        
        this.parser = JSGLR2.standard(getParseTable(parseTableTermProvider, termFactory, grammar));
    }

    protected IParseTable parseTableFromTerm(IParseTableTermProvider parseTableTermProvider, ITermFactory termFactory, FileObject grammar) throws IOException {
        try {
            IStrategoTerm parseTableTerm = config.getParseTableProvider().parseTableTerm();
            
            return ParseTableReader.read(parseTableTerm);
        } catch(Exception e) {
            throw new IOException("Could not load parse table from " + resource, e);
        }
    }

    public ParseContrib parse(@Nullable JSGLRParserConfiguration parserConfig) throws IOException {
        if(parserConfig == null) {
            parserConfig = new JSGLRParserConfiguration();
        }

        final Timer timer = new Timer(true);
        
        final IStrategoTerm ast = parser.parse(input);

        final long duration = timer.stop();

        final boolean hasAst = ast != null;
        final Iterable<IMessage> messages = Collections.emptyList(); // TODO: add message if parse is invalid
        final boolean hasErrors = ast == null;
        return new ParseContrib(hasAst, hasAst && !hasErrors, ast, messages, duration);
    }
    
    public Set<BadTokenException> getCollectedErrors() {
        return Collections.emptySet();
    }
}
