package org.metaborg.spoofax.core.messages;

import java.util.Arrays;

public class AffectedSourceHelper {
    public static final char AFFECTED = '^';
    public static final char BLANK = ' ';
    public static final char TAB = '\t';
    public static final char NEWLINE = '\n';


    public static String affectedSourceText(ISourceRegion region, String originText, String indentation) {
        final String[] affectedRows = affectedRows(originText, region.startRow(), region.endRow());

        if(affectedRows == null || affectedRows.length == 0)
            return TAB + "(code region unavailable)" + NEWLINE;

        final String[] damagedLines =
            weaveAffectedLines(affectedRows, region.startColumn(), region.endColumn());
        final StringBuilder sb = new StringBuilder();
        for(String dl : damagedLines) {
            sb.append(indentation + dl + NEWLINE);
        }
        return sb.toString();
    }

    private static String[] affectedRows(String originText, int beginLine, int endLine) {
        if(originText.length() > 0 && beginLine > 0 && endLine > 0) {
            final String[] lines = originText.split("\\r?\\n");
            if(beginLine - 1 > lines.length)
                return new String[0];
            return Arrays.copyOfRange(lines, beginLine - 1, endLine);
        } else
            return new String[0];
    }

    private static String[] weaveAffectedLines(String[] lines, int beginColumn, int endColumn) {
        String[] damagedLines = new String[lines.length * 2];
        for(int i = 0; i < lines.length; i++) {
            String line = lines[i];
            damagedLines[i] = line;
            int beginOffset = i == 0 ? beginColumn - 1 : 0;
            int endOffset = i + 1 == lines.length ? endColumn - 1 : line.length();
            char[] damageChars = line.toCharArray();
            for(int j = 0; j < damageChars.length; j++) {
                if(beginOffset <= j && endOffset >= j) {
                    damageChars[j] = AFFECTED;
                } else {
                    char dc = damageChars[j];
                    if(dc != TAB && dc != BLANK) {
                        dc = BLANK;
                    }
                    damageChars[j] = dc;
                }
            }
            damagedLines[i + 1] = new String(damageChars);
        }
        return damagedLines;
    }
}
