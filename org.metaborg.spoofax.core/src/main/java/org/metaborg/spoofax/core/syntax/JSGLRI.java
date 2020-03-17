package org.metaborg.spoofax.core.syntax;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageFactory;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.spoofax.core.unit.ParseContrib;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.jsglr.shared.BadTokenException;
import org.spoofax.terms.util.TermUtils;

abstract public class JSGLRI<PT> {
    protected final IParserConfig config;
    protected final ITermFactory termFactory;
    protected final ILanguageImpl language;
    protected final ILanguageImpl dialect;
    
    protected PT parseTable;

    public JSGLRI(IParserConfig config, ITermFactory termFactory, ILanguageImpl language, ILanguageImpl dialect) {
        this.config = config;
        this.termFactory = termFactory;
        this.language = language;
        this.dialect = dialect;
    }

    @SuppressWarnings("unchecked") protected PT getParseTable(IParseTableProvider parseTableProvider)
        throws IOException {
        // Since JSGLR v1 and v2 use different parse table representations we have to cast here
        return (PT) parseTableProvider.parseTable();
    }

    abstract public ParseContrib parse(@Nullable JSGLRParserConfiguration parserConfig, @Nullable FileObject resource,
        String input);

    abstract public Set<BadTokenException> getCollectedErrors();

    protected String getOrDefaultStartSymbol(@Nullable JSGLRParserConfiguration parserConfig) {
        if(parserConfig != null && parserConfig.overridingStartSymbol != null) {
            return parserConfig.overridingStartSymbol;
        } else {
            return config.getStartSymbol();
        }
    }

    public IParserConfig getConfig() {
        return config;
    }

    public ILanguageImpl getLanguage() {
        return language;
    }

    public ILanguageImpl getDialect() {
        return dialect;
    }

    abstract public SetMultimap<String, String> getNonAssocPriorities();

    abstract public SetMultimap<String, String> getNonNestedPriorities();

    protected Collection<? extends IMessage> addDisambiguationWarnings(IStrategoTerm ast,
        @Nullable FileObject resource) {
        List<IMessage> result = Lists.newArrayList();

        boolean addedMessage = false;

        // non-associative and non-nested operators should be flagged with warnings
        if(TermUtils.isAppl(ast) && ast.getAllSubterms().length >= 1) {
            String sortConsParent =
                ImploderAttachment.getSort(ast) + "." + ((IStrategoAppl) ast).getConstructor().getName();

            IStrategoTerm firstChild = ast.getSubterm(0);
            IStrategoTerm lastChild = ast.getSubterm(ast.getSubtermCount() - 1);

            if(TermUtils.isAppl(firstChild)) {
                IStrategoAppl leftMostChild = (IStrategoAppl) firstChild;
                @Nullable ImploderAttachment leftMostChildAttachment = ImploderAttachment.get(leftMostChild);
                String sortConsChild = ImploderAttachment.getSort(ast) + "." + leftMostChild.getConstructor().getName();
                if(leftMostChildAttachment != null && !leftMostChildAttachment.isBracket()
                    && parseTable instanceof ParseTable
                    && getNonAssocPriorities().containsEntry(sortConsParent, sortConsChild)) {
                    ISourceRegion region = JSGLRSourceRegionFactory.fromTokens(ImploderAttachment.getLeftToken(ast),
                        ImploderAttachment.getRightToken(ast));
                    result.add(MessageFactory.newParseWarning(resource, region, "Operator is non-associative", null));
                    addedMessage = true;
                }
            }

            if(TermUtils.isAppl(lastChild)) {
                IStrategoAppl rightMostChild = (IStrategoAppl) lastChild;
                @Nullable ImploderAttachment rightMostChildAttachment = ImploderAttachment.get(rightMostChild);
                String sortConsChild =
                    ImploderAttachment.getSort(ast) + "." + rightMostChild.getConstructor().getName();
                if(rightMostChildAttachment != null && !rightMostChildAttachment.isBracket()
                    && parseTable instanceof ParseTable
                    && getNonNestedPriorities().containsEntry(sortConsParent, sortConsChild)) {
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
