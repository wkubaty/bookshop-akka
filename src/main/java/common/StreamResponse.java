package common;

import java.io.Serializable;

public class StreamResponse implements Serializable {
    private String line;

    public StreamResponse(String line) {
        this.line = line;
    }

    public String getLine() {
        return line;
    }
}
