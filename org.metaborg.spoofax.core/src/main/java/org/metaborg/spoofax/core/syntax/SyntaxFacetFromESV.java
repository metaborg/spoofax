package org.metaborg.spoofax.core.syntax;

import static org.spoofax.interpreter.core.Tools.termAt;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.spoofax.core.esv.ESVReader;
import org.spoofax.interpreter.terms.IStrategoAppl;

import com.google.common.collect.Sets;

public class SyntaxFacetFromESV {
    public static SyntaxFacet create(IStrategoAppl esv, FileObject location) throws FileSystemException {
        final FileObject parseTable = location.resolveFile(parseTableName(esv));
        final String startSymbol = startSymbol(esv); // GTODO: multiple start symbols
        final SyntaxFacet syntaxFacet = new SyntaxFacet(parseTable, Sets.newHashSet(startSymbol));
        return syntaxFacet;
    }

    private static String startSymbol(IStrategoAppl document) {
        final IStrategoAppl result = ESVReader.findTerm(document, "StartSymbols");
        if(result == null)
            return null;

        return ESVReader.termContents(termAt(termAt(result, 0), 0));
    }

    private static String parseTableName(IStrategoAppl document) {
        String file = ESVReader.getProperty(document, "Table", ESVReader.getProperty(document, "LanguageName"));
        if(!file.endsWith(".tbl"))
            file += ".tbl";
        return file;
    }
}
