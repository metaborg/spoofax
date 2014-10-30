package org.metaborg.spoofax.core.messages;

import java.util.Arrays;

public class CodeRegionHelper {
    public static final char DAMAGE = '^';
    public static final char BLANK = ' ';
    public static final char TAB = '\t';
    public static final char NEWLINE = '\n';


    public static String[] getAffectedLines(String input, int beginLine, int endLine) {
        if(input.length() > 0 && beginLine > 0 && endLine > 0) {
            final String[] lines = input.split("\\r?\\n");
            if(beginLine - 1 > lines.length)
                return new String[0];
            return Arrays.copyOfRange(lines, beginLine - 1, endLine);
        } else
            return new String[0];
    }

    public static String[] weaveDamageLines(String[] lines, int beginColumn, int endColumn) {
        String[] damagedLines = new String[lines.length * 2];
        for(int i = 0; i < lines.length; i++) {
            String line = lines[i];
            damagedLines[i] = line;
            int beginOffset = i == 0 ? beginColumn - 1 : 0;
            int endOffset = i + 1 == lines.length ? endColumn - 1 : line.length();
            char[] damageChars = line.toCharArray();
            for(int j = 0; j < damageChars.length; j++) {
                if(beginOffset <= j && endOffset >= j) {
                    damageChars[j] = DAMAGE;
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
