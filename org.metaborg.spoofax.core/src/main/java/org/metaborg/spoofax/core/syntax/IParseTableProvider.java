package org.metaborg.spoofax.core.syntax;

import java.io.IOException;

public interface IParseTableProvider {
	// Return type is Object because JSGLR v1/v2 use different parse table representations and thus the
	// parse table object is casted to the actual parse table type within the parser implementation itself
	Object parseTable() throws IOException;
}
