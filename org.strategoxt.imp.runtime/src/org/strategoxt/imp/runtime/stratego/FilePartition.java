package org.strategoxt.imp.runtime.stratego;

import java.net.URI;

/**
 * Container for file URI and partition.
 */
public class FilePartition {
    public final URI file;
    public final String partition;

    public FilePartition(URI file, String partition) {
        this.file = file;
        this.partition = partition;
    }

    @Override
    public String toString() {
        String result = file.toString();
        if(partition != null)
            result += "," + partition;
        return result + ")";
    }
}
