package org.metaborg.spoofax.core.completion.jsglr;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.SpoofaxRuntimeException;
import org.metaborg.spoofax.core.esv.ESVReader;
import org.metaborg.spoofax.core.syntax.jsglr.SortCons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CompletionFacetFromESV {
    private static final Logger logger = LoggerFactory.getLogger(CompletionFacetFromESV.class);


    public static CompletionFacet create(IStrategoAppl esv) {
        final Iterable<IStrategoAppl> terms = ESVReader.collectTerms(esv, "CompletionTemplateEx");
        final Map<SortCons, CompletionDefinition> completionDefinitions = Maps.newHashMap();
        for(IStrategoAppl term : terms) {
            final CompletionDefinition completionDefinition = completionDefinition(term);
            completionDefinitions.put(new SortCons(completionDefinition.sort, completionDefinition.cons),
                completionDefinition);
        }
        return new CompletionFacet(completionDefinitions);
    }

    public static CompletionDefinition completionDefinition(IStrategoAppl term) {
        final String sort = Tools.asJavaString(term.getSubterm(0).getSubterm(0).getSubterm(0));
        // TODO: get constructor from ESV when eduardo adds it.
        final String cons = "nothing yet";
        // CompletionTemplateEx([ListSort("ImpSection")],NoCompletionPrefix,[String("\"imports\""),String("\"\\n\\t\""),Cursor],[Blank])
        final IStrategoTerm descriptionTerm = term.getSubterm(1);
        final String description;
        if(descriptionTerm.getSubtermCount() == 0) {
            description = "";
        } else {
            description = ESVReader.termContents(descriptionTerm.getSubterm(0).getSubterm(0)); 
        }
        final Iterable<IStrategoTerm> itemTerms = term.getSubterm(2);
        final Collection<ICompletionItem> items = Lists.newLinkedList();
        for(IStrategoTerm itemTerm : itemTerms) {
            final ICompletionItem item = item((IStrategoAppl) itemTerm);
            if(item != null) {
                items.add(item);
            }
        }
        return new CompletionDefinition(sort, cons, description, items);
    }

    public static @Nullable ICompletionItem item(IStrategoAppl term) {
        final String consName = term.getConstructor().getName();
        switch(consName) {
            case "String":
                return new StringCompletionItem(ESVReader.termContents(term.getSubterm(0)));
            case "PlaceholderWithSort":
                final String name = Tools.asJavaString(term.getSubterm(0));
                // HACK: strip first and last character of the name, to get rid of : and >.
                // TODO: remove hack when eduardo removes the bogus characters.
                final String strippedName;
                if(name.length() > 2) {
                    strippedName = name.substring(1, name.length() - 1);
                } else {
                    strippedName = name;
                }
                return new PlaceholderCompletionItem(Tools.asJavaString(term.getSubterm(1)), strippedName);
            case "Placeholder":
                // Name without a sort, ignore because no useful completions can be given here.
                return null;
            case "Cursor":
                return new CursorCompletionItem();
        }

        final String message = String.format("Unhandled completion item term %s", term);
        logger.error(message);
        throw new SpoofaxRuntimeException(message);
    }
}
