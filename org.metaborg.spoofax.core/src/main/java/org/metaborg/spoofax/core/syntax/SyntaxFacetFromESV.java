package org.metaborg.spoofax.core.syntax;

import static org.spoofax.interpreter.core.Tools.termAt;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.syntax.FenceCharacters;
import org.metaborg.core.syntax.MultiLineCommentCharacters;
import org.metaborg.spoofax.core.esv.ESVReader;
import org.metaborg.util.iterators.Iterables2;
import org.spoofax.interpreter.terms.IStrategoAppl;

import com.google.common.collect.Lists;

public class SyntaxFacetFromESV {
    public static @Nullable SyntaxFacet create(IStrategoAppl esv, FileObject location) throws FileSystemException {
        final String parseTableLocation = parseTableLocation(esv);
        //final String completionTableLocation = "target/metaborg/sdf-completions.tbl";
        final FileObject parseTable;
        if(parseTableLocation == null) {
            parseTable = null;
        } else {
            parseTable = location.resolveFile(parseTableLocation);
        }
        //if()
        final FileObject completionParseTable = null; //location.resolveFile(completionTableLocation);
        final Iterable<String> startSymbols = startSymbols(esv);
        final Iterable<String> singleLineCommentPrefixes = singleLineCommentPrefixes(esv);
        final Iterable<MultiLineCommentCharacters> multiLineCommentCharacters = multiLineCommentCharacters(esv);
        final Iterable<FenceCharacters> fenceCharacters = fenceCharacters(esv);
        final SyntaxFacet syntaxFacet =
            new SyntaxFacet(parseTable, completionParseTable, startSymbols, singleLineCommentPrefixes, multiLineCommentCharacters,
                fenceCharacters);
        return syntaxFacet;
    }


    private static @Nullable String parseTableLocation(IStrategoAppl document) {
        String file = ESVReader.getProperty(document, "Table");
        if(file == null) {
            return null;
        }
        if(!file.endsWith(".tbl")) {
            file += ".tbl";
        }
        return file;
    }

    private static Iterable<String> startSymbols(IStrategoAppl document) {
        final IStrategoAppl result = ESVReader.findTerm(document, "StartSymbols");
        if(result == null) {
            return null;
        }

        final String contents = ESVReader.termContents(termAt(termAt(result, 0), 0));
        return Iterables2.singleton(contents);
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
