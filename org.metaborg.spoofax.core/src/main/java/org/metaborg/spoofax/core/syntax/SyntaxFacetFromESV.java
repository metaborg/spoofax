package org.metaborg.spoofax.core.syntax;

import static org.spoofax.interpreter.core.Tools.termAt;

import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.spoofax.core.esv.ESVReader;
import org.metaborg.util.iterators.Iterables2;
import org.spoofax.interpreter.terms.IStrategoAppl;

import com.google.common.collect.Lists;

public class SyntaxFacetFromESV {
    public static SyntaxFacet create(IStrategoAppl esv, FileObject location) throws FileSystemException {
        final FileObject parseTable = location.resolveFile(parseTableName(esv));
        final Iterable<String> startSymbols = startSymbols(esv);
        final Iterable<String> singleLineCommentPrefixes = singleLineCommentPrefixes(esv);
        final Iterable<MultiLineCommentCharacters> multiLineCommentCharacters = multiLineCommentCharacters(esv);
        final Iterable<FenceCharacters> fenceCharacters = fenceCharacters(esv);
        final SyntaxFacet syntaxFacet =
            new SyntaxFacet(parseTable, startSymbols, singleLineCommentPrefixes, multiLineCommentCharacters,
                fenceCharacters);
        return syntaxFacet;
    }


    private static String parseTableName(IStrategoAppl document) {
        String file = ESVReader.getProperty(document, "Table", ESVReader.getProperty(document, "LanguageName"));
        if(!file.endsWith(".tbl"))
            file += ".tbl";
        return file;
    }

    private static Iterable<String> startSymbols(IStrategoAppl document) {
        // GTODO: multiple start symbols
        final IStrategoAppl result = ESVReader.findTerm(document, "StartSymbols");
        if(result == null)
            return null;

        return Iterables2.singleton(ESVReader.termContents(termAt(termAt(result, 0), 0)));
    }

    private static Iterable<String> singleLineCommentPrefixes(IStrategoAppl document) {
        final Collection<String> lineCommentPrefixes = Lists.newLinkedList();
        final Iterable<IStrategoAppl> terms = ESVReader.collectTerms(document, "LineCommentPrefix");
        for(IStrategoAppl term : terms) {
            lineCommentPrefixes.add(ESVReader.termContents(term.getSubterm(0)));
        }
        return lineCommentPrefixes;
    }

    private static Iterable<MultiLineCommentCharacters> multiLineCommentCharacters(IStrategoAppl document) {
        final Collection<MultiLineCommentCharacters> multiLineCommentCharacters = Lists.newLinkedList();
        final Iterable<IStrategoAppl> terms = ESVReader.collectTerms(document, "BlockCommentDef");
        for(IStrategoAppl term : terms) {
            final String prefix = ESVReader.termContents(term.getSubterm(0));
            final String postfix = ESVReader.termContents(term.getSubterm(2));
            multiLineCommentCharacters.add(new MultiLineCommentCharacters(prefix, postfix));
        }
        return multiLineCommentCharacters;
    }

    private static Iterable<FenceCharacters> fenceCharacters(IStrategoAppl document) {
        final Collection<FenceCharacters> fenceCharacters = Lists.newLinkedList();
        final Iterable<IStrategoAppl> terms = ESVReader.collectTerms(document, "FenceDef");
        for(IStrategoAppl term : terms) {
            final String open = ESVReader.termContents(term.getSubterm(0));
            final String close = ESVReader.termContents(term.getSubterm(1));
            fenceCharacters.add(new FenceCharacters(open, close));
        }
        return fenceCharacters;
    }
}
