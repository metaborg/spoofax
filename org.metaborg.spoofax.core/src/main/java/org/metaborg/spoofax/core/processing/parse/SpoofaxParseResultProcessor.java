package org.metaborg.spoofax.core.processing.parse;

import org.metaborg.core.processing.parse.ParseResultProcessor;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;

import com.google.inject.Inject;

/**
 * Typedef class for {@link ParseResultProcessor} with Spoofax interfaces.
 */
public class SpoofaxParseResultProcessor extends ParseResultProcessor<ISpoofaxInputUnit, ISpoofaxParseUnit>
    implements ISpoofaxParseResultProcessor {
    @Inject public SpoofaxParseResultProcessor(ISpoofaxSyntaxService syntaxService) {
        super(syntaxService);
    }
}
