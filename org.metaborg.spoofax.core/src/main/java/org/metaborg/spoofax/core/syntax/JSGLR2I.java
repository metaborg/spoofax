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
import org.spoofax.jsglr2.JSGLR2Variants;

import com.google.common.collect.Lists;

public class JSGLR2I extends JSGLRI<IParseTable> {

    private final IParseTable parseTable;
    private final JSGLR2<IStrategoTerm> parser;

    public JSGLR2I(IParserConfig config, ITermFactory termFactory, ILanguageImpl language, ILanguageImpl dialect,
        JSGLRVersion parserType) throws IOException {
        super(config, termFactory, language, dialect);

        this.parseTable = getParseTable(config.getParseTableProvider());
        this.parser = jsglrVersionToVariant(parserType).getJSGLR2(parseTable);
    }

    // TODO the two enums JSGLRVersion and JSGLR2Variants should be linked together,
    // but JSGLR2 cannot depend on org.metaborg.core and the other way around.
    // org.metaborg.spoofax.core depends on both, that's why the linking now happens here.
    // To fix this properly, JSGLRVersion should move to org.metaborg.spoofax.core,
    // because org.metaborg.core should be tool-agnositic and should not need to know the specifics of JSGLR(2).
    // JSGLRVersion can then have a JSGLR2Variants.Variant as field.
    private JSGLR2Variants.Variant jsglrVersionToVariant(JSGLRVersion jsglrVersion) {
        switch(jsglrVersion) {
            case dataDependent:
                return JSGLR2Variants.dataDependent.variant;
            case incremental:
                return JSGLR2Variants.incremental.variant;
            case layoutSensitive:
                return JSGLR2Variants.layoutSensitive.variant;
            case v2:
            default:
                return JSGLR2Variants.standard.variant;
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
