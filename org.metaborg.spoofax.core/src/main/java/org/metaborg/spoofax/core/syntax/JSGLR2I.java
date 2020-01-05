package org.metaborg.spoofax.core.syntax;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.JSGLRVersion;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageFactory;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.parsetable.IParseTable;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.metaborg.spoofax.core.unit.ParseContrib;
import org.metaborg.util.time.Timer;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.jsglr.shared.BadTokenException;
import org.spoofax.jsglr2.JSGLR2;

import com.google.common.collect.Lists;

public class JSGLR2I extends JSGLRI<IParseTable> {
    private final JSGLR2<IStrategoTerm> parser;
    private final IParseTable parseTable;


    public JSGLR2I(IParserConfig config, ITermFactory termFactory, ILanguageImpl language, ILanguageImpl dialect,
        JSGLRVersion parserType) throws IOException {
        super(config, termFactory, language, dialect);

        IParseTableProvider parseTableProvider = config.getParseTableProvider();
        parseTable = getParseTable(parseTableProvider);

        switch(parserType) {
            case dataDependent:
                this.parser = JSGLR2.dataDependent(parseTable);
                break;
            case incremental:
                this.parser = JSGLR2.incremental(parseTable);
                break;
            case layoutSensitive:
                this.parser = JSGLR2.layoutSensitive(parseTable);
                break;
            case v2:
            default:
                this.parser = JSGLR2.standard(parseTable);
                break;
        }
    }

    @Override public ParseContrib parse(@Nullable JSGLRParserConfiguration parserConfig, @Nullable FileObject resource,
        String input) {
        if(parserConfig == null) {
            parserConfig = new JSGLRParserConfiguration();
        }

        final String fileName = resource != null ? resource.getName().getURI() : null;
        String startSymbol = getOrDefaultStartSymbol(parserConfig);

        final Timer timer = new Timer(true);

        final IStrategoTerm ast = parser.parse(input, fileName, startSymbol);


        final List<IMessage> messages = Lists.newArrayList();

        // add non-assoc warnings to messages
        messages.addAll(addDisambiguationWarnings(ast, resource));

        final long duration = timer.stop();

        final boolean hasAst = ast != null;
        final boolean hasErrors = ast == null;

        if(hasErrors)
            messages.add(MessageFactory.newParseErrorAtTop(resource, "Invalid syntax", null));

        return new ParseContrib(hasAst, hasAst && !hasErrors, ast, messages, duration);
    }

    @Override public Set<BadTokenException> getCollectedErrors() {
        return Collections.emptySet();
    }

    private Collection<? extends IMessage> addDisambiguationWarnings(IStrategoTerm ast, @Nullable FileObject resource) {
        List<IMessage> result = Lists.newArrayList();

        boolean addedMessage = false;

        // non-associative and non-nested operators should be flagged with warnings
        if(ast instanceof IStrategoAppl && ast.getAllSubterms().length >= 1) {
            String sortConsParent =
                ImploderAttachment.getSort(ast) + "." + ((IStrategoAppl) ast).getConstructor().getName();

            IStrategoTerm firstChild = ast.getSubterm(0);
            IStrategoTerm lastChild = ast.getSubterm(ast.getSubtermCount() - 1);

            if(firstChild instanceof IStrategoAppl) {
                IStrategoAppl leftMostChild = (IStrategoAppl) firstChild;
                @Nullable ImploderAttachment leftMostChildAttachment = ImploderAttachment.get(leftMostChild);
                String sortConsChild = ImploderAttachment.getSort(ast) + "." + leftMostChild.getConstructor().getName();
                if(leftMostChildAttachment != null && !leftMostChildAttachment.isBracket()
                    && parseTable instanceof ParseTable && ((ParseTable) parseTable).normalizedGrammar()
                        .getNonAssocPriorities().containsEntry(sortConsParent, sortConsChild)) {
                    ISourceRegion region = JSGLRSourceRegionFactory.fromTokens(ImploderAttachment.getLeftToken(ast),
                        ImploderAttachment.getRightToken(ast));
                    result.add(MessageFactory.newParseWarning(resource, region, "Operator is non-associative", null));
                    addedMessage = true;
                }
            }

            if(lastChild instanceof IStrategoAppl) {
                IStrategoAppl rightMostChild = (IStrategoAppl) lastChild;
                @Nullable ImploderAttachment rightMostChildAttachment = ImploderAttachment.get(rightMostChild);
                String sortConsChild =
                    ImploderAttachment.getSort(ast) + "." + rightMostChild.getConstructor().getName();
                if(rightMostChildAttachment != null && !rightMostChildAttachment.isBracket()
                    && parseTable instanceof ParseTable && ((ParseTable) parseTable).normalizedGrammar()
                        .getNonNestedPriorities().containsEntry(sortConsParent, sortConsChild)) {
                    ISourceRegion region = JSGLRSourceRegionFactory.fromTokens(ImploderAttachment.getLeftToken(ast),
                        ImploderAttachment.getRightToken(ast));
                    result.add(MessageFactory.newParseWarning(resource, region, "Operator is non-nested", null));
                    addedMessage = true;
                }
            }
        }

        if(ast != null && !addedMessage) {
            for(IStrategoTerm child : ast.getAllSubterms()) {
                result.addAll(addDisambiguationWarnings(child, resource));
            }
        }

        return result;
    }
}
