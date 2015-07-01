package org.metaborg.spoofax.core.source;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * Helper class for highlighting code in consoles.
 */
public class AffectedSourceHelper {
    public static final char AFFECTED = '^';
    public static final char BLANK = ' ';
    public static final char TAB = '\t';
    public static final char NEWLINE = '\n';


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
    public static String affectedSourceText(ISourceRegion region, String sourceText, String indentation) {
        final String[] affectedRows = affectedRows(sourceText, region.startRow() + 1, region.endRow() + 1);

        if(affectedRows == null || affectedRows.length == 0) {
            return indentation + "(code region unavailable)" + NEWLINE;
        }

        final String[] affectedLines =
            weaveAffectedLines(affectedRows, region.startColumn() + 1, region.endColumn() + 1);
        final StringBuilder sb = new StringBuilder();
        for(String dl : affectedLines) {
            sb.append(indentation + dl + NEWLINE);
        }
        return sb.toString();
    }


    private static String[] affectedRows(String originText, int beginLine, int endLine) {
        if(originText.length() > 0 && beginLine > 0 && endLine > 0) {
            final String[] lines = originText.split("\\r?\\n");
            if(beginLine - 1 <= lines.length) {
                return Arrays.copyOfRange(lines, beginLine - 1, endLine);
            }
        }

        return new String[0];
    }

    private static String[] weaveAffectedLines(String[] lines, int beginColumn, int endColumn) {
        final String[] affectedRows = new String[lines.length * 2];
        for(int i = 0; i < lines.length; i++) {
            final String line = lines[i].replace(TAB, BLANK);
            affectedRows[i] = line;

            final int beginOffset = i == 0 ? beginColumn - 1 : 0;
            final int endOffset = i + 1 == lines.length ? endColumn - 1 : line.length();

            final List<Character> newChars = Lists.newArrayList();
            for(int j = 0; j < line.length(); j++) {
                if(beginOffset <= j && endOffset >= j) {
                    newChars.add(AFFECTED);
                } else {
                    newChars.add(BLANK);
                }
            }
            affectedRows[i + 1] = Joiner.on("").join(newChars);
        }
        return affectedRows;
    }
}
