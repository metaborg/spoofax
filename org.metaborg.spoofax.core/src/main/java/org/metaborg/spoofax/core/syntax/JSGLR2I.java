package org.metaborg.spoofax.core.syntax;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageFactory;
import org.metaborg.spoofax.core.unit.ParseContrib;
import org.metaborg.util.time.Timer;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.shared.BadTokenException;
import org.spoofax.jsglr2.JSGLR2;
import org.spoofax.jsglr2.parsetable.IParseTable;
import org.spoofax.jsglr2.parsetable.ParseTableReadException;

public class JSGLR2I extends JSGLRI<IParseTable> {
    private final JSGLR2<?, ?, IStrategoTerm> parser;

    public JSGLR2I(IParserConfig config, ITermFactory termFactory, ILanguageImpl language, ILanguageImpl dialect, @Nullable FileObject resource, String input) throws IOException, ParseTableReadException {
        super(config, termFactory, language, dialect, resource, input);

        IParseTableProvider parseTableProvider = config.getParseTableProvider();
        IParseTable parseTable = getParseTable(parseTableProvider);
        
        this.parser = JSGLR2.standard(parseTable);
    }

    public ParseContrib parse(@Nullable JSGLRParserConfiguration parserConfig) throws IOException {
        if(parserConfig == null) {
            parserConfig = new JSGLRParserConfiguration();
        }
        
        final String fileName = resource != null ? resource.getName().getURI() : null;
        String startSymbol = getOrDefaultStartSymbol(parserConfig);

        final Timer timer = new Timer(true);
        
        final IStrategoTerm ast = parser.parse(input, fileName, startSymbol);

        final long duration = timer.stop();

        final boolean hasAst = ast != null;
        final boolean hasErrors = ast == null;
        
        final Iterable<IMessage> messages;

        if (hasErrors)
        		messages = Collections.singletonList(MessageFactory.newParseErrorAtTop(resource, "Invalid syntax", null));
        else
    			messages = Collections.emptyList();
        	
        return new ParseContrib(hasAst, hasAst && !hasErrors, ast, messages, duration);
    }
    
    public Set<BadTokenException> getCollectedErrors() {
        return Collections.emptySet();
    }
}
