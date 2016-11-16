package org.metaborg.meta.core.signature.generate;

import java.io.IOException;
import java.util.Collection;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.meta.core.signature.ConstructorSig;
import org.metaborg.meta.core.signature.ISig;
import org.metaborg.meta.core.signature.ISigVisitor;
import org.metaborg.meta.core.signature.ISort;
import org.metaborg.meta.core.signature.InjectionSig;
import org.metaborg.util.collection.HashSetMultiTable;
import org.metaborg.util.collection.MultiTable;
import org.metaborg.util.file.IFileAccess;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class SigGenHelper {
    private final Iterable<ISigGen> generators;


    public SigGenHelper(Iterable<ISigGen> generators) {
        this.generators = generators;
    }


    public void generate(Iterable<ISig> signatures, final FileObject dir, final @Nullable IFileAccess access)
        throws IOException {

        for(ISigGen generator : generators) {
            generator.start(dir, access);
        }

        final Multimap<String, ISort> sortInjections = HashMultimap.create();
        final MultiTable<String, Integer, String> constructorSorts = HashSetMultiTable.create();
        final Collection<ConstructorSig> constructorSignatures = Lists.newArrayList();

        for(ISig sig : signatures) {
            sig.accept(new ISigVisitor() {
                @Override public void visitInjection(InjectionSig sig) {
                    sortInjections.put(sig.sort, sig.argument);
                }

                @Override public void visitApplication(ConstructorSig sig) {
                    constructorSorts.put(sig.constructor, sig.arity, sig.sort);
                    constructorSignatures.add(sig);
                }
            });
        }

        for(Entry<String, Collection<ISort>> entry : sortInjections.asMap().entrySet()) {
            final String sort = entry.getKey();
            final Iterable<ISort> injections = entry.getValue();
            for(ISigGen generator : generators) {
                generator.generateSort(sort, injections);
            }
        }

        for(ConstructorSig sig : constructorSignatures) {
            final Iterable<String> sorts = constructorSorts.get(sig.constructor, sig.arity);
            for(ISigGen generator : generators) {
                generator.generateConstructor(sig.constructor, sorts, sig.arguments, sig.arity);
            }
        }
    }
}
