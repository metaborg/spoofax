package org.metaborg.spoofax.core.completion.jsglr;

import java.util.Collection;

import org.metaborg.spoofax.core.completion.ICompletionItem;
import org.metaborg.spoofax.core.esv.ESVReader;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class CompletionFacetFromItemSets {
	public static CompletionFacet create(IStrategoAppl itemSets) {
		// collect itemSets
		final Iterable<IStrategoAppl> terms = ESVReader.collectTerms(itemSets, "ItemSet");

		final Multimap<Integer, CompletionDefinition> completionDefinitionMap = ArrayListMultimap.create();

		for (IStrategoAppl term : terms) {
			final int state = Tools.asJavaInt(term.getSubterm(0));
			final Iterable<CompletionDefinition> completionDefinitions = completionDefinition(term);
			for (CompletionDefinition completionDefinition : completionDefinitions) {
				completionDefinitionMap.put(state, completionDefinition);
			}
		}
		return new CompletionFacet(completionDefinitionMap);
	}

	public static Iterable<CompletionDefinition> completionDefinition(IStrategoAppl term) {
		// ItemSet : Label * List(Item) * List(goto) * Completions -> ItemSet
		// Completions : List(CompletionItem) -> Completions
		// CompletionItem : ProducedSort * Expected * Description * String -> CompletionItem

		// CompletionItem(cf(sort("Stm")),
		// "Stm-CF = \"print\" Num-LEX {default(appl(unquoted(\"cons\"),[[fun(quoted(\"Print\"))]]))}",
		// "Num")

		final Collection<CompletionDefinition> completionDefinitions = Lists.newLinkedList();
		final IStrategoTerm completions = term.getSubterm(3).getSubterm(0);

		for (IStrategoTerm completion : completions) {
			final IStrategoTerm producedSortTerm = completion.getSubterm(0);
			final IStrategoTerm expectedSortTerm = completion.getSubterm(1);
			final String description = Tools.asJavaString(completion.getSubterm(2));
			final String item = Tools.asJavaString(completion.getSubterm(3));

			final Collection<ICompletionItem> items = Lists.newLinkedList();
			items.add(new TextCompletionItem(item));
			final CompletionDefinition completionDefinition = new CompletionDefinition(producedSortTerm, expectedSortTerm, description, items);
			completionDefinitions.add(completionDefinition);
		}

		return completionDefinitions;
	}

}
