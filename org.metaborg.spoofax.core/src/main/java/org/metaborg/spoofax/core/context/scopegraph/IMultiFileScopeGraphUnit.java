package org.metaborg.spoofax.core.context.scopegraph;

import java.util.Optional;

import org.metaborg.meta.nabl2.constraints.IConstraint;
import org.metaborg.meta.nabl2.spoofax.analysis.IScopeGraphUnit;
import org.metaborg.meta.nabl2.spoofax.analysis.UnitResult;

public interface IMultiFileScopeGraphUnit extends IScopeGraphUnit {

    void setUnitResult(UnitResult result);

    Optional<UnitResult> unitResult();

    void setNormalizedConstraints(Iterable<IConstraint> result);

    Optional<Iterable<IConstraint>> normalizedConstraints();
    
    void clear();

}
