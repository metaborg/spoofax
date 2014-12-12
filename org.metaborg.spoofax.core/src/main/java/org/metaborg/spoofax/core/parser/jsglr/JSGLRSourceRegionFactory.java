package org.metaborg.spoofax.core.parser.jsglr;

import org.metaborg.spoofax.core.messages.SourceRegion;
import org.spoofax.jsglr.client.imploder.IToken;

public class JSGLRSourceRegionFactory {
    public static SourceRegion fromSourceText(IToken left, IToken right, String sourceText) {
        boolean leftDone = false, rightDone = false;
        int leftRow = 0, leftColumn = 0, rightRow = 0, rightColumn = 0;
        char[] input = sourceText.toCharArray();
        int currentLine = 1;
        int currentColumn = 0;
        for(int i = 0; i < input.length; i++) {
            char c = input[i];
            if(c == '\n' || c == '\r') {
                currentLine++;
                currentColumn = 0;
            } else {
                currentColumn++;
            }

            if(!leftDone && i == left.getStartOffset()) {
                leftRow = currentLine;
                leftColumn = currentColumn;
            }
            if(!rightDone && i == right.getEndOffset()) {
                rightRow = currentLine;
                rightColumn = currentColumn;
            }
            if(rightDone && leftDone) {
                break;
            }
        }
        return new SourceRegion(leftRow, leftColumn, rightRow, rightColumn);
    }

    public static SourceRegion fromTokens(IToken left, IToken right) {
        return new SourceRegion(left.getLine() + 1, left.getColumn() + 1, right.getEndLine() + 1,
            right.getEndColumn() + 1);
    }
}
