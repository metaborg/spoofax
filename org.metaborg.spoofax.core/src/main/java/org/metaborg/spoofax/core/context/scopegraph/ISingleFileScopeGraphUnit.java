package org.metaborg.spoofax.core.context.scopegraph;

import org.metaborg.scopegraph.INameResolution;
import org.metaborg.scopegraph.IScopeGraph;
import org.metaborg.scopegraph.context.IScopeGraphUnit;
import org.metaborg.scopegraph.impl.ASTMetadata;
import org.metaborg.scopegraph.impl.OccurrenceTypes;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface ISingleFileScopeGraphUnit extends IScopeGraphUnit {

    void setAnalysis(IStrategoTerm analysis);

    void setScopeGraph(IScopeGraph scopeGraph);

    void setNameResolution(INameResolution nameResolution);

    void setAstMetadata(ASTMetadata astMetadata);

    void setOccurrenceTypes(OccurrenceTypes occurrenceTypes);
    
    void clear();

}