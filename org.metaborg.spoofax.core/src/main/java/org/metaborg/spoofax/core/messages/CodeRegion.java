package org.metaborg.spoofax.core.messages;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.spoofax.jsglr.client.imploder.IToken;

public class CodeRegion implements ICodeRegion {
    private static final Logger logger = LogManager.getLogger(CodeRegion.class.getName());

    private final int startRow;
    private final int startColumn;
    private final int endRow;
    private final int endColumn;
    private String[] affectedLines;


    public CodeRegion(int startRow, int startColumn, int endRow, int endColumn, String input) {
        this.startRow = startRow;
        this.startColumn = startColumn;
        this.endRow = endRow;
        this.endColumn = endColumn;

        if(input != null)
            this.affectedLines = CodeRegionHelper.getAffectedLines(input, startRow, endRow);
    }


    @Override public int startRow() {
        return startRow;
    }

    @Override public int startColumn() {
        return startColumn;
    }

    @Override public int endRow() {
        return endRow;
    }

    @Override public int endColumn() {
        return endColumn;
    }

    @Override public String damagedRegion(String indentation) {
        if(affectedLines == null || affectedLines.length == 0)
            return CodeRegionHelper.TAB + "(code region unavailable)" + CodeRegionHelper.NEWLINE;

        final String[] damagedLines = CodeRegionHelper.weaveDamageLines(affectedLines, startColumn, endColumn);
        final StringBuilder sb = new StringBuilder();
        for(String dl : damagedLines) {
            sb.append(indentation + dl + CodeRegionHelper.NEWLINE);
        }
        return sb.toString();
    }


    public static CodeRegion fromTokens(IToken left, IToken right, IResourceService resourceService) {
        boolean leftDone = false, rightDone = false;
        int leftLine = 0, leftColumn = 0, rightLine = 0, rightColumn = 0;
        final String fileContents = getAttachedInput(left, right, resourceService);
        if(fileContents.length() > 0) {
            char[] input = fileContents.toCharArray();
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
                    leftLine = currentLine;
                    leftColumn = currentColumn;
                }
                if(!rightDone && i == right.getEndOffset()) {
                    rightLine = currentLine;
                    rightColumn = currentColumn;
                }
                if(rightDone && leftDone) {
                    break;
                }
            }
            return new CodeRegion(leftLine, leftColumn, rightLine, rightColumn, fileContents);
        } else {
            return new CodeRegion(left.getLine() + 1, left.getColumn() + 1, right.getEndLine() + 1,
                right.getEndColumn() + 1, "");
        }
    }

    private static String getAttachedInput(IToken left, IToken right, IResourceService resourceService) {
        String input = null;
        input = left.getTokenizer().getInput();
        if(input == null) {
            input = right.getTokenizer().getInput();
        }
        if(input == null) {
            try {
                final FileObject file = resourceService.resolve(left.getTokenizer().getFilename());
                input = IOUtils.toString(file.getContent().getInputStream());
            } catch(IOException e) {
                logger.warn("Cannot read file contents to determine affected code region", e);
                input = "";
            }
        }
        return input;
    }

    
    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + startRow;
        result = prime * result + startColumn;
        result = prime * result + endRow;
        result = prime * result + endColumn;
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;

        final CodeRegion other = (CodeRegion) obj;
        if(startRow != other.startRow)
            return false;
        if(startColumn != other.startColumn)
            return false;
        if(endRow != other.endRow)
            return false;
        if(endColumn != other.endColumn)
            return false;

        return true;
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(startRow);
        sb.append(",");
        sb.append(startColumn);
        sb.append(":");
        sb.append(endRow);
        sb.append(",");
        sb.append(endColumn);
        return sb.toString();
    }
}
