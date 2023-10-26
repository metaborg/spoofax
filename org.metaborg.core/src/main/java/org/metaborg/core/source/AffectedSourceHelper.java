package org.metaborg.core.source;

import java.util.Collections;

import jakarta.annotation.Nullable;

import org.metaborg.util.Strings;

/**
 * Helper class for highlighting code in consoles.
 */
public class AffectedSourceHelper {
    /**
     * Returns a multi-line string that highlights the affected source code region, given the full source text and
     * indentation to use.
     * 
     * @param region
     *            Region in the source text that should be highlighted.
     * @param sourceText
     *            Full source text.
     * @param indentation
     *            Indentation to add to each line in the resulting string.
     * @return Multi-line string that highlights the affected source code region.
     */
    public static @Nullable String affectedSourceText(ISourceRegion region, String sourceText, String indentation) {
        final int startOffset = region.startOffset();
        final int endOffset = region.endOffset();

        int startRow = -1;
        int endRow = -1;
        int startExtend = Integer.MAX_VALUE;
        int endExtend = 0;
        int pos = 0;
        final String[] lines = sourceText.split("\\r?\\n");
        for(int i = 0; i < lines.length; ++i) {
            final String line = lines[i];
            final int length = line.length();

            final int startDist = startOffset - pos;
            if(startDist >= 0 && startDist <= length) {
                startExtend = Math.min(startExtend, startDist);
            }

            final int endDist = endOffset - pos;
            if(endDist >= 0 && endDist <= length) {
                endExtend = Math.max(endExtend, endDist + 1);
            }

            pos += length + 1; // + 1 because newline characters are stripped out.
            if(startRow == -1 && pos >= startOffset) {
                startRow = i;
            }
            if(startRow != -1) {
                if(pos >= endOffset) {
                    endRow = i;
                    break;
                } else {
                    startExtend = 0;
                    endExtend = Math.max(endExtend, length);
                }
            }
        }

        if(endRow == -1 || startExtend == Integer.MAX_VALUE) {
            return null;
        }

        final StringBuilder builder = new StringBuilder();
        for(int i = startRow; i <= endRow; ++i) {
            builder.append(indentation);
            builder.append(lines[i].replace('\t', ' '));
            builder.append('\n');
        }
        if (startExtend <= endExtend) {
            // WORKAROUND: On Windows apparently startExtend can be bigger than endExtend,
            // causing an IllegalArgumentException: invalid count: -22
            // As a workaround, we skip generating the ^^^^, but this is of course a bug.

            builder.append(indentation);
            builder.append(Strings.repeat(" ", startExtend));
            builder.append(Strings.repeat("^", endExtend - startExtend));
            builder.append('\n');
        }

        return builder.toString();
    }
}
